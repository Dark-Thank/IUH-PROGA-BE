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

-- Chèn 7 Users với thông tin hồ sơ chi tiết (Mật khẩu mặc định: password123)
INSERT INTO users (id, username, email, password, avatar_url, phone_number, full_name, display_name, job_title, bio, is_admin, created_at) VALUES 
(1, 'admin_user', 'admin@proga.iuh.edu.vn', '$2a$10$QUC2khs76FWt7FUpdiDX1.09bAEuyQ4ptkdcrQwYqx.ec4ZEqgWvS', 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin', '0901234567', 'Hệ Thống Admin', 'Quản Trị Viên', 'System Administrator', 'Quản trị viên hệ thống IUH-PROGA', true, NOW()),
(2, 'pm_sonluu', 'sonluu@proga.iuh.edu.vn', '$2a$10$QUC2khs76FWt7FUpdiDX1.09bAEuyQ4ptkdcrQwYqx.ec4ZEqgWvS', 'https://api.dicebear.com/7.x/avataaars/svg?seed=sonluu', '0912345678', 'Sơn Lưu', 'Sơn Lưu (PM)', 'Project Manager', 'Quản lý dự án KLTN IUH-PROGA', false, NOW()),
(3, 'pm_lananh', 'lananh@proga.iuh.edu.vn', '$2a$10$QUC2khs76FWt7FUpdiDX1.09bAEuyQ4ptkdcrQwYqx.ec4ZEqgWvS', 'https://api.dicebear.com/7.x/avataaars/svg?seed=lananh', '0923456789', 'Lân Anh', 'Lan Anh (PM)', 'Scrum Master', 'Quản lý dự án E-Commerce', false, NOW()),
(4, 'dev_duy', 'duy@proga.iuh.edu.vn', '$2a$10$QUC2khs76FWt7FUpdiDX1.09bAEuyQ4ptkdcrQwYqx.ec4ZEqgWvS', 'https://api.dicebear.com/7.x/avataaars/svg?seed=duy', '0934567890', 'Trần Duy', 'Duy Dev', 'Fullstack Developer', 'Lập trình viên React & Java', false, NOW()),
(5, 'tester_hoa', 'hoa@proga.iuh.edu.vn', '$2a$10$QUC2khs76FWt7FUpdiDX1.09bAEuyQ4ptkdcrQwYqx.ec4ZEqgWvS', 'https://api.dicebear.com/7.x/avataaars/svg?seed=hoa', '0945678901', 'Nguyễn Hòa', 'Hoa Tester', 'QA Engineer', 'Kỹ sư kiểm thử chất lượng phần mềm', false, NOW()),
(6, 'dev_minhtri', 'tri@proga.iuh.edu.vn', '$2a$10$QUC2khs76FWt7FUpdiDX1.09bAEuyQ4ptkdcrQwYqx.ec4ZEqgWvS', 'https://api.dicebear.com/7.x/avataaars/svg?seed=minhtri', '0956789012', 'Vũ Minh Trí', 'Trí Backend', 'Backend Developer', 'Lập trình viên Java Spring Boot Microservices', false, NOW()),
(7, 'ba_tuan', 'tuan@proga.iuh.edu.vn', '$2a$10$QUC2khs76FWt7FUpdiDX1.09bAEuyQ4ptkdcrQwYqx.ec4ZEqgWvS', 'https://api.dicebear.com/7.x/avataaars/svg?seed=batuan', '0967890123', 'Lê Anh Tuấn', 'Tuấn BA', 'Business Analyst', 'Chuyên viên phân tích yêu cầu nghiệp vụ', false, NOW())
ON CONFLICT (id) DO NOTHING;

-- Reset sequences cho proga_auth_db
SELECT setval(pg_get_serial_sequence('roles', 'id'), COALESCE(MAX(id), 1)) FROM roles;
SELECT setval(pg_get_serial_sequence('users', 'id'), COALESCE(MAX(id), 1)) FROM users;


-- =============================================================================
-- 2. CONNECT TO DATABASE: proga_workspace_db
-- =============================================================================
\c proga_workspace_db;

-- Chèn 5 Workspaces mẫu
INSERT INTO workspaces (id, name, description, owner_id, created_at) VALUES 
(1, 'Dự Án Quản Lý Tiến Độ IUH-PROGA', 'Hệ thống quản lý tiến độ dự án tích hợp AI Agents cho KLTN IUH', 2, NOW()),
(2, 'Dự Án E-Commerce Microservices', 'Hệ thống thương mại điện tử kiến trúc Microservices', 3, NOW()),
(3, 'Dự Án AI Chatbot & Customer Service', 'Hệ thống trợ lý ảo chăm sóc khách hàng bằng LLM', 2, NOW()),
(4, 'Dự Án Mobile App Banking', 'Ứng dụng ngân hàng số đa nền thực thi mã hóa sinh trắc học', 3, NOW()),
(5, 'Dự Án HRM Quản Nhân Sự', 'Hệ thống quản lý nguồn nhân lực và chấm công thông minh', 2, NOW())
ON CONFLICT (id) DO NOTHING;

-- Chèn 8 Workspace Members (Bảng workspace_members) với trạng thái ACCEPTED & PENDING
INSERT INTO workspace_members (workspace_id, user_id, role_id, status, joined_at) VALUES 
(1, 2, 2, 'ACCEPTED', NOW()), -- pm_sonluu (PM/Owner)
(1, 4, 3, 'ACCEPTED', NOW()), -- dev_duy (MEMBER ACCEPTED)
(1, 5, 3, 'ACCEPTED', NOW()), -- tester_hoa (MEMBER ACCEPTED)
(1, 6, 3, 'PENDING', NOW()),  -- dev_minhtri (LỜI MỜI PENDING)
(1, 7, 3, 'PENDING', NOW()),  -- ba_tuan (LỜI MỜI PENDING)
(2, 3, 2, 'ACCEPTED', NOW()), -- pm_lananh (PM/Owner)
(2, 4, 3, 'ACCEPTED', NOW()), -- dev_duy (MEMBER ACCEPTED)
(2, 6, 3, 'PENDING', NOW())   -- dev_minhtri (LỜI MỜI PENDING)
ON CONFLICT (workspace_id, user_id) DO NOTHING;

-- Chèn 5 Spaces mẫu (Space 3 là Private `is_private = true`)
INSERT INTO spaces (id, workspace_id, name, start_date, end_date, is_private, created_at) VALUES 
(1, 1, 'Development Space 1', '2026-07-01 08:00:00', '2026-07-31 18:00:00', false, NOW()),
(2, 1, 'Development Space 2', '2026-08-01 08:00:00', '2026-08-31 18:00:00', false, NOW()),
(3, 1, 'AI Research & R&D Space (Private)', '2026-09-01 08:00:00', '2026-09-30 18:00:00', true, NOW()),
(4, 2, 'Backend Services Space', '2026-07-01 08:00:00', '2026-07-31 18:00:00', false, NOW()),
(5, 2, 'UI/UX Design System Space', '2026-08-01 08:00:00', '2026-08-31 18:00:00', false, NOW())
ON CONFLICT (id) DO NOTHING;

-- Chèn 8 Space Members (Bảng space_members)
INSERT INTO space_members (space_id, user_id, role_id, joined_at) VALUES
(1, 2, 2, NOW()), -- pm_sonluu trong Space 1 (Role 2: Admin/PM)
(1, 4, 3, NOW()), -- dev_duy trong Space 1 (Role 3: Member)
(1, 5, 3, NOW()), -- tester_hoa trong Space 1 (Role 3: Member)
(1, 6, 3, NOW()), -- dev_minhtri trong Space 1 (Role 3: Member)
(2, 2, 2, NOW()), -- pm_sonluu trong Space 2 (Role 2: Admin/PM)
(2, 4, 3, NOW()), -- dev_duy trong Space 2 (Role 3: Member)
(2, 7, 3, NOW()), -- ba_tuan trong Space 2 (Role 3: Member)
(3, 2, 2, NOW())  -- pm_sonluu trong Space 3 (Role 2: Admin/PM)
ON CONFLICT (space_id, user_id) DO NOTHING;

-- Chèn 6 Sprints mẫu
INSERT INTO sprints (id, space_id, name, goal, status, start_date, end_date, created_at) VALUES 
(1, 1, 'Sprint 1', 'Foundation & Microservices Core', 'ACTIVE', '2026-07-01 08:00:00', '2026-07-15 18:00:00', NOW()),
(2, 1, 'Sprint 2', 'Kanban Board & AI Agent Integration', 'FUTURE', '2026-07-16 08:00:00', '2026-07-31 18:00:00', NOW()),
(3, 1, 'Sprint 3', 'Testing & Deployment Optimization', 'FUTURE', '2026-08-01 08:00:00', '2026-08-15 18:00:00', NOW()),
(4, 2, 'Sprint 1', 'User Management & Payment Gateway', 'ACTIVE', '2026-07-01 08:00:00', '2026-07-20 18:00:00', NOW()),
(5, 2, 'Sprint 2', 'Product Catalog & Search Engine', 'FUTURE', '2026-07-21 08:00:00', '2026-08-10 18:00:00', NOW()),
(6, 3, 'Sprint 1', 'Prompt Engineering & Fine-tuning LLM', 'ACTIVE', '2026-07-01 08:00:00', '2026-07-30 18:00:00', NOW())
ON CONFLICT (id) DO NOTHING;

-- Chèn 8 Tasks mẫu
INSERT INTO tasks (id, space_id, sprint_id, title, description, status, priority, owner_id, start_date, due_date, created_at) VALUES 
(1, 1, 1, 'Thiết kế sơ đồ ERD & PostgreSQL Schema', 'Xác định các bảng users, workspaces, spaces, sprints, tasks và mối quan hệ giữa chúng.', 'DONE', 'HIGH', 2, '2026-07-01 08:00:00', '2026-07-05 18:00:00', NOW()),
(2, 1, 1, 'Xây dựng Auth-Service & Cấu hình Security JWT', 'Triển khai đăng ký, đăng nhập và cấp JWT Token cho toàn hệ thống.', 'IN_PROGRESS', 'URGENT', 4, '2026-07-06 08:00:00', '2026-07-12 18:00:00', NOW()),
(3, 1, 2, 'Xây dựng Giao diện Kanban Board kéo thả', 'Triển khai bảng Kanban trên Web Frontend dùng Next.js & Tailwind CSS.', 'TODO', 'MEDIUM', 4, '2026-07-16 08:00:00', '2026-07-22 18:00:00', NOW()),
(4, 1, 2, 'Tích hợp Requirement Agent tự động phân rã Task', 'Gọi OpenAI Assistant API để tự động gợi ý danh sách task nhỏ từ yêu cầu bài toán.', 'TODO', 'HIGH', 5, '2026-07-23 08:00:00', '2026-07-30 18:00:00', NOW()),
(5, 1, 1, 'Tối ưu hóa performance API Gateway', 'Cấu hình Spring Cloud Gateway Caching & Rate Limiting.', 'DONE', 'MEDIUM', 6, '2026-07-07 08:00:00', '2026-07-14 18:00:00', NOW()),
(6, 1, 2, 'Thiết kế UI/UX Dark Mode cho Web', 'Xây dựng hệ thống Tailwind Design System hỗ trợ Dark Mode.', 'IN_PROGRESS', 'LOW', 4, '2026-07-17 08:00:00', '2026-07-25 18:00:00', NOW()),
(7, 1, 3, 'Viết Unit Test cho SprintService', 'Tạo JUnit 5 test cases bao phủ 85% logic quản lý Sprint.', 'TODO', 'LOW', 5, '2026-08-02 08:00:00', '2026-08-08 18:00:00', NOW()),
(8, 1, 1, 'Cấu hình Docker Compose Multi-container', 'Đóng gói 5 microservices và PostgreSQL, Redis bằng Docker Compose.', 'DONE', 'HIGH', 6, '2026-07-02 08:00:00', '2026-07-08 18:00:00', NOW())
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

-- Reset sequences cho proga_workspace_db
SELECT setval(pg_get_serial_sequence('workspaces', 'id'), COALESCE(MAX(id), 1)) FROM workspaces;
SELECT setval(pg_get_serial_sequence('spaces', 'id'), COALESCE(MAX(id), 1)) FROM spaces;
SELECT setval(pg_get_serial_sequence('sprints', 'id'), COALESCE(MAX(id), 1)) FROM sprints;
SELECT setval(pg_get_serial_sequence('tasks', 'id'), COALESCE(MAX(id), 1)) FROM tasks;
SELECT setval(pg_get_serial_sequence('task_notes', 'id'), COALESCE(MAX(id), 1)) FROM task_notes;
SELECT setval(pg_get_serial_sequence('workspace_logs', 'id'), COALESCE(MAX(id), 1)) FROM workspace_logs;
