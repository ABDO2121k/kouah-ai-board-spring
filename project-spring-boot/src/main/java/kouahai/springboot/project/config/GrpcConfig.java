package kouahai.springboot.project.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcConfig {

    @Value("${email.service.host:email-service}")
    private String emailServiceHost;

    @Value("${email.service.port:50051}")
    private int emailServicePort;

    private ManagedChannel channel;

    @Bean
    public ManagedChannel emailServiceChannel() {
        channel = ManagedChannelBuilder
                .forAddress(emailServiceHost, emailServicePort)
                .usePlaintext() // Use TLS in production
                // .useTransportSecurity()// hadi test TLS f prod khass tdar
                .build();
        return channel;
    }

    @PreDestroy
    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
        }
    }
}
