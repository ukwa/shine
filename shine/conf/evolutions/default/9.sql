# --- !Ups

create sequence account_seq start with 1;

# --- !Downs

drop sequence if exists account_seq;