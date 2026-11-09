create table businesses
(
    business_id     uuid not null,
    product_id      uuid not null,
    business_active boolean,
    product_name    varchar(255),
    product_price   numeric(38, 2),
    primary key (business_id, product_id)
);

create table customers
(
    id          uuid not null
        primary key,
    family_name varchar(255),
    given_name  varchar(255),
    username    varchar(255)
);

create table order_addresses
(
    id          uuid not null
        primary key,
    city        varchar(255),
    postal_code varchar(255),
    street      varchar(255)
);

create table orders
(
    id               uuid not null
        primary key,
    business_id      uuid,
    customer_id      uuid,
    failure_messages varchar(255),
    order_status     varchar(255)
        constraint orders_order_status_check
            check ((order_status)::text = ANY
                   ((ARRAY ['PENDING'::character varying, 'PAID'::character varying, 'APPROVED'::character varying, 'CANCELLING'::character varying, 'CANCELLED'::character varying])::text[])),
    price            numeric(38, 2),
    tracking_id      uuid,
    order_address_id uuid
        constraint uklkaucp7geo007n2fyvif8dxo6
            unique
        constraint fkx1t8eioxmuh2e77oqahf2ctn
            references order_addresses
);

create table order_items
(
    id         integer not null,
    price      numeric(38, 2),
    product_id uuid,
    quantity   integer,
    sub_total  numeric(38, 2),
    order_id   uuid    not null
        constraint fkbioxgbv59vetrxe0ejfubep1w
            references orders,
    primary key (id, order_id)
);

