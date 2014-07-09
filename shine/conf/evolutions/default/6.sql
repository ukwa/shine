# --- !Ups

create table role_permissions (
  role_id                   	bigint not null,
  permission_id             	bigint not null,
  last_update               	timestamp not null,
  foreign key(role_id)   		references role(id) on delete cascade,
  foreign key(permission_id)	references permission(id) on delete cascade
);

create table user_roles (
  user_id                   	bigint not null,
  role_id             			bigint not null,
  last_update               	timestamp not null,
  foreign key(user_id)			references creator(uid) on delete cascade,
  foreign key(role_id)   		references role(id) on delete cascade
);

# --- !Downs

drop table if exists role_permissions;
drop table if exists user_roles;
