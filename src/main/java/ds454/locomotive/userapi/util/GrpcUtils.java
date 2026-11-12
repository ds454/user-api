package ds454.locomotive.userapi.util;

import java.util.function.Function;

import ds454.locomotive.generated.grpc.problem.Problem;
import ds454.locomotive.generated.grpc.problem.ProblemType;
import io.grpc.stub.StreamObserver;

/**
 * 
 * GrpcUtils contains utility methods for the User Service that handle gRPC related tasks.
 * 
 */
public class GrpcUtils {
    
    // Sends a single Problem and completes the stream. Every error path uses this.
    public static <T> void sendProblem(StreamObserver<T> observer, ProblemType type, String field,
            String message, Function<Problem, T> responseFactory) {
        Problem.Builder problem = Problem.newBuilder().setProblem(type).setMessage(message);
        if (field != null) {
            problem.setField(field);
        }
        observer.onNext(responseFactory.apply(problem.build()));
        observer.onCompleted();
    }
}
