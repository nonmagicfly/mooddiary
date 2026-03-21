-- Create PostgreSQL roles for the application.
-- Replace placeholders before running.

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'mooddiary_app') THEN
    CREATE ROLE mooddiary_app LOGIN PASSWORD '__DB_PASSWORD__';
  END IF;
END
$$;

