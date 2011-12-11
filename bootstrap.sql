-- initalization for the trophy database
-- anderson@submarinerich.com

DROP DATABASE IF EXISTS trophy;
CREATE DATABASE trophy;
\c trophy;
CREATE USER trophy nocreatedb nocreateuser;
CREATE SCHEMA trophy authorization trophy;
GRANT all ON SCHEMA trophy to trophy;
GRANT usage ON schema public to trophy;
ALTER USER trophy SET search_path TO trophy, public;
ALTER USER trophy WITH PASSWORD 'trophy48284sdwrervivce';

DROP TABLE IF EXISTS favorites;
CREATE TABLE favorites (
  id bigserial constraint favorites_pk primary key,
	source bigint NOT NULL,
	destination bigint NOT NULL,
	category int DEFAULT 0 NOT NULL
);
grant all on favorites to trophy;
grant all on favorites_id_seq to trophy;
COMMENT ON TABLE favorites IS 'favorites table';

DROP TABLE IF EXISTS ratings;
CREATE TABLE ratings (
	id bigserial constraint ratings_pk primary key,
	source bigint NOT NULL,
	destination bigint NOT NULL,
	rating int DEFAULT 0,
	min int DEFAULT 0,
	max int DEFAULT 5
);
COMMENT ON TABLE ratings IS 'ratings table';
grant all on ratings to trophy;
grant all on ratings_id_seq to trophy;
