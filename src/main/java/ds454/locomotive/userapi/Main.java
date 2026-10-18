package ds454.locomotive.userapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ds454.locomotive.generated.grpc.Problem;
import ds454.locomotive.userapi.util.Result;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        // Instead of using System.out.println, use log.
        // log prints the time, location, and severity to the console in color, making debugging easier and cooler.
        log.info("Hello, World!");

        // This is a Result<Success, Failure>
        Result<String, Problem> result;

        result = new Result.Success<>("Success!");
        // result = new Result.Failure<>(Problem.newBuilder().setProblem(ProblemType.ServerError).build());

        // When there is a problem processing a gRPC request, you need to return a problem instead of throwing an exception.
        // Results let us handle and track problems at different places in our code. To handle both cases quickly, we can use pattern matching like this:
        switch (result) {
			case Result.Success(String value) -> {
                // String interpolation
                log.info("{}", value);
            }
			case Result.Failure(Problem error) -> {
                log.error("Ran into a problem: {}", error.getProblem().toString());
            }
        }
    }
}
