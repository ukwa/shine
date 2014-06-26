# --- !Ups

create table account (
  email                     varchar(255) not null primary key,
  name                      varchar(255) not null,
  password                  varchar(255) not null
);

# --- !Downs

drop table if exists account;
