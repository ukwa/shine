# --- !Ups

alter table role drop column permissions;
alter table creator drop column roles;

# --- !Downs

alter table role add permissions TEXT;
alter table creator add roles TEXT;