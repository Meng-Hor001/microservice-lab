package kh.edu.istad.platform.business.persistence.mapper;

import java.util.UUID;
import kh.edu.istad.platform.business.domain.entity.Business;
import kh.edu.istad.platform.business.domain.entity.Product;
import kh.edu.istad.platform.business.domain.valueobject.BusinessId;
import kh.edu.istad.platform.business.domain.valueobject.BusinessName;
import kh.edu.istad.platform.business.domain.valueobject.BusinessContact;
import kh.edu.istad.platform.business.domain.valueobject.ProductId;
import kh.edu.istad.platform.business.domain.valueobject.Money;
import kh.edu.istad.platform.business.persistence.document.BusinessDocument;
import kh.edu.istad.platform.business.persistence.document.ProductDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface BusinessPersistenceMapper {
    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "legalName", source = "name.legalName")
    @Mapping(target = "displayName", source = "name.displayName")
    @Mapping(target = "email", source = "contact.email")
    @Mapping(target = "phoneNumber", source = "contact.phoneNumber")
    BusinessDocument toDocument(Business business);

    @Mapping(target = "id", source = "productId.value")
    @Mapping(target = "price", source = "price.amount")
    ProductDocument toDocument(Product product);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "document")
    @Mapping(target = "contact", source = "document")
    Business toDomain(BusinessDocument document);

    @Mapping(target = "productId", source = "id")
    @Mapping(target = "price", source = "price")
    Product toDomain(ProductDocument document);

    default BusinessId toBusinessId(String id) {
        return new BusinessId(UUID.fromString(id));
    }

    default ProductId toProductId(String id) {
        return new ProductId(UUID.fromString(id));
    }

    default Money toMoney(java.math.BigDecimal amount) {
        return new Money(amount);
    }

    default BusinessName toName(BusinessDocument document) {
        return new BusinessName(document.legalName(), document.displayName());
    }

    default BusinessContact toContact(BusinessDocument document) {
        return new BusinessContact(document.email(), document.phoneNumber());
    }
}
