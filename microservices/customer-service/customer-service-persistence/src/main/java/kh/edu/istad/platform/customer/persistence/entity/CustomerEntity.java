package kh.edu.istad.platform.customer.persistence.entity;

import jakarta.persistence.*;
import kh.edu.istad.platform.customer.domain.valueobject.CustomerStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "customers")
public class CustomerEntity {

    @Id
    private UUID customerId;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String familyName;

    @Column(nullable = false)
    private String givenName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CustomerStatus status;
}
