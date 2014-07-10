# --- !Ups

alter table creator drop column name;
alter table creator drop column field_affiliation;
alter table creator drop column url;
alter table creator drop column edit_url;
alter table creator drop column last_access;
alter table creator drop column last_login;
alter table creator drop column created;
alter table creator drop column status;
alter table creator drop column language;
alter table creator drop column feed_nid;
alter table creator drop column revision;

# --- !Downs

alter table creator add name varchar(255);
alter table creator add field_affiliation varchar(255);
alter table creator add url varchar(255);
alter table creator add edit_url varchar(255);
alter table creator add last_access varchar(255);
alter table creator add last_login varchar(255);
alter table creator add created varchar(255);
alter table creator add status bigint;
alter table creator add language varchar(255);
alter table creator add feed_nid bigint;
alter table creator add revision TEXT;
