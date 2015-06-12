# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table image (
  id                        bigint auto_increment not null,
  name                      varchar(255),
  data                      longblob,
  constraint pk_image primary key (id))
;




# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table image;

SET FOREIGN_KEY_CHECKS=1;

