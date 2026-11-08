package kh.edu.istad.platform.order.client.config;

import build.buf.gen.customer.v1.CustomerServiceGrpc;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.ImportGrpcClients;

@Configuration(proxyBeanMethods = false)
@ImportGrpcClients(
        target = "customer-service",
        types = CustomerServiceGrpc.CustomerServiceBlockingStub.class
)
public class GrpcClientConfig {
}
