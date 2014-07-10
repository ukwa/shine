# --- !Ups

create sequence creator_seq start with 1;

# --- !Downs

drop sequence if exists creator_seq;