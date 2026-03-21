-- Grant privileges to application roles.

DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_database WHERE datname = 'mooddiary') THEN
    GRANT CONNECT ON DATABASE mooddiary TO mooddiary_app;
    GRANT USAGE ON SCHEMA public TO mooddiary_app;
    GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO mooddiary_app;
    GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO mooddiary_app;
  END IF;
END
$$;

