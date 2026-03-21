#!/bin/bash
set -e
# Создаём БД и пользователя Keycloak (выполняется только при первой инициализации PostgreSQL)

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "postgres" <<-EOSQL
  CREATE DATABASE keycloak;
  CREATE USER keycloak WITH PASSWORD '${KEYCLOAK_DB_PASSWORD}';
  GRANT ALL PRIVILEGES ON DATABASE keycloak TO keycloak;
EOSQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "keycloak" <<-EOSQL
  GRANT ALL ON SCHEMA public TO keycloak;
EOSQL
