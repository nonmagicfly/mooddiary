-- Create application database in PostgreSQL.
-- Run as a superuser.

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'mooddiary') THEN
    CREATE DATABASE mooddiary;
  END IF;
END
$$;

