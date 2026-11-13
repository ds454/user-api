package ds454.locomotive.userapi;

import ds454.locomotive.userapi.grpc.HeaderInterceptor;
import ds454.locomotive.userapi.grpc.InternalUserService;
import ds454.locomotive.userapi.grpc.UserService;
import ds454.locomotive.userapi.grpc.AddUserService;
import ds454.locomotive.userapi.spring.BeanProvider;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    // Should be set with env variable GRPC_PORT
    private static int grpcPort = Integer.valueOf(System.getenv().get("GRPC_PORT"));

    public static void main(String[] args) throws Exception {
        log.info("Starting User API Application...");

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("ds454.locomotive.userapi");
        BeanProvider.setContext(context);

        log.info("Starting gRPC server on port " + Main.grpcPort);
        Server s;

        // Calls that should be authenticated need to be intercepted by the HeaderInterceptor.
        // The HeaderInterceptor will let the gRPC handler get the user uuid and steamid from the
        // request metadata.

        // Not all calls should be authenticated, for example logging in should not be 
        // authenticated, and internal verification gRPC calls should not be authenticated.
        // Private gRPC services need to be implemented in a seperate class and added seperately
        // to the server below.

        // Add and build the service.
        UserService userService = context.getBean(UserService.class);
        s = ServerBuilder.forPort(Main.grpcPort)
                .addService(ServerInterceptors.intercept(userService, new HeaderInterceptor()))
                .addService(new InternalUserService())
                .addService(new AddUserService())
                .build()
                .start();
        log.info("gRPC server started.");

        s.awaitTermination();
    }
}