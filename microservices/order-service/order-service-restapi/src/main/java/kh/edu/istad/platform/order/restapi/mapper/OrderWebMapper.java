package kh.edu.istad.platform.order.restapi.mapper;

import kh.edu.istad.platform.order.application.dto.CreateOrderCommand;
import kh.edu.istad.platform.order.application.dto.Item;
import kh.edu.istad.platform.order.restapi.dto.CreateOrderRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface OrderWebMapper {
    @Mapping(target = "street", source = "deliveryAddress.street")
    @Mapping(target = "postalCode", source = "deliveryAddress.postalCode")
    @Mapping(target = "city", source = "deliveryAddress.city")
    CreateOrderCommand toCommand(CreateOrderRequest request);

    Item toItem(CreateOrderRequest.Item item);
}
