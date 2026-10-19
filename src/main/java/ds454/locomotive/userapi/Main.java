package ds454.locomotive.userapi;

import ds454.locomotive.userapi.dbconnection.RepositoryProvider;
import ds454.locomotive.userapi.grpc.HelloService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    // Should be set with env variable GRPC_PORT
    private static int grpcPort = 8081;

    public static void main(String[] args) throws Exception {
        log.info("Starting User API Application...");

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("ds454.locomotive.userapi");
        RepositoryProvider.setContext(context);

        log.info("Starting gRPC server on port " + Main.grpcPort);
        Server s;
        s = ServerBuilder.forPort(Main.grpcPort)
                .addService(new HelloService())
                .build()
                .start();
        log.info("gRPC server started.");

        s.awaitTermination();
    }
}