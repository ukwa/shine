# --- !Ups

create table role_permissions (
  role_id                   	bigint not null,
  permission_id             	bigint not null,
  last_update               	timestamp not null,
  constraint ck_role_permissions primary key (role_id, permission_id),
  foreign key(role_id)   		references role(id) on delete cascade on update restrict,
  foreign key(permission_id)	references permission(id) on delete cascade on update restrict
);

create table user_roles (
  user_id                   	bigint not null,
  role_id             			bigint not null,
  last_update               	timestamp not null,
  constraint ck_user_roles primary key (user_id, role_id),
  foreign key(user_id)			references account(id) on delete cascade on update restrict,
  foreign key(role_id)   		references role(id) on delete cascade on update restrict
);

# --- !Downs

drop table if exists role_permissions;
drop table if exists user_roles;
