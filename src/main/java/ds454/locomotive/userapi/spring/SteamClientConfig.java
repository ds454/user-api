package ds454.locomotive.userapi.spring;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ds454.locomotive.generated.grpc.SteamServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

@Configuration
public class SteamClientConfig {

    @Bean(destroyMethod = "shutdown")
    public ManagedChannel steamChannel(
            @Value("${steam-service.host:localhost}") String host,
            @Value("${steam-service.port:50051}") int port) {
        return ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
    }

    @Bean
    public SteamServiceGrpc.SteamServiceBlockingStub steamStub(ManagedChannel steamChannel) {
        return SteamServiceGrpc.newBlockingStub(steamChannel);
    }
}