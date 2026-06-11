create table admin
(
    admin_id   int auto_increment
        primary key,
    full_name  varchar(100)                        not null,
    email      varchar(100)                        not null,
    password   varchar(100)                        not null,
    created_at timestamp default CURRENT_TIMESTAMP null
);

create table category
(
    category_id   int auto_increment
        primary key,
    category_name varchar(100) not null
);

create table customer
(
    customer_id int auto_increment
        primary key,
    full_name   varchar(100)                        not null,
    email       varchar(100)                        not null,
    phone       varchar(20)                         not null,
    address     text                                not null,
    created_at  timestamp default CURRENT_TIMESTAMP null,
    constraint email
        unique (email)
);

create table cart
(
    cart_id     int auto_increment
        primary key,
    customer_id int                                 not null,
    created_at  timestamp default CURRENT_TIMESTAMP null,
    constraint cart_ibfk_1
        foreign key (customer_id) references customer (customer_id)
            on delete cascade
);

create index customer_id
    on cart (customer_id);

create table flyway_schema_history
(
    installed_rank int                                 not null
        primary key,
    version        varchar(50)                         null,
    description    varchar(200)                        not null,
    type           varchar(20)                         not null,
    script         varchar(1000)                       not null,
    checksum       int                                 null,
    installed_by   varchar(100)                        not null,
    installed_on   timestamp default CURRENT_TIMESTAMP not null,
    execution_time int                                 not null,
    success        tinyint(1)                          not null
);

create index flyway_schema_history_s_idx
    on flyway_schema_history (success);

create table orders
(
    order_id     int auto_increment
        primary key,
    customer_id  int                                                                                          not null,
    order_date   timestamp                                                          default CURRENT_TIMESTAMP null,
    total_amount decimal(10, 2)                                                                               not null,
    order_status enum ('Pending', 'Processing', 'Shipped', 'Delivery', 'Cancelled') default 'Pending'         null,
    constraint orders_ibfk_1
        foreign key (customer_id) references customer (customer_id)
            on delete cascade
);

create table delivery
(
    delivery_id        int auto_increment
        primary key,
    order_id           int                                                               not null,
    delivery_person    varchar(100)                                                      not null,
    delivery_phone     varchar(20)                                                       not null,
    delivery_address   text                                                              not null,
    delivery_status    enum ('Preparing', 'On the way', 'Delivered') default 'Preparing' null,
    estimated_delivery datetime                                                          null,
    constraint delivery_ibfk_1
        foreign key (order_id) references orders (order_id)
            on delete cascade
);

create index order_id
    on delivery (order_id);

create index customerL_id
    on orders (customer_id);

create index customer_id
    on orders (customer_id);

create table payment
(
    payment_id     int auto_increment
        primary key,
    order_id       int                                                          not null,
    payment_method enum ('Cash', 'Credit Card', 'ABA', 'ACELEDA', 'Wing')       not null,
    payment_status enum ('Pending', 'Paid', 'Failed') default 'Pending'         null,
    payment_date   timestamp                          default CURRENT_TIMESTAMP null,
    constraint payment_ibfk_1
        foreign key (order_id) references orders (order_id)
            on delete cascade
);

create index order_id
    on payment (order_id);

create table products
(
    product_id     int auto_increment
        primary key,
    category_id    int                                 null,
    product_name   varchar(100)                        not null,
    description    text                                null,
    price          decimal(10, 2)                      not null,
    stock_quantity int       default 0                 null,
    image_url      varchar(255)                        null,
    created_at     timestamp default CURRENT_TIMESTAMP null,
    constraint products_ibfk_1
        foreign key (category_id) references category (category_id)
            on delete cascade
);

create table cart_item
(
    cart_item_id int auto_increment
        primary key,
    cart_id      int not null,
    product_id   int not null,
    quantity     int not null,
    constraint cart_item_ibfk_1
        foreign key (cart_id) references cart (cart_id)
            on delete cascade,
    constraint cart_item_ibfk_2
        foreign key (product_id) references products (product_id)
            on delete cascade
);

create table review
(
    review_id   int auto_increment
        primary key,
    customer_id int                                 not null,
    product_id  int                                 not null,
    rating      int                                 null,
    comment     text                                null,
    created_at  timestamp default CURRENT_TIMESTAMP null,
    constraint review_ibfk_1
        foreign key (customer_id) references customer (customer_id)
            on delete cascade,
    constraint review_ibfk_2
        foreign key (product_id) references products (product_id)
            on delete cascade,
    check (`rating` between 1 and 5)
);

