-- =============================================================================
-- INITIALIZE DATABASES & NORMALIZED SCHEMA FOR IUH-PROGA MICROSERVICES
-- =============================================================================

CREATE DATABASE proga_auth_db;
CREATE DATABASE proga_workspace_db;
CREATE DATABASE proga_ai_db;
CREATE DATABASE proga_notification_db;

-- =============================================================================
-- 1. SCHEMA FOR proga_auth_db (Auth & User Profile Service)
-- =============================================================================
\c proga_auth_db;

CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(225),
    phone_number VARCHAR(20),
    full_name VARCHAR(100),
    display_name VARCHAR(100),
    job_title VARCHAR(100),
    bio TEXT,
    is_admin BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);

-- =============================================================================
-- 2. SCHEMA FOR proga_workspace_db (Workspace, Space RBAC, Sprint & Kanban Task Service)
-- =============================================================================
\c proga_workspace_db;

CREATE TABLE IF NOT EXISTS workspaces (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    owner_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS workspace_members (
    workspace_id BIGINT NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL DEFAULT 3, -- 2: PM, 3: MEMBER, 4: ASSISTANT
    status VARCHAR(20) NOT NULL DEFAULT 'ACCEPTED', -- ACCEPTED | PENDING
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (workspace_id, user_id)
);

CREATE TABLE IF NOT EXISTS spaces (
    id BIGSERIAL PRIMARY KEY,
    workspace_id BIGINT NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    is_private BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS space_members (
    space_id BIGINT NOT NULL REFERENCES spaces(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL DEFAULT 3, -- 2: PM (Lead PM / PM Phụ), 4: ASSISTANT (Trợ lý điều phối), 3: MEMBER (Thành viên thực thi)
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (space_id, user_id)
);

CREATE TABLE IF NOT EXISTS sprints (
    id BIGSERIAL PRIMARY KEY,
    space_id BIGINT NOT NULL REFERENCES spaces(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    goal TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'FUTURE', -- FUTURE | ACTIVE | CLOSED
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tasks (
    id BIGSERIAL PRIMARY KEY,
    space_id BIGINT NOT NULL REFERENCES spaces(id) ON DELETE CASCADE,
    sprint_id BIGINT REFERENCES sprints(id) ON DELETE SET NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'TODO', -- TODO | IN_PROGRESS | IN_REVIEW | DONE
    priority VARCHAR(10) NOT NULL DEFAULT 'MEDIUM', -- LOW | MEDIUM | HIGH | URGENT
    owner_id BIGINT,
    start_date TIMESTAMP,
    due_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS task_notes (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    author_id BIGINT NOT NULL,
    note_content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS workspace_logs (
    id BIGSERIAL PRIMARY KEY,
    workspace_id BIGINT NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    task_id BIGINT,
    user_id BIGINT NOT NULL,
    action_type VARCHAR(50),
    old_value TEXT,
    new_value TEXT,
    log_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_spaces_workspace_id ON spaces(workspace_id);
CREATE INDEX IF NOT EXISTS idx_sprints_space_id ON sprints(space_id);
CREATE INDEX IF NOT EXISTS idx_tasks_space_id ON tasks(space_id);
CREATE INDEX IF NOT EXISTS idx_tasks_sprint_id ON tasks(sprint_id);
CREATE INDEX IF NOT EXISTS idx_tasks_owner_id ON tasks(owner_id);
CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks(status);

-- =============================================================================
-- 3. SCHEMA FOR proga_ai_db (AI Multi-Agent & RAG Knowledge Service)
-- =============================================================================
\c proga_ai_db;

CREATE TABLE IF NOT EXISTS ai_threads (
    id BIGSERIAL PRIMARY KEY,
    openai_thread_id VARCHAR(100),
    space_id BIGINT NOT NULL,
    user_id BIGINT,
    agent_type VARCHAR(30) NOT NULL, -- REQUIREMENT | PROJECT_MANAGER | TECHNICAL_ADVISOR
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ai_chat_messages (
    id BIGSERIAL PRIMARY KEY,
    thread_id BIGINT NOT NULL REFERENCES ai_threads(id) ON DELETE CASCADE,
    sender_type VARCHAR(20) NOT NULL, -- USER | ASSISTANT
    message_content TEXT,
    json_payload TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ai_knowledge_samples (
    id BIGSERIAL PRIMARY KEY,
    space_id BIGINT,
    title VARCHAR(255) NOT NULL,
    domain VARCHAR(100),
    requirement_text TEXT NOT NULL,
    summary TEXT,
    tasks_json TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- 4. SCHEMA FOR proga_notification_db (Real-time Notification Service)
-- =============================================================================
\c proga_notification_db;

CREATE TABLE IF NOT EXISTS notification_types (
    id SMALLSERIAL PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    label VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    workspace_id BIGINT,
    title VARCHAR(150),
    content TEXT,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    type_id SMALLINT REFERENCES notification_types(id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications(user_id);
