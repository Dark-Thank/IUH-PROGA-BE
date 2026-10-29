-- =============================================================================
-- COMPREHENSIVE SCHEMA & RICH SEED DATA FOR IUH-PROGA SYSTEM
-- Execute via: docker exec -i proga-postgres psql -U postgres < seed-data.sql
-- Default password for all accounts: password123
-- =============================================================================

-- =============================================================================
-- 1. CONNECT TO DATABASE: proga_auth_db
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

TRUNCATE TABLE users, roles RESTART IDENTITY CASCADE;

-- 1.1. Chèn 4 Roles chuẩn của hệ thống (ADMIN, PM, MEMBER, ASSISTANT)
INSERT INTO roles (id, name) VALUES 
(1, 'ADMIN'),
(2, 'PM'),
(3, 'MEMBER'),
(4, 'ASSISTANT')
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name;

-- 1.2. Chèn 12 Users với hồ sơ chuyên môn chuẩn hóa đa dạng (Mật khẩu: password123)
INSERT INTO users (id, username, email, password, avatar_url, phone_number, full_name, display_name, job_title, bio, is_admin, created_at) VALUES 
(1, 'admin_user', 'admin@proga.iuh.edu.vn', '$2a$10$QUC2khs76FWt7FUpdiDX1.09bAEuyQ4ptkdcrQwYqx.ec4ZEqgWvS', 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin', '0901234567', 'Hệ Thống Admin', 'Quản Trị Viên', 'System Administrator', 'Quản trị viên toàn hệ thống IUH-PROGA', true, NOW()),
(2, 'pm_sonluu', 'sonluu@proga.iuh.edu.vn', '$2a$10$QUC2khs76FWt7FUpdiDX1.09bAEuyQ4ptkdcrQwYqx.ec4ZEqgWvS', 'https://api.dicebear.com/7.x/avataaars/svg?seed=sonluu', '0912345678', 'Lưu Ngọc Cao Sơn', 'Cao Sơn (Lead PM)', 'AI & Fullstack Architect', 'Fullstack, AI Agent, Kiến trúc dữ liệu PostgreSQL/PGVector và Mobile React Native', false, NOW()),
(3, 'pm_lananh', 'lananh@proga.iuh.edu.vn', '$2a$10$QUC2khs76FWt7FUpdiDX1.09bAEuyQ4ptkdcrQwYqx.ec4ZEqgWvS', 'https://api.dicebear.com/7.x/avataaars/svg?seed=lananh', '0923456789', 'Nguyễn Lân Anh', 'Lan Anh (PM)', 'Project Manager & Scrum Master', 'Quản lý dự án Agile/Scrum, điều phối tiến độ E-Commerce và Fintech', false, NOW()),
(4, 'dev_duy', 'duy@proga.iuh.edu.vn', '$2a$10$QUC2khs76FWt7FUpdiDX1.09bAEuyQ4ptkdcrQwYqx.ec4ZEqgWvS', 'https://api.dicebear.com/7.x/avataaars/svg?seed=duy', '0934567890', 'Trần Duy', 'Duy Fullstack', 'Fullstack Developer', 'Lập trình viên React/Next.js, TypeScript và Java Spring Boot', false, NOW()),
(5, 'tester_hoa', 'hoa@proga.iuh.edu.vn', '$2a$10$QUC2khs76FWt7FUpdiDX1.09bAEuyQ4ptkdcrQwYqx.ec4ZEqgWvS', 'https://api.dicebear.com/7.x/avataaars/svg?seed=hoa', '0945678901', 'Nguyễn Hòa', 'Hòa QA/BA', 'QA Lead & Project Coordinator', 'Kỹ sư kiểm thử chất lượng phần mềm, hỗ trợ điều phối Sprint và kiểm soát biên bản họp', false, NOW()),
(6, 'dev_minhtri', 'tri@proga.iuh.edu.vn', '$2a$10$QUC2khs76FWt7FUpdiDX1.09bAEuyQ4ptkdcrQwYqx.ec4ZEqgWvS', 'https://api.dicebear.com/7.x/avataaars/svg?seed=minhtri', '0956789012', 'Vũ Minh Trí', 'Trí Backend', 'Backend & DevOps Lead', 'Lập trình viên Java Spring Boot Microservices, Docker, Redis và bảo mật API Gateway', false, NOW()),
(7, 'ba_tuan', 'tuan@proga.iuh.edu.vn', '$2a$10$QUC2khs76FWt7FUpdiDX1.09bAEuyQ4ptkdcrQwYqx.ec4ZEqgWvS', 'https://api.dicebear.com/7.x/avataaars/svg?seed=batuan', '0967890123', 'Lê Anh Tuấn', 'Tuấn BA (Assistant)', 'Business Analyst & Scrum Assistant', 'Chuyên viên phân tích nghiệp vụ BA, soạn thảo SRS, nạp tài liệu cho AI phân rã WBS nháp', false, NOW()),
(8, 'lead_khanh', 'khanh@proga.iuh.edu.vn', '$2a$10$QUC2khs76FWt7FUpdiDX1.09bAEuyQ4ptkdcrQwYqx.ec4ZEqgWvS', 'https://api.dicebear.com/7.x/avataaars/svg?seed=khanh', '0978901234', 'Trần Đỗ Đăng Khánh', 'Đăng Khánh (PM Backend)', 'Tech Lead / Backend & DevOps', 'Backend, DevOps, Quản lý tiến độ dự án, tối ưu hóa kiến trúc Microservices', false, NOW()),
(9, 'fe_nam', 'nam@proga.iuh.edu.vn', '$2a$10$QUC2khs76FWt7FUpdiDX1.09bAEuyQ4ptkdcrQwYqx.ec4ZEqgWvS', 'https://api.dicebear.com/7.x/avataaars/svg?seed=nam', '0989012345', 'Phạm Hoàng Nam', 'Nam Frontend', 'Frontend & UI/UX Developer', 'Chuyên gia giao diện Next.js 15, TailwindCSS, thiết kế Kanban Drag & Drop và Mindmap', false, NOW()),
(10, 'ai_thao', 'thao@proga.iuh.edu.vn', '$2a$10$QUC2khs76FWt7FUpdiDX1.09bAEuyQ4ptkdcrQwYqx.ec4ZEqgWvS', 'https://api.dicebear.com/7.x/avataaars/svg?seed=thao', '0990123456', 'Đặng Thu Thảo', 'Thảo AI Engineer', 'AI & RAG Specialist', 'Nghiên cứu mô hình LLM Gemini/OpenAI, Spring AI, PGVector và hệ thống RAG đa tác tử', false, NOW()),
(11, 'devops_bao', 'bao@proga.iuh.edu.vn', '$2a$10$QUC2khs76FWt7FUpdiDX1.09bAEuyQ4ptkdcrQwYqx.ec4ZEqgWvS', 'https://api.dicebear.com/7.x/avataaars/svg?seed=bao', '0911223344', 'Ngô Quốc Bảo', 'Bảo DevOps', 'DevOps & Cloud Engineer', 'Triển khai CI/CD GitHub Actions, Docker Swarm, giám sát Prometheus & Grafana', false, NOW()),
(12, 'qa_linh', 'linh@proga.iuh.edu.vn', '$2a$10$QUC2khs76FWt7FUpdiDX1.09bAEuyQ4ptkdcrQwYqx.ec4ZEqgWvS', 'https://api.dicebear.com/7.x/avataaars/svg?seed=linh', '0922334455', 'Bùi Khánh Linh', 'Linh QA Tester', 'Automation QA Tester', 'Kiểm thử tự động Postman/Playwright, viết kịch bản Test Case và nghiệm thu tính năng', false, NOW())
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('roles', 'id'), COALESCE(MAX(id), 1)) FROM roles;
SELECT setval(pg_get_serial_sequence('users', 'id'), COALESCE(MAX(id), 1)) FROM users;


-- =============================================================================
-- 2. CONNECT TO DATABASE: proga_workspace_db
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
    workspace_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL DEFAULT 3,
    status VARCHAR(20) NOT NULL DEFAULT 'ACCEPTED',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (workspace_id, user_id)
);

CREATE TABLE IF NOT EXISTS spaces (
    id BIGSERIAL PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    is_private BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS space_members (
    space_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL DEFAULT 3,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (space_id, user_id)
);

CREATE TABLE IF NOT EXISTS sprints (
    id BIGSERIAL PRIMARY KEY,
    space_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    goal TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'FUTURE',
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tasks (
    id BIGSERIAL PRIMARY KEY,
    space_id BIGINT NOT NULL,
    sprint_id BIGINT,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'TODO',
    priority VARCHAR(10) NOT NULL DEFAULT 'MEDIUM',
    owner_id BIGINT,
    start_date TIMESTAMP,
    due_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP
);

-- Gỡ bỏ ràng buộc CHECK cũ của Hibernate (nếu có) để hỗ trợ đầy đủ 4 cột Kanban: TODO, IN_PROGRESS, IN_REVIEW, DONE
ALTER TABLE tasks DROP CONSTRAINT IF EXISTS tasks_status_check;

CREATE TABLE IF NOT EXISTS task_notes (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    note_content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS workspace_logs (
    id BIGSERIAL PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    task_id BIGINT,
    user_id BIGINT NOT NULL,
    action_type VARCHAR(50),
    old_value TEXT,
    new_value TEXT,
    log_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

TRUNCATE TABLE workspace_logs, task_notes, tasks, sprints, space_members, spaces, workspace_members, workspaces RESTART IDENTITY CASCADE;

-- 2.1. Chèn 10 Workspaces mẫu (Công ty / Trường học / Tổ chức)
INSERT INTO workspaces (id, name, description, owner_id, created_at) VALUES 
(1, 'Trường Đại Học Công Nghiệp TP.HCM (IUH)', 'Tổ chức giáo dục & Quản lý Khóa luận tốt nghiệp sinh viên khoa CNTT IUH', 2, NOW()),
(2, 'Công Ty Công Nghệ PROGA Solutions', 'Tổ chức phát triển phần mềm Doanh nghiệp & Giải pháp Microservices', 3, NOW()),
(3, 'Tập Đoàn Tài Chính VietFintech Corporation', 'Tổ chức quản lý các giải pháp Ngân hàng số & Ví điện tử bảo mật', 8, NOW()),
(4, 'Tập Đoàn Bán Lẻ E-Mart Global', 'Tổ chức điều hành chuỗi siêu thị bán lẻ & Sàn thương mại điện tử', 3, NOW()),
(5, 'Viện Nghiên Cứu Trí Tuệ Nhân Tạo AI Research Lab', 'Tổ chức nghiên cứu & phát triển mô hình AI Agents / LLM RAG', 2, NOW()),
(6, 'Công Ty Phần Mềm FPT Software Services', 'Tổ chức gia công phần mềm quốc tế & Giải pháp Cloud Transformation', 8, NOW()),
(7, 'Tổng Công Ty Logistics & Vận Tải FastDelivery', 'Tổ chức điều hành mạng lưới kho vận & Giao hàng chặng cuối', 2, NOW()),
(8, 'Bệnh Viện Đa Khoa Quốc Tế MedCare', 'Tổ chức y tế quản lý phòng khám & Hồ sơ bệnh án điện tử', 3, NOW()),
(9, 'Tập Đoàn Giáo Dục SmartLearning Group', 'Tổ chức quản lý các nền tảng học trực tuyến LMS & Đào tạo từ xa', 2, NOW()),
(10, 'Công Ty Truyền Thông & Streaming MediaFlix', 'Tổ chức cung cấp dịch vụ giải trí & Truyền hình trực tuyến HLS', 3, NOW())
ON CONFLICT (id) DO NOTHING;

-- 2.2. Chèn Workspace Members (38 bản ghi nhân sự thuộc các Workspace)
INSERT INTO workspace_members (workspace_id, user_id, role_id, status, joined_at) VALUES 
-- Workspace 1 (IUH - Owner: Cao Sơn id=2)
(1, 2, 2, 'ACCEPTED', NOW()),  -- Lưu Ngọc Cao Sơn (Owner / Lead PM)
(1, 8, 2, 'ACCEPTED', NOW()),  -- Trần Đỗ Đăng Khánh (Tech Lead / PM Backend)
(1, 7, 4, 'ACCEPTED', NOW()),  -- Lê Anh Tuấn (BA / Assistant)
(1, 5, 4, 'ACCEPTED', NOW()),  -- Nguyễn Hòa (QA / Assistant)
(1, 4, 3, 'ACCEPTED', NOW()),  -- Trần Duy (Fullstack Member)
(1, 6, 3, 'ACCEPTED', NOW()),  -- Vũ Minh Trí (Backend Member)
(1, 9, 3, 'ACCEPTED', NOW()),  -- Phạm Hoàng Nam (Frontend Member)
(1, 10, 3, 'ACCEPTED', NOW()), -- Đặng Thu Thảo (AI Member)
(1, 11, 3, 'ACCEPTED', NOW()), -- Ngô Quốc Bảo (DevOps Member)
(1, 12, 3, 'PENDING', NOW()),  -- Bùi Khánh Linh (Lời mời đang chờ xác nhận)

-- Workspace 2 (PROGA Solutions - Owner: Lân Anh id=3)
(2, 3, 2, 'ACCEPTED', NOW()),
(2, 2, 2, 'ACCEPTED', NOW()),
(2, 4, 3, 'ACCEPTED', NOW()),
(2, 6, 3, 'ACCEPTED', NOW()),
(2, 7, 4, 'ACCEPTED', NOW()),
(2, 9, 3, 'ACCEPTED', NOW()),
(2, 12, 3, 'ACCEPTED', NOW()),

-- Workspace 3 (VietFintech - Owner: Đăng Khánh id=8)
(3, 8, 2, 'ACCEPTED', NOW()),
(3, 2, 2, 'ACCEPTED', NOW()),
(3, 4, 3, 'ACCEPTED', NOW()),
(3, 5, 4, 'ACCEPTED', NOW()),
(3, 6, 3, 'ACCEPTED', NOW()),
(3, 11, 3, 'ACCEPTED', NOW()),

-- Workspace 4..10
(4, 3, 2, 'ACCEPTED', NOW()),
(4, 5, 3, 'ACCEPTED', NOW()),
(4, 9, 3, 'ACCEPTED', NOW()),
(5, 2, 2, 'ACCEPTED', NOW()),
(5, 10, 2, 'ACCEPTED', NOW()),
(5, 6, 3, 'ACCEPTED', NOW()),
(5, 7, 4, 'ACCEPTED', NOW()),
(6, 8, 2, 'ACCEPTED', NOW()),
(6, 11, 3, 'ACCEPTED', NOW()),
(7, 2, 2, 'ACCEPTED', NOW()),
(8, 3, 2, 'ACCEPTED', NOW()),
(9, 2, 2, 'ACCEPTED', NOW()),
(10, 3, 2, 'ACCEPTED', NOW())
ON CONFLICT (workspace_id, user_id) DO NOTHING;

-- 2.3. Chèn 12 Spaces (Dự án phần mềm nằm trong các Workspace)
INSERT INTO spaces (id, workspace_id, name, start_date, end_date, is_private, created_at) VALUES 
(1, 1, 'Dự Án Khóa Luận Tốt Nghiệp IUH-PROGA', '2026-07-01 08:00:00', '2026-11-15 18:00:00', false, NOW()),
(2, 1, 'Dự Án Hệ Thống Quản Lý Ký Túc Xá Sinh Viên IUH', '2026-08-01 08:00:00', '2026-10-30 18:00:00', false, NOW()),
(3, 1, 'Dự Án Cổng Đăng Ký Tín Chỉ & Thời Khóa Biểu (Private)', '2026-09-01 08:00:00', '2026-11-30 18:00:00', true, NOW()),
(4, 2, 'Dự Án Sàn Thương Mại Điện Tử Microservices', '2026-07-01 08:00:00', '2026-10-31 18:00:00', false, NOW()),
(5, 2, 'Dự Án Hệ Thống Quản Lý Nhân Sự Smart HRM', '2026-08-01 08:00:00', '2026-10-30 18:00:00', false, NOW()),
(6, 3, 'Dự Án App Mobile Banking & Sinh Trắc Học', '2026-07-01 08:00:00', '2026-10-31 18:00:00', false, NOW()),
(7, 3, 'Dự Án Cổng Thanh Toán Mã QR VietQR (Private)', '2026-08-01 08:00:00', '2026-10-30 18:00:00', true, NOW()),
(8, 4, 'Dự Án Chuỗi Cung Ứng & Quản Lý Kho WMS', '2026-07-01 08:00:00', '2026-10-31 18:00:00', false, NOW()),
(9, 5, 'Dự Án Hệ Thống Multi-Agent AI & RAG Knowledge Base', '2026-07-01 08:00:00', '2026-11-15 18:00:00', false, NOW()),
(10, 5, 'Dự Án Phân Tích Dữ Liệu Khách Hàng BigData (Private)', '2026-09-01 08:00:00', '2026-11-30 18:00:00', true, NOW()),
(11, 6, 'Dự Án Chuyển Đổi Hạ Tầng Cloud Kubernetes', '2026-08-01 08:00:00', '2026-11-30 18:00:00', false, NOW()),
(12, 9, 'Dự Án Nền Tảng Học Trực Tuyến E-Learning LMS', '2026-08-15 08:00:00', '2026-11-30 18:00:00', false, NOW())
ON CONFLICT (id) DO NOTHING;

-- 2.4. Chèn Space Members với Ma trận 3 Vai trò:
--      role_id = 2: PM (Lead PM / PM Phụ duyệt task DONE)
--      role_id = 4: ASSISTANT (Trợ lý điều phối, nạp tài liệu AI nháp, sửa hạn chót, kéo Kanban nhưng không duyệt DONE)
--      role_id = 3: MEMBER (Thành viên thực thi mặc định, chuyển task sang IN_REVIEW chờ PM duyệt)
INSERT INTO space_members (space_id, user_id, role_id, joined_at) VALUES
-- Space 1: Dự Án Khóa Luận Tốt Nghiệp IUH-PROGA (Đầy đủ 9 thành viên với 3 tầng vai trò rõ nét)
(1, 2, 2, NOW()),  -- Lưu Ngọc Cao Sơn: Lead PM (Người tạo Space)
(1, 8, 2, NOW()),  -- Trần Đỗ Đăng Khánh: PM Phụ (PM Backend & DevOps - cùng duyệt task)
(1, 7, 4, NOW()),  -- Lê Anh Tuấn: ASSISTANT (BA - Trợ lý điều phối & nạp tài liệu AI phân rã nháp)
(1, 5, 4, NOW()),  -- Nguyễn Hòa: ASSISTANT (QA Coordinator - Trợ lý điều phối & kiểm tra chất lượng)
(1, 4, 3, NOW()),  -- Trần Duy: MEMBER (Fullstack Developer)
(1, 6, 3, NOW()),  -- Vũ Minh Trí: MEMBER (Backend Developer)
(1, 9, 3, NOW()),  -- Phạm Hoàng Nam: MEMBER (Frontend Developer)
(1, 10, 3, NOW()), -- Đặng Thu Thảo: MEMBER (AI Specialist)
(1, 11, 3, NOW()), -- Ngô Quốc Bảo: MEMBER (DevOps Engineer)

-- Space 2: Dự Án Quản Lý Ký Túc Xá Sinh Viên IUH
(2, 2, 2, NOW()),  -- Cao Sơn (Lead PM)
(2, 7, 4, NOW()),  -- Anh Tuấn (ASSISTANT)
(2, 4, 3, NOW()),  -- Trần Duy (MEMBER)
(2, 9, 3, NOW()),  -- Hoàng Nam (MEMBER)
(2, 6, 3, NOW()),  -- Minh Trí (MEMBER)

-- Space 3: Dự Án Đăng Ký Tín Chỉ (Private)
(3, 2, 2, NOW()),
(3, 8, 2, NOW()),
(3, 6, 3, NOW()),

-- Space 4: Dự Án Sàn Thương Mại Điện Tử Microservices
(4, 3, 2, NOW()),  -- Lân Anh (Lead PM)
(4, 2, 2, NOW()),  -- Cao Sơn (PM Phụ Kỹ thuật)
(4, 7, 4, NOW()),  -- Anh Tuấn (ASSISTANT)
(4, 4, 3, NOW()),  -- Trần Duy (MEMBER)
(4, 6, 3, NOW()),  -- Minh Trí (MEMBER)
(4, 9, 3, NOW()),  -- Hoàng Nam (MEMBER)
(4, 12, 3, NOW()), -- Khánh Linh (MEMBER)

-- Space 5..12
(5, 3, 2, NOW()),
(5, 4, 3, NOW()),
(6, 8, 2, NOW()),  -- Đăng Khánh (Lead PM)
(6, 2, 2, NOW()),  -- Cao Sơn (PM Phụ AI & Mobile)
(6, 5, 4, NOW()),  -- Nguyễn Hòa (ASSISTANT)
(6, 4, 3, NOW()),  -- Trần Duy (MEMBER)
(6, 6, 3, NOW()),  -- Minh Trí (MEMBER)
(7, 8, 2, NOW()),
(8, 3, 2, NOW()),
(9, 2, 2, NOW()),
(9, 10, 2, NOW()),
(9, 7, 4, NOW()),
(9, 6, 3, NOW()),
(10, 2, 2, NOW()),
(11, 8, 2, NOW()),
(11, 11, 3, NOW()),
(12, 2, 2, NOW())
ON CONFLICT (space_id, user_id) DO NOTHING;

-- 2.5. Chèn 13 Sprints mẫu qua các Space
INSERT INTO sprints (id, space_id, name, goal, status, start_date, end_date, created_at) VALUES 
-- Space 1 (5 Sprints: 2 CLOSED, 1 ACTIVE, 2 FUTURE)
(1, 1, 'Sprint 1: Khởi tạo Kiến trúc Microservices & Auth JWT', 'Thiết kế CSDL 4 Databases, cấu hình Eureka, API Gateway và Auth-Service JWT', 'CLOSED', '2026-07-01 08:00:00', '2026-07-20 18:00:00', NOW()),
(2, 1, 'Sprint 2: Quản lý Workspace, Space RBAC & Backlog UI', 'Xây dựng phân quyền 3 tầng (PM, ASSISTANT, MEMBER), Space Public/Private và Backlog Accordion', 'CLOSED', '2026-07-21 08:00:00', '2026-08-15 18:00:00', NOW()),
(3, 1, 'Sprint 3: Bảng Kanban 4 Cột (In Review) & Đồng bộ WebSocket', 'Triển khai Kanban 4 cột (TODO, IN_PROGRESS, IN_REVIEW, DONE), luồng Member nộp bài & PM duyệt', 'ACTIVE', '2026-10-15 08:00:00', '2026-11-05 18:00:00', NOW()),
(4, 1, 'Sprint 4: Tích hợp Multi-Agent AI & Kho Tri Thức RAG PGVector', 'Xây dựng Requirement Agent phân rã WBS, PM Agent dự báo rủi ro và Auto-Assign cân bằng tải', 'FUTURE', '2026-11-06 08:00:00', '2026-11-20 18:00:00', NOW()),
(5, 1, 'Sprint 5: Kiểm thử E2E, Tối ưu Hiệu năng & Bảo vệ Khóa luận', 'Kiểm thử toàn diện, đóng gói Docker Compose và hoàn thiện quyển báo cáo KLTN', 'FUTURE', '2026-11-21 08:00:00', '2026-12-05 18:00:00', NOW()),

-- Space 2 (Ký túc xá IUH)
(6, 2, 'Sprint 1: Phân hệ Quản lý Phòng ở & Hợp đồng Nội trú', 'Xây dựng API đăng ký phòng, xếp giường và tính phí nội trú sinh viên', 'CLOSED', '2026-08-01 08:00:00', '2026-08-25 18:00:00', NOW()),
(7, 2, 'Sprint 2: Hóa đơn Điện Nước & Thanh toán trực tuyến', 'Tích hợp gạch nợ hóa đơn tự động qua mã QR và thống kê doanh thu KTX', 'ACTIVE', '2026-10-10 08:00:00', '2026-11-05 18:00:00', NOW()),

-- Space 4 (Sàn TMĐT Microservices)
(8, 4, 'Sprint 1: Product Catalog & Tìm kiếm ElasticSearch', 'Xây dựng dịch vụ danh mục sản phẩm và đồng bộ chỉ mục tìm kiếm nhanh', 'CLOSED', '2026-07-01 08:00:00', '2026-07-25 18:00:00', NOW()),
(9, 4, 'Sprint 2: Giỏ hàng Redis, Đặt hàng & Thanh toán VNPAY/MoMo', 'Xử lý đặt hàng chịu tải cao, khóa tồn kho Flash Sale và cổng thanh toán', 'ACTIVE', '2026-10-12 08:00:00', '2026-11-05 18:00:00', NOW()),
(10, 4, 'Sprint 3: Gợi ý sản phẩm bằng AI & Đánh giá Người mua', 'Tích hợp hệ thống gợi ý cá nhân hóa và quản lý đánh giá có hình ảnh', 'FUTURE', '2026-11-06 08:00:00', '2026-11-25 18:00:00', NOW()),

-- Space 6 (Mobile Banking)
(11, 6, 'Sprint 1: Core Banking, eKYC & Xác thực Sinh trắc học FaceID', 'Xác thực khuôn mặt sinh trắc học và chuyển khoản nhanh Napas 247', 'ACTIVE', '2026-10-10 08:00:00', '2026-11-05 18:00:00', NOW()),

-- Space 9 (AI Research Lab)
(12, 9, 'Sprint 1: Huấn luyện Embedding & Vector Store PGVector', 'Tối ưu hóa độ chính xác truy xuất ngữ nghĩa RAG trên tài liệu tiếng Việt', 'ACTIVE', '2026-10-10 08:00:00', '2026-11-05 18:00:00', NOW())
ON CONFLICT (id) DO NOTHING;

-- 2.6. Chèn 42 Tasks mẫu chi tiết (Phủ đầy đủ 4 trạng thái: TODO, IN_PROGRESS, IN_REVIEW, DONE + Backlog)
INSERT INTO tasks (id, space_id, sprint_id, title, description, status, priority, owner_id, start_date, due_date, created_at) VALUES 
-- ==================== SPACE 1 - SPRINT 1 (CLOSED) ====================
(1, 1, 1, 'Thiết kế sơ đồ ERD & Chuẩn hóa Schema 4 Databases PostgreSQL', 'Thiết kế chi tiết bảng users, roles, workspaces, spaces, space_members, sprints, tasks, ai_threads và notifications.', 'DONE', 'URGENT', 2, '2026-07-01 08:00:00', '2026-07-05 18:00:00', NOW()),
(2, 1, 1, 'Xây dựng Auth-Service & Bộ lọc bảo mật JWT Gateway', 'Triển khai đăng ký, đăng nhập BCrypt và xác thực JWT Token tại API Gateway.', 'DONE', 'HIGH', 8, '2026-07-06 08:00:00', '2026-07-11 18:00:00', NOW()),
(3, 1, 1, 'Cấu hình Docker Compose cho 6 Microservices & Redis', 'Đóng gói container PostgreSQL, Redis, Eureka Server, API Gateway, Auth, Workspace, AI và Notification Service.', 'DONE', 'HIGH', 11, '2026-07-10 08:00:00', '2026-07-15 18:00:00', NOW()),
(4, 1, 1, 'Tích hợp Spring Cloud OpenFeign giao tiếp liên dịch vụ', 'Kết nối Feign Client từ AI-Service sang Workspace-Service để truy xuất ngữ cảnh Space/Task.', 'DONE', 'MEDIUM', 6, '2026-07-15 08:00:00', '2026-07-20 18:00:00', NOW()),

-- ==================== SPACE 1 - SPRINT 2 (CLOSED) ====================
(5, 1, 2, 'Phát triển REST APIs CRUD Workspace, Space & Phân quyền 3 tầng', 'Thiết lập ma trận phân quyền Space: Lead PM / PM Phụ (role=2), ASSISTANT (role=4) và MEMBER (role=3).', 'DONE', 'URGENT', 8, '2026-07-21 08:00:00', '2026-07-28 18:00:00', NOW()),
(6, 1, 2, 'Xây dựng thuật toán Tư vấn Vai trò & Chuẩn hóa Job Title tự động', 'Phân tích hồ sơ kỹ năng của thành viên để gợi ý ai làm ASSISTANT, PM Phụ theo mảng hoặc MEMBER.', 'DONE', 'HIGH', 2, '2026-07-29 08:00:00', '2026-08-03 18:00:00', NOW()),
(7, 1, 2, 'Xây dựng Giao diện Sprint Accordion, Backlog & Thùng rác khôi phục', 'Hiển thị danh sách công việc theo từng Sprint, hỗ trợ xóa mềm (Soft Delete) và khôi phục từ thùng rác.', 'DONE', 'HIGH', 9, '2026-08-04 08:00:00', '2026-08-10 18:00:00', NOW()),
(8, 1, 2, 'Xây dựng Task Detail Drawer & Luồng tự nhận việc (Self-assign)', 'Cho phép MEMBER bấm nhận các task chưa có người phụ trách và cập nhật tiến độ thực thi.', 'DONE', 'MEDIUM', 4, '2026-08-11 08:00:00', '2026-08-15 18:00:00', NOW()),

-- ==================== SPACE 1 - SPRINT 3 (ACTIVE - Đầy đủ 4 cột TODO, IN_PROGRESS, IN_REVIEW, DONE) ====================
(9, 1, 3, 'Nâng cấp Bảng Kanban 4 Cột (Cần làm, Đang làm, Chờ duyệt, Đã xong)', 'Bổ sung cột IN_REVIEW (Chờ PM duyệt) vào giữa cột Đang làm và Đã xong để kiểm soát chất lượng.', 'DONE', 'URGENT', 9, '2026-10-15 08:00:00', '2026-10-20 18:00:00', NOW()),
(10, 1, 3, 'Cấu hình WebSocket STOMP phát đồng bộ trạng thái Task tức thì', 'Khi thành viên kéo thẻ sang cột Chờ duyệt hoặc PM bấm Duyệt Done, màn hình toàn nhóm tự động cập nhật.', 'DONE', 'HIGH', 6, '2026-10-18 08:00:00', '2026-10-24 18:00:00', NOW()),
(11, 1, 3, 'Triển khai luồng Member bấm Done tự động chuyển vào cột Chờ PM Duyệt (IN_REVIEW)', 'Đảm bảo thành viên báo cáo hoàn thành công việc sẽ vào hàng đợi IN_REVIEW để PM kiểm tra nghiệm thu.', 'IN_REVIEW', 'URGENT', 4, '2026-10-22 08:00:00', '2026-10-30 18:00:00', NOW()),
(12, 1, 3, 'Xây dựng Sơ đồ Tư duy Tiến độ (Progress Mindmap) & Cảnh báo Rủi ro', 'Trực quan hóa cây tiến độ dự án theo thời gian thực và làm nổi bật các nhánh có nguy cơ trễ hạn.', 'IN_REVIEW', 'HIGH', 9, '2026-10-23 08:00:00', '2026-10-31 18:00:00', NOW()),
(13, 1, 3, 'Viết kịch bản Kiểm thử Phân quyền RBAC & Nghiệm thu cột In Review', 'Kiểm tra đầy đủ các trường hợp: Member nộp bài -> In Review -> PM bấm Duyệt Done hoặc Yêu cầu sửa lại.', 'IN_REVIEW', 'HIGH', 5, '2026-10-24 08:00:00', '2026-11-01 18:00:00', NOW()),
(14, 1, 3, 'Tối ưu thuật toán Tự động Phân công (Auto-Assign) & Cân bằng tải nhân sự', 'Chấm điểm khớp chuyên môn (Frontend, Backend, AI, QA, DevOps) kết hợp cân bằng khối lượng công việc.', 'IN_PROGRESS', 'URGENT', 2, '2026-10-25 08:00:00', '2026-11-02 18:00:00', NOW()),
(15, 1, 3, 'Xây dựng Biểu đồ Gantt Timeline Lộ trình 52 Tuần thực tế', 'Hiển thị thanh tiến độ công việc theo lịch tuần/tháng với màu sắc phân biệt 4 trạng thái.', 'IN_PROGRESS', 'HIGH', 4, '2026-10-26 08:00:00', '2026-11-03 18:00:00', NOW()),
(16, 1, 3, 'Tích hợp Redis Caching tăng tốc truy vấn Danh sách Task & Thành viên', 'Giảm thời gian phản hồi API lấy chi tiết Space và danh sách thành viên xuống dưới 30ms.', 'IN_PROGRESS', 'MEDIUM', 6, '2026-10-27 08:00:00', '2026-11-04 18:00:00', NOW()),
(17, 1, 3, 'Tự động lăn (Rollover) các Task chưa hoàn thành khi đóng Sprint', 'Khi Sprint kết thúc, tự động chuyển các task TODO/IN_PROGRESS/IN_REVIEW sang Sprint kế tiếp kèm nhãn lưu vết.', 'TODO', 'HIGH', 8, '2026-10-29 08:00:00', '2026-11-04 18:00:00', NOW()),
(18, 1, 3, 'Soạn thảo tài liệu đặc tả SRS & Biên bản họp Sprint 3 cho AI Co-Pilot', 'Chuẩn bị tệp tài liệu chuẩn để ASSISTANT nạp vào AI Co-Pilot tạo danh sách task nháp.', 'TODO', 'MEDIUM', 7, '2026-10-30 08:00:00', '2026-11-05 18:00:00', NOW()),
(19, 1, 3, 'Thiết lập cảnh báo Email & Thông báo Real-time khi có Task chờ PM duyệt', 'Gửi thông báo tức thì cho Lead PM và PM Phụ ngay khi thành viên nộp task sang cột IN_REVIEW.', 'TODO', 'MEDIUM', NULL, '2026-10-31 08:00:00', '2026-11-05 18:00:00', NOW()),

-- ==================== SPACE 1 - SPRINT 4 (FUTURE) ====================
(20, 1, 4, 'Cấu hình Spring AI & Kho Vector PGVector cho AI-Service', 'Tích hợp mô hình LLM và kho tri thức RAG phục vụ phân rã công việc thông minh.', 'TODO', 'URGENT', 10, '2026-11-06 08:00:00', '2026-11-10 18:00:00', NOW()),
(21, 1, 4, 'Xây dựng Kho Tri thức RAG 12 Bộ dữ liệu mẫu dự án phần mềm', 'Biênman dữ liệu mẫu đa lĩnh vực (E-Commerce, Fintech, LMS, HRM, Healthcare) cho Similarity Search.', 'TODO', 'HIGH', 7, '2026-11-08 08:00:00', '2026-11-13 18:00:00', NOW()),
(22, 1, 4, 'Phát triển Requirement Agent bóc tách tài liệu SRS/PDF thành Task WBS', 'Hỗ trợ PM và ASSISTANT tải lên file PDF/DOCX để AI tự động đề xuất danh sách Sprint và Task nháp.', 'TODO', 'HIGH', 2, '2026-11-11 08:00:00', '2026-11-16 18:00:00', NOW()),
(23, 1, 4, 'Phát triển PM Agent phân tích điểm nghẽn & Dự báo rủi ro trễ hạn', 'Tổng hợp số lượng task đang kẹt ở IN_PROGRESS / IN_REVIEW để cảnh báo sớm cho PM.', 'TODO', 'HIGH', 10, '2026-11-14 08:00:00', '2026-11-18 18:00:00', NOW()),
(24, 1, 4, 'Phát triển Technical Advisor Agent gợi ý giải pháp xử lý Bug kỹ thuật', 'Hỗ trợ lập trình viên hỏi đáp giải pháp kiến trúc và sửa lỗi ngay trong từng Task.', 'TODO', 'MEDIUM', 4, '2026-11-16 08:00:00', '2026-11-20 18:00:00', NOW()),

-- ==================== SPACE 1 - SPRINT 5 (FUTURE) ====================
(25, 1, 5, 'Viết Unit Test & Integration Test cho Workspace-Service và AI-Service', 'Đảm bảo độ bao phủ kiểm thử trên 85% các luồng nghiệp vụ quan trọng.', 'TODO', 'HIGH', 5, '2026-11-21 08:00:00', '2026-11-26 18:00:00', NOW()),
(26, 1, 5, 'Thiết lập Pipeline CI/CD GitHub Actions & Kiểm tra bảo mật Container', 'Tự động hóa quy trình build, test và đóng gói Docker Image cho toàn bộ hệ thống.', 'TODO', 'MEDIUM', 11, '2026-11-25 08:00:00', '2026-11-30 18:00:00', NOW()),
(27, 1, 5, 'Hoàn thiện Quyển Báo cáo Khóa luận Tốt nghiệp & Slide Thuyết trình', 'Tổng hợp cơ sở lý thuyết, thiết kế hệ thống, sơ đồ phân quyền RBAC và kết quả thực nghiệm.', 'TODO', 'URGENT', 2, '2026-11-28 08:00:00', '2026-12-03 18:00:00', NOW()),
(28, 1, 5, 'Quay Video Demo kịch bản End-to-End & Chuẩn bị Bảo vệ trước Hội đồng', 'Demo trực tiếp luồng: Tạo Space -> AI phân rã -> Tự động phân công -> Member nộp In Review -> PM duyệt Done.', 'TODO', 'URGENT', 8, '2026-12-01 08:00:00', '2026-12-05 18:00:00', NOW()),

-- ==================== SPACE 1 - BACKLOG (Chưa gắn vào Sprint) ====================
(29, 1, NULL, 'Nghiên cứu tính năng Xuất báo cáo tiến độ Sprint ra file Excel & PDF', 'Cho phép PM và ASSISTANT xuất bảng tổng hợp kết quả Sprint phục vụ báo cáo tuần.', 'TODO', 'LOW', NULL, NULL, NULL, NOW()),
(30, 1, NULL, 'Tích hợp Webhook thông báo tự động qua Discord / Slack của nhóm dự án', 'Gửi thông báo vào kênh chat nhóm khi có công việc chuyển sang trạng thái Chờ duyệt (IN_REVIEW).', 'TODO', 'LOW', NULL, NULL, NULL, NOW()),

-- ==================== SPACE 2 - KÝ TÚC XÁ SINH VIÊN IUH ====================
(31, 2, 6, 'Thiết kế CSDL Quản lý Tòa nhà, Phòng ở và Giường nội trú', 'Quản lý danh sách phòng nam/nữ, sức chứa và trạng thái giường trống.', 'DONE', 'HIGH', 6, '2026-08-01 08:00:00', '2026-08-10 18:00:00', NOW()),
(32, 2, 7, 'Xây dựng API tính tiền Điện Nước theo chỉ số đồng hồ hàng tháng', 'Tự động tính bậc thang giá điện nước cho từng phòng ký túc xá.', 'IN_REVIEW', 'URGENT', 6, '2026-10-12 08:00:00', '2026-10-28 18:00:00', NOW()),
(33, 2, 7, 'Thiết kế giao diện Sinh viên tra cứu hóa đơn & Thanh toán VietQR', 'Hiển thị mã QR động để sinh viên quét thanh toán phí nội trú và điện nước.', 'IN_PROGRESS', 'HIGH', 9, '2026-10-18 08:00:00', '2026-11-02 18:00:00', NOW()),
(34, 2, 7, 'Phân tích quy trình duyệt đơn xin chuyển phòng & trả phòng trực tuyến', 'Mô tả luồng nghiệp vụ cho Ban quản lý KTX xét duyệt đơn của sinh viên.', 'TODO', 'MEDIUM', 7, '2026-10-25 08:00:00', '2026-11-05 18:00:00', NOW()),

-- ==================== SPACE 4 - SÀN THƯƠNG MẠI ĐIỆN TỬ MICROSERVICES ====================
(35, 4, 8, 'Thiết kế Data Schema cho Product Catalog & Biến thể sản phẩm (SKU)', 'Tạo bảng products, categories, brands, skus và tồn kho.', 'DONE', 'HIGH', 4, '2026-07-01 08:00:00', '2026-07-10 18:00:00', NOW()),
(36, 4, 8, 'Phát triển ElasticSearch Indexing Service tìm kiếm tiếng Việt không dấu', 'Đồng bộ dữ liệu sản phẩm lên ElasticSearch hỗ trợ gợi ý từ khóa.', 'DONE', 'URGENT', 6, '2026-07-11 08:00:00', '2026-07-22 18:00:00', NOW()),
(37, 4, 9, 'Xây dựng cơ chế Distributed Lock bằng Redis chống bán lố (Overselling) Flash Sale', 'Đảm bảo chính xác tồn kho khi hàng ngàn người dùng bấm mua cùng lúc.', 'IN_REVIEW', 'URGENT', 6, '2026-10-14 08:00:00', '2026-10-29 18:00:00', NOW()),
(38, 4, 9, 'Tích hợp cổng thanh toán VNPAY & Ví điện tử MoMo IPN Webhook', 'Xử lý callback xác nhận giao dịch thành công và tự động cập nhật trạng thái đơn hàng.', 'IN_REVIEW', 'HIGH', 4, '2026-10-16 08:00:00', '2026-10-30 18:00:00', NOW()),
(39, 4, 9, 'Xây dựng trang Checkout đa bước & Áp dụng mã giảm giá Voucher', 'Giao diện thanh toán trực quan, tự động tính phí vận chuyển và khuyến mãi.', 'IN_PROGRESS', 'HIGH', 9, '2026-10-22 08:00:00', '2026-11-03 18:00:00', NOW()),
(40, 4, 9, 'Kiểm thử hiệu năng chịu tải (Load Testing JMeter) cho luồng Đặt hàng', 'Kiểm thử 1.000 CCU đồng thời trên API tạo đơn hàng.', 'TODO', 'MEDIUM', 12, '2026-10-28 08:00:00', '2026-11-05 18:00:00', NOW()),

-- ==================== SPACE 6 - APP MOBILE BANKING ====================
(41, 6, 11, 'Tích hợp SDK nhận diện khuôn mặt sinh trắc học (Liveness Detection)', 'Chống giả mạo hình ảnh/video khi xác thực giao dịch chuyển tiền trên 10 triệu đồng.', 'IN_REVIEW', 'URGENT', 2, '2026-10-12 08:00:00', '2026-10-30 18:00:00', NOW()),
(42, 6, 11, 'Xây dựng dịch vụ mã hóa OTP & Chữ ký số Digital Signature giao dịch', 'Bảo mật hai lớp chuẩn ngân hàng nhà nước cho mọi giao dịch chuyển khoản.', 'IN_PROGRESS', 'URGENT', 8, '2026-10-18 08:00:00', '2026-11-04 18:00:00', NOW())
ON CONFLICT (id) DO NOTHING;

-- 2.7. Chèn 12 Task Notes (Bình luận / Báo cáo nộp chờ PM duyệt trên các Task)
INSERT INTO task_notes (id, task_id, author_id, note_content, created_at) VALUES 
(1, 9, 9, 'Đã hoàn thành giao diện 4 cột Kanban (Cần làm, Đang làm, Chờ duyệt, Đã xong) và hiệu ứng kéo thả mượt mà.', NOW() - INTERVAL '3 days'),
(2, 9, 2, 'Lead PM đã kiểm tra giao diện và duyệt sang ĐÃ XONG (DONE). Thiết kế rất trực quan!', NOW() - INTERVAL '2 days'),
(3, 10, 6, 'Đã cấu hình xong STOMP WebSocket Broker trên cổng 8082, đồng bộ real-time dưới 50ms.', NOW() - INTERVAL '2 days'),
(4, 11, 4, 'Em đã hoàn thành logic tự động chuyển sang cột CHỜ DUYỆT (IN_REVIEW) khi Member bấm hoàn thành. Nhờ anh Sơn/anh Khánh (PM) kiểm tra và duyệt giúp em ạ!', NOW() - INTERVAL '6 hours'),
(5, 12, 9, 'Đã hoàn thiện Sơ đồ Mindmap hiển thị đầy đủ trạng thái Chờ duyệt. Đã chuyển sang cột IN_REVIEW chờ PM nghiệm thu.', NOW() - INTERVAL '4 hours'),
(6, 13, 5, 'Đã viết xong 15 kịch bản test case cho ma trận phân quyền PM / ASSISTANT / MEMBER. Đang chờ PM duyệt chốt.', NOW() - INTERVAL '2 hours'),
(7, 14, 2, 'Đang tinh chỉnh thêm trọng số ưu tiên cho các thành viên ít việc hơn trong thuật toán Auto-Assign.', NOW() - INTERVAL '1 hour'),
(8, 37, 6, 'Đã xử lý xong Redis Redlock cho Flash Sale, đã đẩy PR và chuyển task sang IN_REVIEW chờ chị Lân Anh duyệt.', NOW() - INTERVAL '5 hours'),
(9, 38, 4, 'Webhook VNPAY & MoMo đã chạy ổn định trên môi trường Sandbox, chờ PM kiểm tra duyệt sang DONE.', NOW() - INTERVAL '3 hours'),
(10, 41, 2, 'Mô hình Liveness Detection đạt độ chính xác 99.2%, đã chuyển sang IN_REVIEW để Lead PM Đăng Khánh nghiệm thu.', NOW() - INTERVAL '2 hours')
ON CONFLICT (id) DO NOTHING;

-- 2.8. Chèn 10 Workspace Logs (Lịch sử hoạt động & Luồng duyệt công việc)
INSERT INTO workspace_logs (id, workspace_id, task_id, user_id, action_type, old_value, new_value, log_message, created_at) VALUES 
(1, 1, 9, 9, 'SUBMIT_FOR_REVIEW', 'IN_PROGRESS', 'IN_REVIEW', 'Phạm Hoàng Nam (MEMBER) đã báo cáo hoàn thành Task #9 và chuyển sang cột CHỜ DUYỆT (IN_REVIEW)', NOW() - INTERVAL '3 days'),
(2, 1, 9, 2, 'PM_APPROVE_DONE', 'IN_REVIEW', 'DONE', 'Lưu Ngọc Cao Sơn (Lead PM) đã nghiệm thu và duyệt Task #9 sang trạng thái ĐÃ XONG (DONE)', NOW() - INTERVAL '2 days'),
(3, 1, 10, 8, 'PM_APPROVE_DONE', 'IN_REVIEW', 'DONE', 'Trần Đỗ Đăng Khánh (PM Backend) đã duyệt hoàn thành Task #10 sang trạng thái ĐÃ XONG (DONE)', NOW() - INTERVAL '2 days'),
(4, 1, 11, 4, 'SUBMIT_FOR_REVIEW', 'IN_PROGRESS', 'IN_REVIEW', 'Trần Duy (MEMBER) đã hoàn thành Task #11 và nộp sang cột CHỜ DUYỆT (IN_REVIEW) chờ PM kiểm tra', NOW() - INTERVAL '6 hours'),
(5, 1, 12, 9, 'SUBMIT_FOR_REVIEW', 'IN_PROGRESS', 'IN_REVIEW', 'Phạm Hoàng Nam (MEMBER) đã nộp Task #12 sang cột CHỜ DUYỆT (IN_REVIEW)', NOW() - INTERVAL '4 hours'),
(6, 1, 13, 5, 'SUBMIT_FOR_REVIEW', 'IN_PROGRESS', 'IN_REVIEW', 'Nguyễn Hòa (ASSISTANT) đã nộp Task #13 sang cột CHỜ DUYỆT (IN_REVIEW) để PM duyệt cuối cùng', NOW() - INTERVAL '2 hours'),
(7, 2, 37, 6, 'SUBMIT_FOR_REVIEW', 'IN_PROGRESS', 'IN_REVIEW', 'Vũ Minh Trí (MEMBER) đã nộp Task #37 sang cột CHỜ DUYỆT (IN_REVIEW)', NOW() - INTERVAL '5 hours'),
(8, 2, 38, 4, 'SUBMIT_FOR_REVIEW', 'IN_PROGRESS', 'IN_REVIEW', 'Trần Duy (MEMBER) đã nộp Task #38 sang cột CHỜ DUYỆT (IN_REVIEW)', NOW() - INTERVAL '3 hours')
ON CONFLICT (id) DO NOTHING;

-- Reset sequences cho proga_workspace_db
SELECT setval(pg_get_serial_sequence('workspaces', 'id'), COALESCE(MAX(id), 1)) FROM workspaces;
SELECT setval(pg_get_serial_sequence('spaces', 'id'), COALESCE(MAX(id), 1)) FROM spaces;
SELECT setval(pg_get_serial_sequence('sprints', 'id'), COALESCE(MAX(id), 1)) FROM sprints;
SELECT setval(pg_get_serial_sequence('tasks', 'id'), COALESCE(MAX(id), 1)) FROM tasks;
SELECT setval(pg_get_serial_sequence('task_notes', 'id'), COALESCE(MAX(id), 1)) FROM task_notes;
SELECT setval(pg_get_serial_sequence('workspace_logs', 'id'), COALESCE(MAX(id), 1)) FROM workspace_logs;


-- =============================================================================
-- 3. CONNECT TO DATABASE: proga_ai_db
-- =============================================================================
\c proga_ai_db;

CREATE TABLE IF NOT EXISTS ai_threads (
    id BIGSERIAL PRIMARY KEY,
    openai_thread_id VARCHAR(100),
    space_id BIGINT NOT NULL,
    user_id BIGINT,
    agent_type VARCHAR(30) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ai_chat_messages (
    id BIGSERIAL PRIMARY KEY,
    thread_id BIGINT NOT NULL,
    sender_type VARCHAR(20) NOT NULL,
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

TRUNCATE TABLE ai_chat_messages, ai_threads, ai_knowledge_samples RESTART IDENTITY CASCADE;

INSERT INTO ai_threads (id, openai_thread_id, space_id, user_id, agent_type, created_at) VALUES
(1, 'thread_space1_req_001', 1, 2, 'REQUIREMENT', NOW() - INTERVAL '5 days'),
(2, 'thread_space1_pm_002', 1, 2, 'PROJECT_MANAGER', NOW() - INTERVAL '3 days'),
(3, 'thread_space1_tech_003', 1, 8, 'TECHNICAL_ADVISOR', NOW() - INTERVAL '1 day')
ON CONFLICT (id) DO NOTHING;

INSERT INTO ai_chat_messages (id, thread_id, sender_type, message_content, json_payload, created_at) VALUES
(1, 1, 'USER', 'Hãy phân rã các công việc cho Sprint 3: Bảng Kanban 4 cột (có cột In Review chờ PM duyệt) và đồng bộ WebSocket.', NULL, NOW() - INTERVAL '5 days'),
(2, 1, 'ASSISTANT', 'Tôi đã phân tích yêu cầu và bóc tách thành các công việc WBS cho Frontend, Backend và QA theo chuẩn Agile/Scrum, bao gồm luồng kiểm duyệt chất lượng qua cột IN_REVIEW.', NULL, NOW() - INTERVAL '5 days'),
(3, 2, 'USER', 'Tóm tắt tình hình tiến độ Sprint 3 hiện tại và các công việc đang chờ PM duyệt.', NULL, NOW() - INTERVAL '2 hours'),
(4, 2, 'ASSISTANT', 'Báo cáo Sprint 3: Hiện có 2 công việc ĐÃ XONG (DONE), 3 công việc đã được thành viên hoàn thành và đang nằm ở cột CHỜ DUYỆT (IN_REVIEW) gồm Task #11, #12, #13. Đề xuất Lead PM và PM Phụ kiểm tra nghiệm thu sớm để thông luồng tiến độ.', NULL, NOW() - INTERVAL '2 hours')
ON CONFLICT (id) DO NOTHING;

INSERT INTO ai_knowledge_samples (id, space_id, title, domain, requirement_text, summary, tasks_json, created_at) VALUES
(1, 1, 'Hệ thống Quản lý Dự án Thông minh tích hợp Multi-Agent AI & RBAC (IUH-PROGA)', 'Project Management / AI SaaS', 'Xây dựng hệ thống quản lý dự án phần mềm theo mô hình Agile/Scrum với 4 cột Kanban (TODO, IN_PROGRESS, IN_REVIEW, DONE), phân quyền 3 tầng (PM, ASSISTANT, MEMBER) và tích hợp AI Co-Pilot.', 'Mẫu tri thức chuẩn cho hệ thống quản lý dự án phần mềm Agile/Scrum tích hợp AI.', '[]', NOW())
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('ai_threads', 'id'), COALESCE(MAX(id), 1)) FROM ai_threads;
SELECT setval(pg_get_serial_sequence('ai_chat_messages', 'id'), COALESCE(MAX(id), 1)) FROM ai_chat_messages;
SELECT setval(pg_get_serial_sequence('ai_knowledge_samples', 'id'), COALESCE(MAX(id), 1)) FROM ai_knowledge_samples;


-- =============================================================================
-- 4. CONNECT TO DATABASE: proga_notification_db
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
    type_id SMALLINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

TRUNCATE TABLE notifications, notification_types RESTART IDENTITY CASCADE;

INSERT INTO notification_types (id, code, label) VALUES
(1, 'TASK_ASSIGNED', 'Giao việc mới'),
(2, 'TASK_IN_REVIEW', 'Công việc chờ PM duyệt (In Review)'),
(3, 'TASK_APPROVED_DONE', 'PM đã duyệt hoàn thành (Done)'),
(4, 'WORKSPACE_INVITE', 'Lời mời tham gia Workspace'),
(5, 'DEADLINE_WARNING', 'Cảnh báo sắp tới hạn chót')
ON CONFLICT (id) DO NOTHING;

INSERT INTO notifications (id, user_id, workspace_id, title, content, is_read, type_id, created_at) VALUES
(1, 2, 1, '📋 Có 3 công việc mới đang chờ PM duyệt (IN REVIEW)', 'Trần Duy, Phạm Hoàng Nam và Nguyễn Hòa vừa hoàn thành công việc (#11, #12, #13) và chuyển sang cột CHỜ DUYỆT. Vui lòng kiểm tra và bấm Duyệt Done!', false, 2, NOW() - INTERVAL '2 hours'),
(2, 8, 1, '📋 Công việc #11 đang chờ PM nghiệm thu', 'Trần Duy (MEMBER) đã nộp báo cáo hoàn thành cho Task #11: "Triển khai luồng Member bấm Done tự động chuyển vào cột Chờ PM Duyệt".', false, 2, NOW() - INTERVAL '3 hours'),
(3, 9, 1, '✅ PM đã duyệt hoàn thành Task #9', 'Lead PM Lưu Ngọc Cao Sơn đã kiểm tra và duyệt Task #9 của bạn sang trạng thái ĐÃ XONG (DONE).', false, 3, NOW() - INTERVAL '1 day'),
(4, 6, 1, '✅ PM đã duyệt hoàn thành Task #10', 'PM Trần Đỗ Đăng Khánh đã kiểm tra và duyệt Task #10 của bạn sang trạng thái ĐÃ XONG (DONE).', true, 3, NOW() - INTERVAL '2 days'),
(5, 4, 1, '📌 Bạn được giao công việc mới trong Sprint 3', 'Bạn đã được phân công thực hiện Task #15: "Xây dựng Biểu đồ Gantt Timeline Lộ trình 52 Tuần thực tế".', false, 1, NOW() - INTERVAL '1 day'),
(6, 7, 1, '🤝 Cấp quyền Trợ lý Điều phối (ASSISTANT)', 'Bạn đã được cấp quyền ASSISTANT trong dự án "Dự Án Khóa Luận Tốt Nghiệp IUH-PROGA". Bạn có thể hỗ trợ nạp tài liệu AI phân rã nháp và điều phối tiến độ Sprint.', false, 1, NOW() - INTERVAL '2 days')
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('notification_types', 'id'), COALESCE(MAX(id), 1)) FROM notification_types;
SELECT setval(pg_get_serial_sequence('notifications', 'id'), COALESCE(MAX(id), 1)) FROM notifications;
