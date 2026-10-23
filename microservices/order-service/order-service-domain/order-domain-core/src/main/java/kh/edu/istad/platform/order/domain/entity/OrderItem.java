package kh.edu.istad.platform.order.domain.entity;

import kh.edu.istad.common.domain.entity.BaseEntity;
import kh.edu.istad.common.domain.valueobject.Money;
import kh.edu.istad.platform.order.domain.valueobject.OrderId;
import kh.edu.istad.platform.order.domain.valueobject.OrderItemId;


import kh.edu.istad.common.domain.valueobject.ProductId;

public class OrderItem extends BaseEntity<OrderItemId> {
    private OrderId orderId;
    private final ProductId productId;
    private final Integer quantity;
    private final Money price;
    private final Money subTotal;


    public OrderItem(ProductId productId, Integer quantity, Money price, Money subTotal, OrderId orderId) {
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
        this.subTotal = subTotal;
        this.orderId = orderId;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public ProductId getProductId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Money getPrice() {
        return price;
    }

    public Money getSubTotal() {
        return subTotal;
    }

    private OrderItem(Builder builder) {
        super.setId(builder.id);
        orderId = builder.orderId;
        productId = builder.productId;
        quantity = builder.quantity;
        price = builder.price;
        subTotal = builder.subTotal;
    }


    public static final class Builder {
        private OrderItemId id;
        private OrderId orderId;
        private ProductId productId;
        private Integer quantity;
        private Money price;
        private Money subTotal;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder id(OrderItemId val) {
            id = val;
            return this;
        }

        public Builder orderId(OrderId val) {
            orderId = val;
            return this;
        }

        public Builder productId(ProductId val) {
            productId = val;
            return this;
        }

        public Builder quantity(Integer val) {
            quantity = val;
            return this;
        }

        public Builder price(Money val) {
            price = val;
            return this;
        }

        public Builder subTotal(Money val) {
            subTotal = val;
            return this;
        }

        public OrderItem build() {
            return new OrderItem(this);
        }
    }
}

