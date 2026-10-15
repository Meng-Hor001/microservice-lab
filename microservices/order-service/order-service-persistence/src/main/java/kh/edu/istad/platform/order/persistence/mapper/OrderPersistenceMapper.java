package kh.edu.istad.platform.order.persistence.mapper;

import kh.edu.istad.platform.order.domain.entity.Order;
import kh.edu.istad.platform.order.domain.entity.OrderItem;
import kh.edu.istad.platform.order.persistence.entity.OrderEntity;
import kh.edu.istad.platform.order.persistence.entity.OrderItemEmbeddable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface OrderPersistenceMapper {
    @Mapping(target = "id", source = "orderId.id")
    @Mapping(target = "customerId", source = "customerId.id")
    @Mapping(target = "businessId", source = "businessId.id")
    @Mapping(target = "trackingId", source = "trackingId.id")
    @Mapping(target = "addressId", source = "streetAddress.id")
    @Mapping(target = "street", source = "streetAddress.street")
    @Mapping(target = "postalCode", source = "streetAddress.postalCode")
    @Mapping(target = "city", source = "streetAddress.city")
    @Mapping(target = "price", source = "price.amount")
    OrderEntity toEntity(Order order);

    @Mapping(target = "itemId", source = "orderItemId.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "price", source = "price.amount")
    @Mapping(target = "subTotal", source = "subTotal.amount")
    OrderItemEmbeddable toItemEntity(OrderItem item);
}
