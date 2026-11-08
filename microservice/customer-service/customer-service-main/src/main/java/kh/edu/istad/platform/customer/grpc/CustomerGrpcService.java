package kh.edu.istad.platform.customer.grpc;

import org.springframework.grpc.server.service.GrpcService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import build.buf.gen.customer.v1.CustomerServiceGrpc;
import build.buf.gen.customer.v1.CustomerStatus;
import build.buf.gen.customer.v1.GetCustomerRequest;
import build.buf.gen.customer.v1.GetCustomerResponse;
import build.buf.gen.customer.v1.GetCustomerByUsernameRequest;
import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.usecase.QueryCustomerUseCase;
import kh.edu.istad.common.domain.valueobject.CustomerId;
import reactor.core.publisher.Mono;

import java.util.UUID;

@GrpcService
public class CustomerGrpcService extends CustomerServiceGrpc.CustomerServiceImplBase {

    private final QueryCustomerUseCase queryCustomerUseCase;

    public CustomerGrpcService(QueryCustomerUseCase queryCustomerUseCase) {
        this.queryCustomerUseCase = queryCustomerUseCase;
    }

    @Override
    public void getCustomer(
            GetCustomerRequest request,
            StreamObserver<GetCustomerResponse> responseObserver
    ) {
        final CustomerId customerId;
        try {
            customerId = new CustomerId(UUID.fromString(request.getUsername()));
        } catch (IllegalArgumentException exception) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("customer_id must be a valid UUID")
                    .asRuntimeException());
            return;
        }

        Mono<Customer> result = queryCustomerUseCase.findById(customerId);
        result.subscribe(
                customer -> {
                    GetCustomerResponse response = GetCustomerResponse.newBuilder()
                            .setCustomerId(customerId.value().toString())
                            .setUsername(valueOrEmpty(customer.getUsername()))
                            .setGivenName(valueOrEmpty(customer.getGivenName()))
                            .setFamilyName(valueOrEmpty(customer.getFamilyName()))
                            .setEmail(customer.getEmail() == null
                                    ? "" : valueOrEmpty(customer.getEmail().value()))
                            .setPhoneNumber(customer.getPhoneNumber() == null
                                    ? "" : valueOrEmpty(customer.getPhoneNumber().value()))
                            .setStatus(toGrpcStatus(customer))
                            .build();
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                },
                error -> responseObserver.onError(Status.INTERNAL
                        .withDescription("Failed to look up customer")
                        .withCause(error)
                        .asRuntimeException()),
                () -> responseObserver.onError(Status.NOT_FOUND
                        .withDescription("Customer not found")
                        .asRuntimeException())
        );
    }

    @Override
    public void getCustomerByUsername(
            GetCustomerByUsernameRequest request,
            StreamObserver<GetCustomerResponse> responseObserver
    ) {
        queryCustomerUseCase.findByUsername(request.getUsername()).subscribe(
                customer -> {
                    GetCustomerResponse response = GetCustomerResponse.newBuilder()
                            .setCustomerId(customer.getId().value().toString())
                            .setUsername(valueOrEmpty(customer.getUsername()))
                            .setGivenName(valueOrEmpty(customer.getGivenName()))
                            .setFamilyName(valueOrEmpty(customer.getFamilyName()))
                            .setEmail(customer.getEmail() == null
                                    ? "" : valueOrEmpty(customer.getEmail().value()))
                            .setPhoneNumber(customer.getPhoneNumber() == null
                                    ? "" : valueOrEmpty(customer.getPhoneNumber().value()))
                            .setStatus(toGrpcStatus(customer))
                            .build();
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                },
                error -> responseObserver.onError(Status.INTERNAL
                        .withDescription("Failed to look up customer by username")
                        .withCause(error)
                        .asRuntimeException()),
                () -> responseObserver.onError(Status.NOT_FOUND
                        .withDescription("Customer not found")
                        .asRuntimeException())
        );
    }

    private static CustomerStatus toGrpcStatus(Customer customer) {
        if (customer.getStatus() == null) {
            return CustomerStatus.CUSTOMER_STATUS_UNSPECIFIED;
        }
        return switch (customer.getStatus()) {
            case ACTIVE -> CustomerStatus.CUSTOMER_STATUS_ACTIVE;
            case INACTIVE -> CustomerStatus.CUSTOMER_STATUS_INACTIVE;
        };
    }

    private static String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
