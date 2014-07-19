# --- !Ups

create sequence permission_seq start with 1;
create sequence role_seq start with 1;

# --- !Downs

drop sequence if exists permission_seq;
drop sequence if exists role_seq;
