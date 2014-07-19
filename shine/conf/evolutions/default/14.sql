# --- !Ups

alter table saved_search add summary TEXT;

# --- !Downs

alter table saved_search drop column summary;