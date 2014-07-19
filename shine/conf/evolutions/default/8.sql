# --- !Ups

alter table account drop column name;
alter table account drop column field_affiliation;
alter table account drop column url;
alter table account drop column edit_url;
alter table account drop column last_access;
alter table account drop column last_login;
alter table account drop column created;
alter table account drop column status;
alter table account drop column language;
alter table account drop column feed_nid;
alter table account drop column revision;

# --- !Downs

alter table account add name varchar(255);
alter table account add field_affiliation varchar(255);
alter table account add url varchar(255);
alter table account add edit_url varchar(255);
alter table account add last_access varchar(255);
alter table account add last_login varchar(255);
alter table account add created varchar(255);
alter table account add status bigint;
alter table account add language varchar(255);
alter table account add feed_nid bigint;
alter table account add revision TEXT;
