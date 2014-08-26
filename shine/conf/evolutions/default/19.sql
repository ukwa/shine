# --- !Ups

create sequence resource_seq start with 1;

# --- !Downs

drop sequence if exists resource_seq;