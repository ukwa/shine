# --- !Ups

create sequence corpus_seq start with 1;

# --- !Downs

drop sequence if exists corpus_seq;