# --- !Ups

create table saved_search (
  id			bigint not null,
  name  		varchar(255),
  url			TEXT,
  last_update   timestamp not null,
  user_id 		bigint NOT NULL,
  FOREIGN KEY (user_id) REFERENCES creator(uid),
  constraint pk_saved_search primary key (id))
;

# --- !Downs

drop table if exists saved_search;
