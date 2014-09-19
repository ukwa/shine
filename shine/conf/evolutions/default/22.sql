# --- !Ups

alter table resource add waybackDate timestamp;

# --- !Downs

alter table resource drop column waybackDate; 
