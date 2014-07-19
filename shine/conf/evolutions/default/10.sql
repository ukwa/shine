# --- !Ups

alter table role drop column url;
alter table role drop column revision;

alter table permission drop column url;
alter table permission drop column revision;

# --- !Downs

alter table role add url TEXT;
alter table role add revision TEXT;

alter table permission add url TEXT;
alter table permission add revision TEXT;

