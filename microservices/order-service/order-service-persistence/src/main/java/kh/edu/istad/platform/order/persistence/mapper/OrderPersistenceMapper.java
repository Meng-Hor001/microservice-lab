package kh.edu.istad.platform.order.persistence.mapper;

import kh.edu.istad.platform.order.domain.entity.Order;
import kh.edu.istad.platform.order.domain.entity.OrderItem;
import kh.edu.istad.platform.order.domain.entity.Product;
import kh.edu.istad.platform.order.domain.valueobject.*;
import kh.edu.istad.platform.order.persistence.entity.OrderEntity;
import kh.edu.istad.platform.order.persistence.entity.OrderItemEmbeddable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.UUID;

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
    @Mapping(target = "businessApprovalId", ignore = true)
    OrderEntity toEntity(Order order);

    @Mapping(target = "itemId", source = "orderItemId.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "price", source = "price.amount")
    @Mapping(target = "subTotal", source = "subTotal.amount")
    OrderItemEmbeddable toItemEntity(OrderItem item);

    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "customerId", source = "customerId")
    @Mapping(target = "businessId", source = "businessId")
    @Mapping(target = "streetAddress", source = ".")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "items",
            expression = "java(toItems(entity.getItems(), entity.getId()))")
    @Mapping(target = "trackingId", source = "trackingId")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "initCancel", ignore = true)
    @Mapping(target = "cancel", ignore = true)
    Order toDomain(OrderEntity entity);

    @Mapping(target = "orderItemId", source = "item.itemId")
    @Mapping(target = "orderId",
            expression = "java(new OrderId(orderId))")
    @Mapping(target = "product", source = "item")
    @Mapping(target = "price", source = "item.price")
    @Mapping(target = "subTotal", source = "item.subTotal")
    OrderItem toDomain(OrderItemEmbeddable item, UUID orderId);


    @Mapping(target = "name", source = "productName")
    @Mapping(target = "price", source = "price")
    Product toProduct(OrderItemEmbeddable item);

    default List<OrderItem> toItems(
            List<OrderItemEmbeddable> items,
            UUID orderId) {
        return items.stream()
                .map(item -> toDomain(item, orderId))
                .toList();
    }

    default StreetAddress toStreetAddress(OrderEntity entity) {
        return new StreetAddress(
                entity.getAddressId(),
                entity.getStreet(),
                entity.getPostalCode(),
                entity.getCity()
        );
    }

    default OrderItemId toOrderItemId(UUID id){
        return  new OrderItemId(id);
    }
    default OrderId toOrderId(UUID id) {
        return new OrderId(id);
    }

    default CustomerId toCustomerId(UUID id) {
        return new CustomerId(id);
    }

    default BusinessId toBusinessId(UUID id) {
        return new BusinessId(id);
    }

    default TrackingId toTrackingId(UUID id) {
        return new TrackingId(id);
    }

    default Money toMoney(java.math.BigDecimal amount) {
        return new Money(amount);
    }

    default OrderStatus toOrderStatus(String status) {
        return OrderStatus.valueOf(status);
    }

}
