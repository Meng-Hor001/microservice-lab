package kh.edu.istad.platform.customer.persistence.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor

public class CustomerEntity {

    private UUID customerId;
}
