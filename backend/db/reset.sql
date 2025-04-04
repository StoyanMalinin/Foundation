SET search_path TO foundation, public;

DROP SCHEMA IF EXISTS foundation CASCADE;
CREATE SCHEMA foundation;

create table foundation.presences
(
    id        serial
        constraint presences_pk
            primary key,
    point     geometry(Point),
    timestamp integer default 0 not null
);

alter table foundation.presences
    owner to postgres;

create table foundation.searches
(
    id          serial
        constraint searches_pk
            primary key,
    title       varchar(255) default NULL::character varying not null,
    description text                                         not null
);

alter table foundation.searches
    owner to postgres;

create table foundation.search_to_presence
(
    search_id   integer not null
        constraint search_to_presence_searches_id_fk
            references searches,
    presence_id integer not null
        constraint search_to_presence_presences_id_fk
            references presences,
    constraint search_to_presence_pk
        primary key (search_id, presence_id)
);

alter table foundation.search_to_presence
    owner to postgres;