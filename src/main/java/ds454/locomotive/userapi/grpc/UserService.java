package ds454.locomotive.userapi.grpc;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;

import ds454.locomotive.userapi.repository.UserRepository;
import ds454.locomotive.userapi.spring.BeanProvider;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import jakarta.transaction.Transactional;
import ds454.locomotive.generated.grpc.AddFriendRequest;
import ds454.locomotive.generated.grpc.AddFriendResponse;
import ds454.locomotive.generated.grpc.AddUserResponse;
import ds454.locomotive.generated.grpc.DeleteAccountRequest;
import ds454.locomotive.generated.grpc.DeleteAccountResponse;
import ds454.locomotive.generated.grpc.GetUserRequest;
import ds454.locomotive.generated.grpc.GetUserResponse;
import ds454.locomotive.generated.grpc.LocomotiveUserInformation;
import ds454.locomotive.generated.grpc.RemoveFriendRequest;
import ds454.locomotive.generated.grpc.RemoveFriendResponse;
import ds454.locomotive.generated.grpc.SteamServiceGrpc;
import ds454.locomotive.generated.grpc.UserServiceGrpc.UserServiceImplBase;
import ds454.locomotive.generated.grpc.ValidateCredentialResponse;
import ds454.locomotive.generated.grpc.UpdateUserRequest;
import ds454.locomotive.generated.grpc.UpdateUserResponse;
import ds454.locomotive.userapi.model.User;
import ds454.locomotive.generated.grpc.problem.Problem;
import ds454.locomotive.generated.grpc.problem.ProblemType;

import ds454.locomotive.userapi.util.GrpcUtils;

/**
 * The UserService class implements all the functions that use Envoy Proxy's header metadata.
 * These functions only work for authenticated 
 */
public class UserService extends UserServiceImplBase {
    // This is what performs the logging. A Logger object writes to the log files and terminal.
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    
    // This is how Java interacts with the SQL part of the database.
    UserRepository userRepo = BeanProvider.getBean(UserRepository.class);
    
    private final ManagedChannel steamChannel =
        ManagedChannelBuilder.forAddress("steam-service", 50051).usePlaintext().build();

    private final SteamServiceGrpc.SteamServiceBlockingStub steamStub =
        SteamServiceGrpc.newBlockingStub(steamChannel);
    
    @Override
    @Transactional
    public void getUser(GetUserRequest request, StreamObserver<GetUserResponse> responseObserver) {
        String headerUuid = HeaderInterceptor.UserUuid.get();
        
        UUID uuid = UUID.fromString(headerUuid);
        
        var userFromRepo = userRepo.findById(uuid);
        // Catch errors with UUID.
        if (userFromRepo.isEmpty()) {
            log.warn("Received invalid UUID %s.", headerUuid);
            GrpcUtils.sendProblem(responseObserver, ProblemType.InvalidInput,"header UUID",
                "Invalid UUID extracted from header" + headerUuid + ".",
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
    public void addFriend(AddFriendRequest request, StreamObserver<AddFriendResponse> responseObserver) {
        
    }
    
    @Override
    @Transactional 
    public void removeFriend(RemoveFriendRequest request, StreamObserver<RemoveFriendResponse> responseObserver) {
        
    }
    
    @Override 
    public void deleteAccount(DeleteAccountRequest request, StreamObserver<DeleteAccountResponse> responseObserver) {
        // Get the UUID of the person who sent the request
        var requesterUuid = HeaderInterceptor.UserUuid.get();

        var result = DeleteAccountResponse.newBuilder();

        log.debug("Received delete account request from user {}", requesterUuid);

        responseObserver.onNext(result.build());
        responseObserver.onCompleted();
    }
    
    @Override
    public void updateUser(UpdateUserRequest request, StreamObserver<UpdateUserResponse> responseObserver) {
        
    }
    
}