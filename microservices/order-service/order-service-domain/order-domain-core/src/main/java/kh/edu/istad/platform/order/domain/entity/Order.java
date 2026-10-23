package kh.edu.istad.platform.order.domain.entity;

import kh.edu.istad.common.domain.entity.AggregateRoot;
import kh.edu.istad.common.domain.valueobject.Money;
import kh.edu.istad.common.domain.valueobject.OrderStatus;
import kh.edu.istad.common.domain.valueobject.StreetAddress;
import kh.edu.istad.platform.order.domain.exception.OrderDomainException;
import kh.edu.istad.platform.order.domain.valueobject.BusinessId;
import kh.edu.istad.common.domain.valueobject.CustomerId;
import kh.edu.istad.platform.order.domain.valueobject.OrderId;
import kh.edu.istad.platform.order.domain.valueobject.TrackingId;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Order extends AggregateRoot<OrderId> {
    private final OrderId orderId;
    private final CustomerId customerId;
    private final BusinessId businessId;
    private final StreetAddress deliveryAddress;
    private final Money price;
    private final List<OrderItem> items;
    private  TrackingId trackingId;
    private OrderStatus orderStatus;
    private List<String> failureMessages;
    private Long version;
//---------start domain logic ----------//
    /**
     * Initialize order
     * */
    public void initializeOrder(){
        if (super.getId() != null || orderStatus != null) {
            throw new OrderDomainException("Order has already initialized");
        }
        super.setId(new OrderId(UUID.randomUUID()));
        trackingId = new TrackingId(UUID.randomUUID());
        orderStatus = OrderStatus.PENDING;
        validateOrder();
    }

    public void pay(){
        if (orderStatus != orderStatus.PENDING){
            throw new OrderDomainException("Order state is not correct for pay operation");
        }

        orderStatus = OrderStatus.PAID;
    }

    public void approved(){
        if (orderStatus != orderStatus.PAID){
            throw new OrderDomainException("Order state is not correct for approved operation");
        }
        orderStatus = OrderStatus.APPROVED;
    }

    public void initCancel (List<String> failureMessages){
        if (orderStatus != orderStatus.PAID){
            throw new OrderDomainException("Order state is not correct for cancel operation");
        }
        orderStatus = OrderStatus.CANCELLED;
        updateFailureMessage(failureMessages);
    }

    public void cancel (){
        if (!(orderStatus == orderStatus.PENDING || orderStatus == orderStatus.CANCELLING)){
            throw new OrderDomainException("Order state is not correct for cancel operation");
        }
        orderStatus = OrderStatus.CANCELLED;
    }
    public void updateFailureMessage(List<String> failureMessages){
        if (failureMessages != null){
            this.failureMessages.addAll(failureMessages.stream().filter(
                    message -> !message.isBlank()
            ).toList());
        }else {
            this.failureMessages = new ArrayList<>();
        }
    }

    public void validateOrder() {
        // validate order price
        if (price == null || price.isGreaterThanZero()) {
            throw new OrderDomainException("Price must be greater than zero");
        }


        // validate product price
        items.stream()
                .map(orderItem -> {
                    validateItemPrice(orderItem);
                    return orderItem.getSubTotal();
                })
                .reduce(Money.ZERO, Money::add);

        if (!price.equals(orderSubTotal)){
            throw new OrderDomainException("price must be equal to product");
        }
    }
    private void validateItemPrice(){

    }
    public void validateItemPrice(OrderItem orderItem){
        if (!orderItem.getPrice().equals(orderItem.getProductId().getPrice())){
            throw new OrderDomainException("price must be equal to product");
        }
    }
//-------- Close domain logic ----------//
    public Order(OrderId orderId, CustomerId customerId, BusinessId businessId, StreetAddress deliveryAddress, Money price, List<OrderItem> items, TrackingId trackingId, OrderStatus orderStatus, List<String> failureMessages, Long version) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.businessId = businessId;
        this.deliveryAddress = deliveryAddress;
        this.price = price;
        this.items = items;
        this.trackingId = trackingId;
        this.orderStatus = orderStatus;
        this.failureMessages = failureMessages;
        this.version = version;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public BusinessId getBusinessId() {
        return businessId;
    }

    public StreetAddress getDeliveryAddress() {
        return deliveryAddress;
    }

    public Money getPrice() {
        return price;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public TrackingId getTrackingId() {
        return trackingId;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public List<String> getFailureMessages() {
        return failureMessages;
    }

    public Long getVersion() {
        return version;
    }

    private Order(Builder builder) {
        super.setId(builder.id);
        orderId = builder.orderId;
        customerId = builder.customerId;
        businessId = builder.businessId;
        deliveryAddress = builder.deliveryAddress;
        price = builder.price;
        items = builder.items;
        trackingId = builder.trackingId;
        orderStatus = builder.orderStatus;
        failureMessages = builder.failureMessages;
        version = builder.version;
    }


    public static final class Builder {
        private OrderId id;
        private OrderId orderId;
        private CustomerId customerId;
        private BusinessId businessId;
        private StreetAddress deliveryAddress;
        private Money price;
        private List<OrderItem> items;
        private TrackingId trackingId;
        private OrderStatus orderStatus;
        private List<String> failureMessages;
        private Long version;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder id(OrderId val) {
            id = val;
            return this;
        }

        public Builder orderId(OrderId val) {
            orderId = val;
            return this;
        }

        public Builder customerId(CustomerId val) {
            customerId = val;
            return this;
        }

        public Builder businessId(BusinessId val) {
            businessId = val;
            return this;
        }

        public Builder deliveryAddress(StreetAddress val) {
            deliveryAddress = val;
            return this;
        }

        public Builder price(Money val) {
            price = val;
            return this;
        }

        public Builder items(List<OrderItem> val) {
            items = val;
            return this;
        }

        public Builder trackingId(TrackingId val) {
            trackingId = val;
            return this;
        }

        public Builder orderStatus(OrderStatus val) {
            orderStatus = val;
            return this;
        }

        public Builder failureMessages(List<String> val) {
            failureMessages = val;
            return this;
        }

        public Builder version(Long val) {
            version = val;
            return this;
        }

        public Order build() {
            return new Order(this);
        }
    }
}
