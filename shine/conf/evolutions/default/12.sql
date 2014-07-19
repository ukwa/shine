# --- !Ups

alter table saved_search add description TEXT;

# --- !Downs

alter table saved_search drop column description;

