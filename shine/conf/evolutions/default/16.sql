# --- !Ups

alter table corpus add description TEXT;
alter table corpus add tags TEXT;
alter table corpus add justification TEXT;

# --- !Downs

alter table corpus drop column summary;
alter table corpus drop column tags;
alter table corpus drop column justification;
