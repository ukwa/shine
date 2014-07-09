# --- !Ups

create sequence saved_search_seq start with 1;

# --- !Downs

drop sequence if exists saved_search_seq;