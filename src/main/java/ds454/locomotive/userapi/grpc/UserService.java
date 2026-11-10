package ds454.locomotive.userapi.grpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ds454.locomotive.userapi.repository.UserRepository;
import ds454.locomotive.userapi.spring.BeanProvider;
import io.grpc.stub.StreamObserver;
import jakarta.transaction.Transactional;
import ds454.locomotive.generated.grpc.AddFriendRequest;
import ds454.locomotive.generated.grpc.AddFriendResponse;
import ds454.locomotive.generated.grpc.AddUserRequest;
import ds454.locomotive.generated.grpc.AddUserResponse;
import ds454.locomotive.generated.grpc.DeleteAccountRequest;
import ds454.locomotive.generated.grpc.DeleteAccountResponse;
import ds454.locomotive.generated.grpc.GetUserRequest;
import ds454.locomotive.generated.grpc.GetUserResponse;
import ds454.locomotive.generated.grpc.HasMetResponse;
import ds454.locomotive.generated.grpc.RemoveFriendRequest;
import ds454.locomotive.generated.grpc.RemoveFriendResponse;
import ds454.locomotive.generated.grpc.ValidateUserRequest;
import ds454.locomotive.generated.grpc.ValidateUserResponse;
import ds454.locomotive.generated.grpc.UserServiceGrpc.UserServiceImplBase;
import ds454.locomotive.generated.grpc.UpdateUserRequest;
import ds454.locomotive.generated.grpc.UpdateUserResponse;

public class UserService extends UserServiceImplBase {
    // This is what performs the logging. A Logger object writes to the log files and terminal.
    private static final Logger log = LoggerFactory.getLogger(HelloService.class);
    
    // This is how Java interacts with the SQL part of the database.
    UserRepository userRepo = BeanProvider.getBean(UserRepository.class);
    
    @Override
    public void addUser(AddUserRequest request, StreamObserver<AddUserResponse> responseObserver) {
        // Log that the request came in.
        log.info("Received add user request!");
        // TODO: Add server interceptor to get metadata from the request
        // Since the user doesn't exist yet, they will not have a UUID or a steam id.
        
        // Initialize variables
        var response = HasMetResponse.newBuilder();
        String username = request.getUsername();
        String email = request.getEmailAddress();
        String steamAccountName = request.getSteamAccountName();
        String steamAccountId = request.getSteamAccountId();
        // Create the user, and don't forget error handling, especially with add requests.
        // Validate Steam Account. If invalid, return an error. Use GetUserInformationFromSteamId
        
        // Make sure the username is unique. If not, return an error.
        
        // User newUser = new User(username, email, steamAccountName, steamAccountId)
        
        // Log the new user was created
        
        // Return the new user or a Problem with problem type and message.
    }
    
    @Override
    @Transactional
    public void getUser(GetUserRequest request, StreamObserver<GetUserResponse> responseObserver) {
        
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
    public void validateUserCredentials(ValidateUserRequest request, StreamObserver<ValidateUserResponse> responseObserver) {
        
    }
    
    @Override
    public void updateUser(UpdateUserRequest request, StreamObserver<UpdateUserResponse> responseObserver) {
        
    }
    
}
