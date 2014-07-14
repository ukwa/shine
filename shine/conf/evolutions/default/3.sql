# --- !Ups

create table account (
  id                       	bigint not null,
  email                     varchar(255),
  name                      varchar(255),
  password                  varchar(255),
  field_affiliation         varchar(255),
  url                       varchar(255),
  edit_url                  varchar(255),
  last_access               varchar(255),
  last_login                varchar(255),
  created                   varchar(255),
  status                    bigint,
  language                  varchar(255),
  feed_nid                  bigint,
  roles                     TEXT,
  revision                  TEXT,
  last_update               timestamp not null,
  constraint pk_account primary key (id))
;

# --- !Downs

drop table if exists account;