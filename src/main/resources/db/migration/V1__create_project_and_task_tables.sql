CREATE TABLE project (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    CONSTRAINT name_min_length CHECK (LENGTH(name) >= 3),
    description TEXT,
    start_date DATE,
    end_date DATE,
    CONSTRAINT end_after_start CHECK (end_date IS NULL OR end_date >= start_date)
);

CREATE TYPE task_status AS ENUM ('TODO', 'DOING', 'DONE');

CREATE TYPE task_priority AS ENUM ('LOW', 'MEDIUM', 'HIGH');

CREATE TABLE task (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(150) NOT NULL,
    CONSTRAINT title_min_length CHECK (LENGTH(title) >= 5),
    description TEXT,
    status task_status NOT NULL,
    priority task_priority NOT NULL,
    due_date DATE,
    project_id UUID NOT NULL,
    CONSTRAINT fk_project
        FOREIGN KEY (project_id)
        REFERENCES project (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_task_project_id ON task (project_id);

CREATE INDEX idx_task_filters ON task (project_id, status, priority);