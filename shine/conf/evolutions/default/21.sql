# --- !Ups

alter table resource add title text;
alter table resource add url text;

# --- !Downs

alter table resource drop column title; 
alter table resource drop column url; 
