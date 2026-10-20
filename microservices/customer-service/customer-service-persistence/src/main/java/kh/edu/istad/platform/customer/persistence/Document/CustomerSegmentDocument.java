package kh.edu.istad.platform.customer.persistence.Document;

import kh.edu.istad.platform.customer.domain.valueobject.SegmentName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "customer_segments")
public class CustomerSegmentDocument {
    @Id
    private String id;

     private SegmentName name;

     private String description;
}
