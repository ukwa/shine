# --- !Ups

create table role (
  id                        bigint not null,
  name                      TEXT,
  url                       TEXT,
  permissions               TEXT,
  description               TEXT,
  revision                  TEXT,
  last_update               timestamp not null,
  constraint pk_role primary key (id))
;

# --- !Downs

drop table if exists role;
