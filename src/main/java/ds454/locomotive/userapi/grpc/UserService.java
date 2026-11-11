package ds454.locomotive.userapi.grpc;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;

import ds454.locomotive.userapi.repository.UserRepository;
import ds454.locomotive.userapi.spring.BeanProvider;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import jakarta.transaction.Transactional;
import ds454.locomotive.generated.grpc.AddFriendRequest;
import ds454.locomotive.generated.grpc.AddFriendResponse;
import ds454.locomotive.generated.grpc.AddUserRequest;
import ds454.locomotive.generated.grpc.AddUserResponse;
import ds454.locomotive.generated.grpc.DeleteAccountRequest;
import ds454.locomotive.generated.grpc.DeleteAccountResponse;
import ds454.locomotive.generated.grpc.GetUserInformationFromSteamIdRequest;
import ds454.locomotive.generated.grpc.GetUserInformationFromSteamIdResponse;
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
import ds454.locomotive.generated.grpc.problem.Problem;
import ds454.locomotive.generated.grpc.problem.ProblemType;

public class UserService extends UserServiceImplBase {
    // This is what performs the logging. A Logger object writes to the log files and terminal.
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    
    // This is how Java interacts with the SQL part of the database.
    UserRepository userRepo = BeanProvider.getBean(UserRepository.class);
    
    // Interact with the Steam Service to validate account creation
    private final SteamServiceGrpc.SteamServiceBlockingStub steamStub =
        BeanProvider.getBean(SteamServiceGrpc.SteamServiceBlockingStub.class);
    
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
    public void updateUser(UpdateUserRequest request, StreamObserver<UpdateUserResponse> responseObserver) {
        
    }
    
    // Sends a single Problem and completes the stream. Every error path uses this.
    // TODO: Make a utility class in the util folder and put this and other helper methods in it.
    private <T> void sendProblem(StreamObserver<T> observer, ProblemType type, String field,
            String message, Function<Problem, T> responseFactory) {
        Problem.Builder problem = Problem.newBuilder().setProblem(type).setMessage(message);

        if (field != null) {
            problem.setField(field);
        }

        observer.onNext(responseFactory.apply(problem.build()));
        observer.onCompleted();
    }
    
}