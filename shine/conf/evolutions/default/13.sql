# --- !Ups

alter table role_permissions drop column last_update;
alter table user_roles drop column last_update;

# --- !Downs

alter table role_permissions add last_update timestamp not null;
alter table user_roles add last_update timestamp not null;
