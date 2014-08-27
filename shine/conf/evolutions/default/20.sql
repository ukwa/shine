# --- !Ups

alter table resource drop constraint pk_resource; 
alter table resource add constraint pk_resource primary key(id);

# --- !Downs

alter table resource drop constraint pk_resource; 
alter table resource add constraint pk_resource primary key(resource_id);
