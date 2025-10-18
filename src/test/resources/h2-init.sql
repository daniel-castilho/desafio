-- Define PostgreSQL-specific types as VARCHAR for H2 compatibility during tests.
CREATE DOMAIN IF NOT EXISTS task_priority AS VARCHAR(255);
CREATE DOMAIN IF NOT EXISTS task_status AS VARCHAR(255);
