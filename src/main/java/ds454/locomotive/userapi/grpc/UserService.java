package ds454.locomotive.userapi.grpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ds454.locomotive.userapi.dbconnection.RepositoryProvider;
import ds454.locomotive.userapi.model.User;
import ds454.locomotive.userapi.repository.UserRepository;
import io.grpc.stub.StreamObserver;
import ds454.locomotive.generated.grpc.UserServiceGrpc.UserServiceImplBase;

public class UserService extends UserServiceImplBase {
    
}
