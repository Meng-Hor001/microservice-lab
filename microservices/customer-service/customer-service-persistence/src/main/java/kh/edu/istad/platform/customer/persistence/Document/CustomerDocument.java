package kh.edu.istad.platform.customer.persistence.Document;


import kh.edu.istad.common.domain.valueobject.CustomerId;
import kh.edu.istad.platform.customer.domain.valueobject.CustomerStatus;
import kh.edu.istad.platform.customer.domain.valueobject.Email;
import kh.edu.istad.platform.customer.domain.valueobject.PhoneNumber;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document
public class CustomerDocument {
    @Id
    private String id;

    private  String username;

    private String familyName;

    private String givenName;

    private  Email email;

    private  PhoneNumber phoneNumber;

    private CustomerStatus status;

    private String segmentId;

}
