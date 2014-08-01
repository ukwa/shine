# --- !Ups

alter table resource alter column resource_id type varchar(255);
alter table resource add id bigint not null;

# --- !Downs

alter table resource alter column resource_id type bigint;
alter table resource drop column id;
