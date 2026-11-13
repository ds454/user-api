package ds454.locomotive.userapi.grpc;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ds454.locomotive.userapi.repository.UserRepository;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.springframework.transaction.annotation.Transactional;
import ds454.locomotive.generated.grpc.AddFriendRequest;
import ds454.locomotive.generated.grpc.AddFriendResponse;
import ds454.locomotive.generated.grpc.AddUserResponse;
import ds454.locomotive.generated.grpc.DeleteAccountRequest;
import ds454.locomotive.generated.grpc.DeleteAccountResponse;
import ds454.locomotive.generated.grpc.GetThisUserRequest;
import ds454.locomotive.generated.grpc.GetThisUserResponse;
import ds454.locomotive.generated.grpc.GetUserRequest;
import ds454.locomotive.generated.grpc.GetUserResponse;
import ds454.locomotive.generated.grpc.LocomotiveUserInformation;
import ds454.locomotive.generated.grpc.RemoveFriendRequest;
import ds454.locomotive.generated.grpc.RemoveFriendResponse;
import ds454.locomotive.generated.grpc.SteamServiceGrpc;
import ds454.locomotive.generated.grpc.UserServiceGrpc.UserServiceImplBase;
import ds454.locomotive.generated.grpc.UpdateUserRequest;
import ds454.locomotive.generated.grpc.UpdateUserResponse;
import ds454.locomotive.userapi.model.User;
import ds454.locomotive.generated.grpc.problem.ProblemType;

import ds454.locomotive.userapi.util.GrpcUtils;

/**
 * The UserService class implements all the functions that use Envoy Proxy's header metadata.
 * These functions only work for authenticated 
 */
@Component
public class UserService extends UserServiceImplBase {
    // This is what performs the logging. A Logger object writes to the log files and terminal.
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    
    // This is how Java interacts with the SQL part of the database.
    private final UserRepository userRepo;
    
    private final ManagedChannel steamChannel =
        ManagedChannelBuilder.forAddress("steam-service", 50051).usePlaintext().build();

    private final SteamServiceGrpc.SteamServiceBlockingStub steamStub =
        SteamServiceGrpc.newBlockingStub(steamChannel);

    public UserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }
    
    @Override
    @Transactional
    public void getUser(GetUserRequest request, StreamObserver<GetUserResponse> responseObserver) {
        // TODO: logging
        // Retrieve UUID from request body
        String bodyUuid = request.getUuid();
        var userFromRepo = userRepo.findById(UUID.fromString(bodyUuid));
        
        // Catch errors with UUID.
        if (userFromRepo.isEmpty()) {
            log.warn("Received invalid UUID " + bodyUuid + ".", bodyUuid);
            GrpcUtils.sendProblem(responseObserver, ProblemType.InvalidInput,"header UUID",
                "Invalid UUID extracted from header" + bodyUuid + ".",
                problem -> GetUserResponse.newBuilder().setProblem(problem).build());
            return;
        }
        User user = userFromRepo.get();
        // Build response from user data
        var info = LocomotiveUserInformation.newBuilder().setUsername(user.getUsername()).setEmailAddress(user.getEmailAddress())      
                        .setSteamAccountName(user.getSteamAccountName()).setSteamAccountId(user.getSteamAccountId()).build();
        
        responseObserver.onNext(GetUserResponse.newBuilder().setUserInfo(info).build());
        responseObserver.onCompleted();
    }
    
    @Override
    @Transactional
    public void getThisUser(GetThisUserRequest request, StreamObserver<GetThisUserResponse> responseObserver) {
        // TODO: logging
        // Retrieve UUID from request body
        UUID uuid = UUID.fromString(HeaderInterceptor.UserUuid.get());
        var userFromRepo = userRepo.findById(uuid);
        
        // Catch errors with UUID.
        if (userFromRepo.isEmpty()) {
            log.warn("Received invalid UUID " + uuid.toString() + ".", uuid);
            GrpcUtils.sendProblem(responseObserver, ProblemType.InvalidInput,"header UUID",
                "Invalid UUID extracted from header" + uuid.toString() + ".",
                problem -> GetThisUserResponse.newBuilder().setProblem(problem).build());
            return;
        }
        User user = userFromRepo.get();
        // Build response from user data
        var info = LocomotiveUserInformation.newBuilder().setUsername(user.getUsername()).setEmailAddress(user.getEmailAddress())      
                        .setSteamAccountName(user.getSteamAccountName()).setSteamAccountId(user.getSteamAccountId()).build();
        
        responseObserver.onNext(GetThisUserResponse.newBuilder().setUserInfo(info).build());
        responseObserver.onCompleted();
    }
    
    @Override
    @Transactional 
    public void addFriend(AddFriendRequest request, StreamObserver<AddFriendResponse> responseObserver) {
        // TODO: logging
        // Get user requesting a new friend
        UUID uuid = UUID.fromString(HeaderInterceptor.UserUuid.get());
        var userFromRepo = userRepo.findById(uuid);
        
        // Get new friend
        UUID friendUuid = UUID.fromString(request.getFriendUuid());
        var friendFromRepo = userRepo.findById(friendUuid);
        
        if (uuid.equals(friendUuid)) {
            GrpcUtils.sendProblem(responseObserver, ProblemType.InvalidInput, "friendUuid",
                "You cannot add yourself as a friend",
                problem -> AddFriendResponse.newBuilder().setProblem(problem).build());
            return;
        }
        
        // Validate both users exist.
        if (userFromRepo.isEmpty()) {
            log.warn("Received invalid UUID " + uuid.toString() + ".", uuid);
            GrpcUtils.sendProblem(responseObserver, ProblemType.InvalidInput,"header UUID",
                "Invalid UUID extracted from header" + uuid.toString() + ".",
                problem -> AddFriendResponse.newBuilder().setProblem(problem).build());
            return;
        } 
        if (friendFromRepo.isEmpty()) {
            log.warn("Received invalid friend UUID " + friendUuid.toString() + ".", friendUuid);
            GrpcUtils.sendProblem(responseObserver, ProblemType.InvalidInput,"friend UUID",
                "Invalid UUID for new friend" + friendUuid.toString() + ".",
                problem -> AddFriendResponse.newBuilder().setProblem(problem).build());
            return;
        }
        
        // Add friendFromRepo to the userFromRepo's friend list and add userFromRepo to friendFromRepo's friend list.
        User user = userFromRepo.get();
        User friend = friendFromRepo.get();
        
        user.addFriend(friendUuid);
        friend.addFriend(uuid);
        userRepo.save(user);
        userRepo.save(friend);
        
        // Return userFromRepo's updated friend list
        AddFriendResponse.Builder responseBuilder = AddFriendResponse.newBuilder().setSuccess(true);
        for (UUID friendId : user.getFriends()) {
            if (friendId == null) {
                // Since a friend was just added, this should never happen.
                throw new IllegalStateException("Friend list contains a null UUID");
            }
            responseBuilder.addFriendIds(friendId.toString());
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
        
    }
    
    @Override
    @Transactional 
    public void removeFriend(RemoveFriendRequest request, StreamObserver<RemoveFriendResponse> responseObserver) {
        // TODO: logging. Consider handling exceptions for malformed UUIDs/validating UUIDs before using.
        // Exceptions are undesirable in the output log, no?
        
        // Get user requesting to remove a friend
        UUID uuid = UUID.fromString(HeaderInterceptor.UserUuid.get());
        var userFromRepo = userRepo.findById(uuid);
        
        // Get friend
        UUID friendUuid = UUID.fromString(request.getFriendUuid());
        var friendFromRepo = userRepo.findById(friendUuid);
        
        if (uuid.equals(friendUuid)) {
            GrpcUtils.sendProblem(responseObserver, ProblemType.InvalidInput, "friendUuid",
                "You cannot remove yourself as a friend",
                problem -> RemoveFriendResponse.newBuilder().setProblem(problem).build());
            return;
        }
        
        // Validate both users exist.
        if (userFromRepo.isEmpty()) {
            log.warn("Received invalid UUID " + uuid.toString() + ".", uuid);
            GrpcUtils.sendProblem(responseObserver, ProblemType.InvalidInput,"header UUID",
                "Invalid UUID extracted from header" + uuid.toString() + ".",
                problem -> RemoveFriendResponse.newBuilder().setProblem(problem).build());
            return;
        } 
        if (friendFromRepo.isEmpty()) {
            log.warn("Received invalid friend UUID " + friendUuid.toString() + ".", friendUuid);
            GrpcUtils.sendProblem(responseObserver, ProblemType.InvalidInput,"friend UUID",
                "Invalid UUID for friend to remove" + friendUuid.toString() + ".",
                problem -> RemoveFriendResponse.newBuilder().setProblem(problem).build());
            return;
        }
        
        // Remove friendFromRepo from the userFromRepo's friend list and remove userFromRepo from friendFromRepo's friend list.
        User user = userFromRepo.get();
        User friend = friendFromRepo.get();
        
        user.removeFriend(friendUuid);
        friend.removeFriend(uuid);
        userRepo.save(user);
        userRepo.save(friend);
        
        // Return userFromRepo's updated friend list
        RemoveFriendResponse.Builder responseBuilder = RemoveFriendResponse.newBuilder().setSuccess(true);
        for (UUID friendId : user.getFriends()) {
            if (friendId == null) {
                // Since a friend was just added, this should never happen.
                // TODO: Is this the best solution here?
                throw new IllegalStateException("Friend list contains a null UUID");
            }
            responseBuilder.addFriendIds(friendId.toString());
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }
    
    @Override 
    public void deleteAccount(DeleteAccountRequest request, StreamObserver<DeleteAccountResponse> responseObserver) {
        // TODO: The procedure to validate a user is repeated in every method. Place it in a helper method.
        // Retrieve UUID from request body
        UUID uuid = UUID.fromString(HeaderInterceptor.UserUuid.get());
        var userFromRepo = userRepo.findById(uuid);
        
        // Catch errors with UUID.
        if (userFromRepo.isEmpty()) {
            log.warn("Received invalid UUID " + uuid.toString() + ".", uuid);
            GrpcUtils.sendProblem(responseObserver, ProblemType.InvalidInput,"header UUID",
                "Invalid UUID extracted from header" + uuid.toString() + ".",
                problem -> DeleteAccountResponse.newBuilder().setProblem(problem).build());
            return;
        }
        
        String username = request.getUsername();
        
        // Why is this debug instead of info?
        log.debug("Received delete account request from user {}", uuid);
        
        // Delete the user
        User user = userFromRepo.get();
        userRepo.delete(user);
        
        // Construct and send response
        var result = DeleteAccountResponse.newBuilder().setGoodByeMessage("Steam on, " + username + ".");

        responseObserver.onNext(result.build());
        responseObserver.onCompleted();
    }
    
    // UpdateUser is implemented in diametric opposition to all best practices.
    // It is not cybersecure in any way.
    @Override
    public void updateUser(UpdateUserRequest request, StreamObserver<UpdateUserResponse> responseObserver) {
        // Retrieve UUID from request body
        UUID uuid = UUID.fromString(HeaderInterceptor.UserUuid.get());
        var userFromRepo = userRepo.findById(uuid);
        
        // Catch errors with UUID.
        if (userFromRepo.isEmpty()) {
            log.warn("Received invalid UUID " + uuid.toString() + ".", uuid);
            GrpcUtils.sendProblem(responseObserver, ProblemType.InvalidInput,"header UUID",
                "Invalid UUID extracted from header" + uuid.toString() + ".",
                problem -> UpdateUserResponse.newBuilder().setProblem(problem).build());
            return;
        }
        
        // Get and update the fields of the user.
        // TODO: honestly, it doesn't matter. Add updating the password here.
        User user = userFromRepo.get();
        user.setUsername(request.getUsername());
        user.setSteamAccountName(request.getSteamAccountName());
        user.setSteamAccountId(request.getSteamAccountId());
        user.setEmailAddress(request.getEmailAddress());
        user.setPassword(request.getPassword());
        // Save to repository
        userRepo.save(user);
        
        // Send response
        log.info("Updated user {} ({})", user.getUsername(), user.getId());
        // 5. Return the new user's information. A brand-new user has no friends.
        var info = LocomotiveUserInformation.newBuilder().setUsername(user.getUsername())
                        .setEmailAddress(user.getEmailAddress()).setSteamAccountName(user.getSteamAccountName())
                        .setSteamAccountId(user.getSteamAccountId()).build();

        responseObserver.onNext(UpdateUserResponse.newBuilder().setUserInfo(info).build());
        responseObserver.onCompleted();
        
    }
    
}