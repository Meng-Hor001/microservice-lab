package kh.edu.istad.platform.order.persistence.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Table("businesses")
@ToString
public class BusinessEntity {

    @Id
    @Embedded.Nullable
    private BusinessEntityId businessEntityId;

    @Column("business_active")
    private Boolean businessActive;
    @Column("product_name")
    private String productName;
    @Column("product_price")
    private BigDecimal productPrice;
}
