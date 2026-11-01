CREATE TABLE users (
 id uuid PRIMARY KEY, email varchar(320) NOT NULL UNIQUE,
 first_name varchar(120) NOT NULL, last_name varchar(120) NOT NULL,
 password_hash varchar(120) NOT NULL, role varchar(32) NOT NULL DEFAULT 'STUDENT',
 created_at timestamptz NOT NULL DEFAULT now(),
 CONSTRAINT ck_user_role CHECK (role IN ('STUDENT','ADMIN'))
);
CREATE TABLE roles (
 id varchar(100) PRIMARY KEY, name varchar(180) NOT NULL,area varchar(100) NOT NULL,
 icon varchar(50),description text NOT NULL,responsibilities text NOT NULL,
 demand_tag varchar(120),active boolean NOT NULL DEFAULT true
);
CREATE TABLE profiles (
 user_id uuid PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
 career varchar(200),stage varchar(140),objective varchar(300),university varchar(200),
 graduation_year integer, selected_role_id varchar(100) REFERENCES roles(id) ON DELETE SET NULL,
 updated_at timestamptz NOT NULL DEFAULT now()
);
CREATE TABLE skill_categories(id varchar(60) PRIMARY KEY,name varchar(150) NOT NULL,icon varchar(50));
CREATE TABLE skills (
 id varchar(100) PRIMARY KEY,category_id varchar(60) NOT NULL REFERENCES skill_categories(id),
 name varchar(180) NOT NULL,description text,active boolean NOT NULL DEFAULT true
);
CREATE TABLE role_skills (
 id uuid PRIMARY KEY, role_id varchar(100) NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
 skill_id varchar(100) NOT NULL REFERENCES skills(id), required_level integer NOT NULL,
 importance varchar(30) NOT NULL, UNIQUE(role_id,skill_id),
 CONSTRAINT ck_role_level CHECK(required_level BETWEEN 1 AND 4),
 CONSTRAINT ck_importance CHECK(importance IN ('esencial','complementaria'))
);
CREATE TABLE user_skills (
 id uuid PRIMARY KEY,user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 skill_id varchar(100) NOT NULL REFERENCES skills(id),level integer NOT NULL,
 updated_at timestamptz NOT NULL DEFAULT now(),UNIQUE(user_id,skill_id),
 CONSTRAINT ck_user_skill_level CHECK(level BETWEEN 0 AND 4)
);
CREATE TABLE interest_answers (
 id uuid PRIMARY KEY,user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 dimension varchar(60) NOT NULL,score integer NOT NULL,updated_at timestamptz NOT NULL DEFAULT now(),
 UNIQUE(user_id,dimension),CONSTRAINT ck_interest_score CHECK(score BETWEEN 0 AND 4)
);
CREATE TABLE refresh_tokens (
 id uuid PRIMARY KEY,user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 token_hash char(64) NOT NULL UNIQUE,expires_at timestamptz NOT NULL,revoked_at timestamptz
);
CREATE INDEX idx_refresh_user ON refresh_tokens(user_id);
CREATE TABLE matching_runs (
 id uuid PRIMARY KEY,user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 algorithm_version varchar(50) NOT NULL,created_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_matching_user_date ON matching_runs(user_id,created_at DESC);
CREATE TABLE matching_results (
 id uuid PRIMARY KEY,run_id uuid NOT NULL REFERENCES matching_runs(id) ON DELETE CASCADE,
 role_id varchar(100) NOT NULL REFERENCES roles(id),affinity integer,readiness integer,
 confidence integer NOT NULL,coverage integer,matched_skill_names text,explanation text NOT NULL,
 UNIQUE(run_id,role_id),CONSTRAINT ck_affinity CHECK(affinity BETWEEN 0 AND 100),
 CONSTRAINT ck_readiness CHECK(readiness BETWEEN 0 AND 100),
 CONSTRAINT ck_confidence CHECK(confidence BETWEEN 0 AND 100)
);
CREATE TABLE skill_gap_results (
 id uuid PRIMARY KEY,user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 role_id varchar(100) NOT NULL REFERENCES roles(id),created_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_skill_gap_user_date ON skill_gap_results(user_id,created_at DESC);
CREATE TABLE skill_gap_items (
 id uuid PRIMARY KEY,run_id uuid NOT NULL REFERENCES skill_gap_results(id) ON DELETE CASCADE,
 skill_id varchar(100) NOT NULL REFERENCES skills(id),required_level integer NOT NULL,
 current_level integer,status varchar(32) NOT NULL,priority varchar(30) NOT NULL,explanation text NOT NULL,
 UNIQUE(run_id,skill_id),CONSTRAINT ck_gap_level CHECK(current_level BETWEEN 0 AND 4)
);
CREATE TABLE learning_resources (
 id varchar(100) PRIMARY KEY,skill_id varchar(100) NOT NULL REFERENCES skills(id),
 name varchar(250) NOT NULL,provider varchar(160) NOT NULL,url varchar(600) NOT NULL,
 type varchar(50) NOT NULL,free boolean NOT NULL,priority varchar(30) NOT NULL,
 active boolean NOT NULL DEFAULT true,verified_at timestamptz
);
CREATE TABLE learning_plans (
 id uuid PRIMARY KEY,user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 role_id varchar(100) NOT NULL REFERENCES roles(id),gap_run_id uuid NOT NULL REFERENCES skill_gap_results(id) ON DELETE CASCADE,
 created_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_learning_user_date ON learning_plans(user_id,created_at DESC);
CREATE TABLE learning_steps (
 id uuid PRIMARY KEY,plan_id uuid NOT NULL REFERENCES learning_plans(id) ON DELETE CASCADE,
 skill_id varchar(100) NOT NULL REFERENCES skills(id),order_index integer NOT NULL,
 resource_id varchar(100) REFERENCES learning_resources(id),priority varchar(30) NOT NULL,
 explanation text NOT NULL,UNIQUE(plan_id,order_index)
);
