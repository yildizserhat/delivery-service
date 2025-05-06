CREATE SCHEMA IF NOT EXISTS ecommerce;

CREATE TABLE IF NOT EXISTS ecommerce.delivery
(
    id          varchar(255) not null primary key,
    address     varchar(255),
    finished_at timestamp(6) with time zone,
    started_at  timestamp(6) with time zone,
    status      varchar(255),
    vehicle_id  varchar(255),
    invoice_id  varchar(255),
        constraint delivery_status_check
            check ((status)::text = ANY
        ((ARRAY ['IN_PROGRESS':: character varying, 'DELIVERED':: character varying])::text[])));



