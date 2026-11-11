package ds454.locomotive.userapi.grpc;

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ds454.locomotive.generated.grpc.InternalUserServiceGrpc.InternalUserServiceImplBase;
import ds454.locomotive.generated.grpc.SteamServiceGrpc;
import ds454.locomotive.userapi.model.User;
import ds454.locomotive.userapi.repository.UserRepository;
import ds454.locomotive.userapi.spring.BeanProvider;
import io.grpc.stub.StreamObserver;

import ds454.locomotive.generated.grpc.ValidateCredentialRequest;
import ds454.locomotive.generated.grpc.ValidateCredentialResponse;
import ds454.locomotive.generated.grpc.UserIds;
import ds454.locomotive.generated.grpc.problem.Problem;
import ds454.locomotive.generated.grpc.problem.ProblemType;

/**
 * 
 * InternalUserService is used to handle internal requests from different services to the user service.
 * It is necessary because the other services may make requests that are not authenticated.
 * When this is added to the User Service API, InternalUserService is NOT bound to a header interceptor.
 * Not binding it to a header interceptor prevents it from requiring authentication.
 * 
 */
public class InternalUserService extends InternalUserServiceImplBase {
    // This is what performs the logging. A Logger object writes to the log files and terminal.
    private static final Logger log = LoggerFactory.getLogger(InternalUserService.class);
    
    // This is how Java interacts with the SQL part of the database.
    UserRepository userRepo = BeanProvider.getBean(UserRepository.class);
    
    // Interact with the Steam Service to validate account creation
    private final SteamServiceGrpc.SteamServiceBlockingStub steamStub =
        BeanProvider.getBean(SteamServiceGrpc.SteamServiceBlockingStub.class);
    
        /**        (non-Javadoc)
         * Validates a set of credentials provided by the authentication service.
         * It ensures they correctly refer to a valid user that can log in.
         * 
         * @see ds454.locomotive.generated.grpc.InternalUserServiceGrpc.AsyncService#validateCredentials(ds454.locomotive.generated.grpc.ValidateCredentialRequest, io.grpc.stub.StreamObserver)
         */
    @Override
    public void validateCredentials(ValidateCredentialRequest request,
        StreamObserver<ValidateCredentialResponse> responseObserver) {
        
        String username = request.getUsername();
        String password = request.getPassword();
        // TODO: implement logging
        var userFromRepo = userRepo.findByUsername(username);
        if (userFromRepo.isEmpty()) {
            // The user did not exist
            sendProblem(responseObserver, ProblemType.InvalidInput, "username", "Username is required",
                    problem -> ValidateCredentialResponse.newBuilder().setProblem(problem).build());
            return;
        }
        User user = userFromRepo.get();
        if (password.equals(user.getPassword())) {
            UserIds ids = UserIds.newBuilder().setUuid(user.getId().toString()).setSteamid(user.getSteamAccountId()).build();
            responseObserver.onNext(ValidateCredentialResponse.newBuilder().setIds(ids).build());
            responseObserver.onCompleted();
        } else {
            sendProblem(responseObserver, ProblemType.InvalidUsernamePassword, null,
                    "Invalid username or password",
                    problem -> ValidateCredentialResponse.newBuilder().setProblem(problem).build());
        }
    }
    
    // Sends a single Problem and completes the stream. Every error path uses this.
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
