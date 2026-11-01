CREATE TABLE profile_projects (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name varchar(250) NOT NULL,
    description text,
    url varchar(700),
    project_type varchar(70) NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_profile_projects_user ON profile_projects(user_id);
