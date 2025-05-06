create table if not exists ecommerce.invoice
(
    delivery_id          varchar(255) not null primary key,
    invoice_id           varchar(255)
);