# --- !Ups

create table corpus (
  id			bigint not null,
  name  		varchar(255),
  metaData		TEXT,
  last_update   timestamp not null,
  user_id 		bigint NOT NULL,
  FOREIGN KEY (user_id) REFERENCES account(id),
  constraint pk_corpus primary key (id))
;

create table resource (
  resource_id	bigint not null,
  corpus_id 	bigint NOT NULL,
  last_update   timestamp not null,
  FOREIGN KEY (corpus_id) REFERENCES corpus(id),
  constraint pk_resource primary key (resource_id))
;

# --- !Downs

drop table if exists corpus;
drop table if exists resource;

    
