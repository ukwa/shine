# --- !Ups

alter table role drop column permissions;
alter table account drop column roles;

# --- !Downs

alter table role add permissions TEXT;
alter table account add roles TEXT;