package ds454.locomotive.userapi.grpc;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;

import ds454.locomotive.generated.grpc.AddUserRequest;
import ds454.locomotive.generated.grpc.AddUserResponse;
import ds454.locomotive.generated.grpc.GetUserInformationFromSteamIdRequest;
import ds454.locomotive.generated.grpc.GetUserInformationFromSteamIdResponse;
import ds454.locomotive.generated.grpc.LocomotiveUserInformation;
import ds454.locomotive.generated.grpc.SteamServiceGrpc;
import ds454.locomotive.generated.grpc.AddUserServiceGrpc.AddUserServiceImplBase;
import ds454.locomotive.generated.grpc.problem.ProblemType;
import ds454.locomotive.userapi.model.User;
import ds454.locomotive.userapi.repository.UserRepository;
import ds454.locomotive.userapi.spring.BeanProvider;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import ds454.locomotive.userapi.util.GrpcUtils;

public class AddUserService extends AddUserServiceImplBase {
    
// This is what performs the logging. A Logger object writes to the log files and terminal.
    private static final Logger log = LoggerFactory.getLogger(AddUserService.class);
    
    // This is how Java interacts with the SQL part of the database.
    UserRepository userRepo = BeanProvider.getBean(UserRepository.class);
    
    private final ManagedChannel steamChannel =
        ManagedChannelBuilder.forAddress("steam-service", 50051).usePlaintext().build();

    private final SteamServiceGrpc.SteamServiceBlockingStub steamStub =
        SteamServiceGrpc.newBlockingStub(steamChannel);
        
        
    @Override
    public void addUser(AddUserRequest request, StreamObserver<AddUserResponse> responseObserver) {
        // Log that the request came in.
        log.info("Received add user request!");
        // Since the user doesn't exist yet, they will not have a UUID or a steam id.
        
        // Initialize variables
        String username = request.getUsername();
        String steamAccountId = request.getSteamAccountId();
        String password = request.getPassword();
        String steamAccountName;
        // Create the user, and don't forget error handling, especially with add requests.
        // Validate Steam Account. If invalid, return an error. Use GetUserInformationFromSteamId
        
        // Make sure the username is unique. If not, return an error.
        
        // User newUser = new User(username, email, steamAccountName, steamAccountId)
        
        // Log the new user was created
        
        // Return the new user or a Problem with problem type and message.
        
        // No requester UUID here: the new user doesn't have one yet, and this
        // request bypasses ext_authz, so don't read HeaderInterceptor.UserUuid.

        // 1. Basic input checks. Proto3 strings are never null, only empty.
        if (username.isBlank()) {
            GrpcUtils.sendProblem(responseObserver, ProblemType.InvalidInput, "username", "Username is required",
                    problem -> AddUserResponse.newBuilder().setProblem(problem).build());
            return;
        }
        if (steamAccountId.isBlank()) {
            GrpcUtils.sendProblem(responseObserver, ProblemType.InvalidInput, "steamAccountId", "Steam account ID is required",
                    problem -> AddUserResponse.newBuilder().setProblem(problem).build());
            return;
        }

        // 2. Username must be unique (cheap local check before the network call).
        if (userRepo.existsByUsername(username)) {
            GrpcUtils.sendProblem(responseObserver, ProblemType.InvalidInput, "username", "Username is already taken",
                    problem -> AddUserResponse.newBuilder().setProblem(problem).build());
            return;
        }

        // 3. Validate the Steam account info with the Steam service.
        GetUserInformationFromSteamIdResponse steamResponse;
        try {
            steamResponse = steamStub.withDeadlineAfter(5, TimeUnit.SECONDS).getUserInformationFromSteamId(
                                GetUserInformationFromSteamIdRequest.newBuilder().setSteamId(steamAccountId).build());
        } catch (StatusRuntimeException e) {
            // Transport-level failure: Steam service down, timed out, etc.
            log.warn("Steam service call failed: {}", e.getStatus());
            GrpcUtils.sendProblem(responseObserver, ProblemType.UnableToConnectToSteam, "steamAccountId",
                    "Could not reach the Steam service",
                    problem -> AddUserResponse.newBuilder().setProblem(problem).build());
            return;
        }

        switch (steamResponse.getResultCase()) {
            case PROBLEM -> {
                // The Steam service already classified it (InvalidSteamID, SteamAccountIsPrivate, ...).
                log.info("Steam validation problem: {}", steamResponse.getProblem().getProblem());
                responseObserver.onNext(AddUserResponse.newBuilder().setProblem(steamResponse.getProblem()).build());
                responseObserver.onCompleted();
                return;
            }
            case STEAM_USER_INFO -> {
                steamAccountName = steamResponse.getSteamUserInfo().getSteamUsername();
            }
            case RESULT_NOT_SET -> {
                GrpcUtils.sendProblem(responseObserver, ProblemType.ServerError, null,
                        "Steam service returned an empty response",
                        problem -> AddUserResponse.newBuilder().setProblem(problem).build());
                return;
            }
            default -> {
                log.error("Impossible state reached! Steam service returned {}, which was not handled", steamResponse.getResultCase().name());
                GrpcUtils.sendProblem(responseObserver, ProblemType.ServerError, null,
                        "Internal Server Error",
                        problem -> AddUserResponse.newBuilder().setProblem(problem).build());
                return;
            }
        }

        // 4. Save. Hibernate generates the UUID here. The unique constraint on
        //    username backstops the race between the check in step 2 and this insert.
        try {
            User saved = userRepo.save(new User(username, steamAccountName, steamAccountId, password));
            log.info("Created user {} ({})", saved.getUsername(), saved.getId());
            // 5. Return the new user's information. A brand-new user has no friends.
            var info = LocomotiveUserInformation.newBuilder().setUsername(saved.getUsername())
                                .setSteamAccountName(steamAccountName)
                                .setSteamAccountId(steamAccountId).build();

            responseObserver.onNext(AddUserResponse.newBuilder().setUserInfo(info).build());
            responseObserver.onCompleted();
        } catch (DataIntegrityViolationException e) {
            GrpcUtils.sendProblem(responseObserver, ProblemType.InvalidInput, "username", "Username is already taken",
                    problem -> AddUserResponse.newBuilder().setProblem(problem).build());
        }
    }
    
}