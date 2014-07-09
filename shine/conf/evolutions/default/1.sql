# --- !Ups

create table permission (
  id                        bigint not null,
  name                      TEXT,
  url                       TEXT,
  description               TEXT,
  revision                  TEXT,
  last_update               timestamp not null,
  constraint pk_permission primary key (id))
;

# --- !Downs

drop table if exists permission;
