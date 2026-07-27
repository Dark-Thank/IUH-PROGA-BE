-- =============================================================================
-- SEED DATA FOR IUH-PROGA SYSTEM (Long ID datatype migration)
-- =============================================================================

-- =============================================================================
-- 1. CONNECT TO DATABASE: proga_auth_db
-- =============================================================================
\c proga_auth_db;

-- Chèn dữ liệu Roles
INSERT INTO roles (id, name) VALUES 
(1, 'ADMIN'),
(2, 'PM'),
(3, 'MEMBER')
ON CONFLICT (id) DO NOTHING;

-- Chèn 5 Users (Mật khẩu mặc định của tất cả user: password123)
INSERT INTO users (id, username, email, password, is_admin, created_at) VALUES 
(1, 'admin_user', 'admin@proga.iuh.edu.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', true, NOW()),
(2, 'pm_sonluu', 'sonluu@proga.iuh.edu.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', false, NOW()),
(3, 'pm_lananh', 'lananh@proga.iuh.edu.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', false, NOW()),
(4, 'dev_duy', 'duy@proga.iuh.edu.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', false, NOW()),
(5, 'tester_hoa', 'hoa@proga.iuh.edu.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', false, NOW())
ON CONFLICT (id) DO NOTHING;

-- Reset sequences cho proga_auth_db để tránh lỗi trùng khóa khi insert tự động sau này
SELECT setval(pg_get_serial_sequence('roles', 'id'), COALESCE(MAX(id), 1)) FROM roles;
SELECT setval(pg_get_serial_sequence('users', 'id'), COALESCE(MAX(id), 1)) FROM users;


-- =============================================================================
-- 2. CONNECT TO DATABASE: proga_workspace_db
-- =============================================================================
\c proga_workspace_db;

-- Chèn dữ liệu Workspaces
INSERT INTO workspaces (id, name, description, owner_id, created_at) VALUES 
(1, 'Dự Án Quản Lý Tiến Độ IUH-PROGA', 'Hệ thống quản lý tiến độ dự án tích hợp AI Agents cho KLTN IUH', 2, NOW()),
(2, 'Dự Án E-Commerce Microservices', 'Hệ thống thương mại điện tử kiến trúc Microservices', 3, NOW())
ON CONFLICT (id) DO NOTHING;

-- Chèn dữ liệu Project Members (Thành viên trong dự án)
INSERT INTO project_members (workspace_id, user_id, role_id, joined_at) VALUES 
(1, 2, 2, NOW()), -- pm_sonluu là PM
(1, 4, 3, NOW()), -- dev_duy là MEMBER
(1, 5, 3, NOW()), -- tester_hoa là MEMBER
(2, 3, 2, NOW())  -- pm_lananh là PM
ON CONFLICT (workspace_id, user_id, role_id) DO NOTHING;

-- Chèn dữ liệu Spaces (Giai đoạn/Sprint)
INSERT INTO spaces (id, workspace_id, name, start_date, end_date, created_at) VALUES 
(1, 1, 'Sprint 1 - Foundation & Microservices Core', '2026-07-01 08:00:00', '2026-07-15 18:00:00', NOW()),
(2, 1, 'Sprint 2 - Kanban Board & AI Agent Integration', '2026-07-16 08:00:00', '2026-07-31 18:00:00', NOW())
ON CONFLICT (id) DO NOTHING;

-- Chèn dữ liệu Tasks (Các công việc)
INSERT INTO tasks (id, space_id, title, description, status, priority, owner_id, start_date, due_date, created_at) VALUES 
(1, 1, 'Thiết kế sơ đồ ERD & PostgreSQL Schema', 'Xác định các bảng users, workspaces, spaces, tasks và mối quan hệ giữa chúng.', 'DONE', 'HIGH', 2, '2026-07-01 08:00:00', '2026-07-05 18:00:00', NOW()),
(2, 1, 'Xây dựng Auth-Service & Cấu hình Security JWT', 'Triển khai đăng ký, đăng nhập và cấp JWT Token cho toàn hệ thống.', 'IN_PROGRESS', 'URGENT', 4, '2026-07-06 08:00:00', '2026-07-12 18:00:00', NOW()),
(3, 2, 'Xây dựng Giao diện Kanban Board kéo thả', 'Triển khai bảng Kanban trên Web Frontend dùng Next.js & Tailwind CSS.', 'TODO', 'MEDIUM', 4, '2026-07-16 08:00:00', '2026-07-22 18:00:00', NOW()),
(4, 2, 'Tích hợp Requirement Agent tự động phân rã Task', 'Gọi OpenAI Assistant API để tự động gợi ý danh sách task nhỏ từ yêu cầu bài toán.', 'TODO', 'HIGH', 5, '2026-07-23 08:00:00', '2026-07-30 18:00:00', NOW())
ON CONFLICT (id) DO NOTHING;

-- Chèn dữ liệu Task Notes (Ghi chú/Bình luận trên Task)
INSERT INTO task_notes (id, task_id, author_id, note_content, created_at) VALUES 
(1, 2, 4, 'Đã hoàn thành phần JWT TokenProvider và CustomUserDetailsService.', NOW()),
(2, 2, 2, 'Cần chú ý xử lý Exception khi token bị hết hạn nhé.', NOW())
ON CONFLICT (id) DO NOTHING;

-- Chèn dữ liệu Workspace Logs (Lịch sử hoạt động)
INSERT INTO workspace_logs (id, workspace_id, task_id, user_id, action_type, old_value, new_value, log_message, created_at) VALUES 
(1, 1, 2, 4, 'STATUS_CHANGE', 'TODO', 'IN_PROGRESS', 'Người dùng dev_duy đã chuyển trạng thái Task sang IN_PROGRESS', NOW())
ON CONFLICT (id) DO NOTHING;

-- Reset sequences cho proga_workspace_db để tránh lỗi trùng khóa khi insert tự động sau này
SELECT setval(pg_get_serial_sequence('workspaces', 'id'), COALESCE(MAX(id), 1)) FROM workspaces;
SELECT setval(pg_get_serial_sequence('spaces', 'id'), COALESCE(MAX(id), 1)) FROM spaces;
SELECT setval(pg_get_serial_sequence('tasks', 'id'), COALESCE(MAX(id), 1)) FROM tasks;
SELECT setval(pg_get_serial_sequence('task_notes', 'id'), COALESCE(MAX(id), 1)) FROM task_notes;
SELECT setval(pg_get_serial_sequence('workspace_logs', 'id'), COALESCE(MAX(id), 1)) FROM workspace_logs;
