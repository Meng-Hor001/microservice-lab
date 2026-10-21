package kh.edu.istad.platform.customer.restapi.mapper;

import kh.edu.istad.platform.customer.domain.entity.CustomerSegment;
import kh.edu.istad.platform.customer.domain.valueobject.CustomerStatus;
import kh.edu.istad.platform.customer.domain.valueobject.SegmentName;
import kh.edu.istad.platform.customer.restapi.dto.CreateCustomerRequest;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class WebMapperTest {
    @Test
    void customerMappingPreservesValueObjectsSegmentAndStatus() {
        var mapper = Mappers.getMapper(CustomerWebMapper.class);
        var customer = mapper.toDomain(new CreateCustomerRequest(
                "alice", "Smith", "Alice", "alice@example.com", "012345678", "gold"));
        customer.initiateCustomer();
        var response = mapper.toResponse(customer);
        assertThat(response.customerId()).isEqualTo(customer.getId().id());
        assertThat(response.email()).isEqualTo("alice@example.com");
        assertThat(response.phoneNumber()).isEqualTo("012345678");
        assertThat(response.segmentId()).isEqualTo("gold");
        assertThat(response.status()).isEqualTo(CustomerStatus.ACTIVE);
        customer.deactivateCustomer();
        assertThat(mapper.toResponse(customer).status()).isEqualTo(CustomerStatus.INACTIVE);
    }

    @Test
    void segmentMappingPreservesNormalizedDescription() {
        var response = Mappers.getMapper(SegmentWebMapper.class)
                .toResponse(new CustomerSegment("gold", SegmentName.GOLD, " Loyal customers "));
        assertThat(response.id()).isEqualTo("gold");
        assertThat(response.name()).isEqualTo(SegmentName.GOLD);
        assertThat(response.description()).isEqualTo("Loyal customers");
    }
}
