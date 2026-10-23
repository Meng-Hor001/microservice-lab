package kh.edu.istad.platform.order.domain.entity;

import kh.edu.istad.common.domain.entity.AggregateRoot;
import kh.edu.istad.platform.order.domain.valueobject.BusinessId;

import java.util.List;

public class Business extends AggregateRoot<BusinessId> {
    private final List<Product> products;
    private Boolean active;

    public Business(List<Product> products, Boolean active) {
        this.products = products;
        this.active = active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getActive() {
        return active;
    }

    public List<Product> getProducts() {
        return products;
    }

    private Business(Builder builder) {
        super.setId(builder.id);
        products = builder.products;
        active = builder.active;
    }


    public static final class Builder {
        private BusinessId id;
        private List<Product> products;
        private Boolean active;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder id(BusinessId val) {
            id = val;
            return this;
        }

        public Builder products(List<Product> val) {
            products = val;
            return this;
        }

        public Builder active(Boolean val) {
            active = val;
            return this;
        }

        public Business build() {
            return new Business(this);
        }
    }
}
