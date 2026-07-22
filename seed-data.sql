-- =============================================================================
-- SEED DATA FOR IUH-PROGA SYSTEM
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
('a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d', 'admin_user', 'admin@proga.iuh.edu.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', true, NOW()),
('b2c3d4e5-f6a7-5b6c-9d0e-1f2a3b4c5d6e', 'pm_sonluu', 'sonluu@proga.iuh.edu.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', false, NOW()),
('c3d4e5f6-a7b8-6c7d-0e1f-2a3b4c5d6e7f', 'pm_lananh', 'lananh@proga.iuh.edu.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', false, NOW()),
('d4e5f6a7-b8c9-7d0e-1f2a-3b4c5d6e7f8a', 'dev_duy', 'duy@proga.iuh.edu.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', false, NOW()),
('e5f6a7b8-c9d0-8e1f-2a3b-4c5d6e7f8a9b', 'tester_hoa', 'hoa@proga.iuh.edu.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', false, NOW())
ON CONFLICT (id) DO NOTHING;


-- =============================================================================
-- 2. CONNECT TO DATABASE: proga_workspace_db
-- =============================================================================
\c proga_workspace_db;

-- Chèn dữ liệu Workspaces
INSERT INTO workspaces (id, name, description, owner_id, created_at) VALUES 
('11111111-2222-3333-4444-555555555555', 'Dự Án Quản Lý Tiến Độ IUH-PROGA', 'Hệ thống quản lý tiến độ dự án tích hợp AI Agents cho KLTN IUH', 'b2c3d4e5-f6a7-5b6c-9d0e-1f2a3b4c5d6e', NOW()),
('22222222-3333-4444-5555-666666666666', 'Dự Án E-Commerce Microservices', 'Hệ thống thương mại điện tử kiến trúc Microservices', 'c3d4e5f6-a7b8-6c7d-0e1f-2a3b4c5d6e7f', NOW())
ON CONFLICT (id) DO NOTHING;

-- Chèn dữ liệu Project Members (Thành viên trong dự án)
INSERT INTO project_members (workspace_id, user_id, role_id, joined_at) VALUES 
('11111111-2222-3333-4444-555555555555', 'b2c3d4e5-f6a7-5b6c-9d0e-1f2a3b4c5d6e', 2, NOW()), -- pm_sonluu là PM
('11111111-2222-3333-4444-555555555555', 'd4e5f6a7-b8c9-7d0e-1f2a-3b4c5d6e7f8a', 3, NOW()), -- dev_duy là MEMBER
('11111111-2222-3333-4444-555555555555', 'e5f6a7b8-c9d0-8e1f-2a3b-4c5d6e7f8a9b', 3, NOW()), -- tester_hoa là MEMBER
('22222222-3333-4444-5555-666666666666', 'c3d4e5f6-a7b8-6c7d-0e1f-2a3b4c5d6e7f', 2, NOW())  -- pm_lananh là PM
ON CONFLICT (workspace_id, user_id, role_id) DO NOTHING;

-- Chèn dữ liệu Spaces (Giai đoạn/Sprint)
INSERT INTO spaces (id, workspace_id, name, start_date, end_date, created_at) VALUES 
('aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee', '11111111-2222-3333-4444-555555555555', 'Sprint 1 - Foundation & Microservices Core', '2026-07-01 08:00:00', '2026-07-15 18:00:00', NOW()),
('bbbbbbbb-cccc-dddd-eeee-ffffffffffff', '11111111-2222-3333-4444-555555555555', 'Sprint 2 - Kanban Board & AI Agent Integration', '2026-07-16 08:00:00', '2026-07-31 18:00:00', NOW())
ON CONFLICT (id) DO NOTHING;

-- Chèn dữ liệu Tasks (Các công việc)
INSERT INTO tasks (id, space_id, title, description, status, priority, owner_id, start_date, due_date, created_at) VALUES 
('10101010-1010-1010-1010-101010101010', 'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee', 'Thiết kế sơ đồ ERD & PostgreSQL Schema', 'Xác định các bảng users, workspaces, spaces, tasks và mối quan hệ giữa chúng.', 'DONE', 'HIGH', 'b2c3d4e5-f6a7-5b6c-9d0e-1f2a3b4c5d6e', '2026-07-01 08:00:00', '2026-07-05 18:00:00', NOW()),
('20202020-2020-2020-2020-202020202020', 'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee', 'Xây dựng Auth-Service & Cấu hình Security JWT', 'Triển khai đăng ký, đăng nhập và cấp JWT Token cho toàn hệ thống.', 'IN_PROGRESS', 'URGENT', 'd4e5f6a7-b8c9-7d0e-1f2a-3b4c5d6e7f8a', '2026-07-06 08:00:00', '2026-07-12 18:00:00', NOW()),
('30303030-3030-3030-3030-303030303030', 'bbbbbbbb-cccc-dddd-eeee-ffffffffffff', 'Xây dựng Giao diện Kanban Board kéo thả', 'Triển khai bảng Kanban trên Web Frontend dùng Next.js & Tailwind CSS.', 'TODO', 'MEDIUM', 'd4e5f6a7-b8c9-7d0e-1f2a-3b4c5d6e7f8a', '2026-07-16 08:00:00', '2026-07-22 18:00:00', NOW()),
('40404040-4040-4040-4040-404040404040', 'bbbbbbbb-cccc-dddd-eeee-ffffffffffff', 'Tích hợp Requirement Agent tự động phân rã Task', 'Gọi OpenAI Assistant API để tự động gợi ý danh sách task nhỏ từ yêu cầu bài toán.', 'TODO', 'HIGH', 'e5f6a7b8-c9d0-8e1f-2a3b-4c5d6e7f8a9b', '2026-07-23 08:00:00', '2026-07-30 18:00:00', NOW())
ON CONFLICT (id) DO NOTHING;

-- Chèn dữ liệu Task Notes (Ghi chú/Bình luận trên Task)
INSERT INTO task_notes (id, task_id, author_id, note_content, created_at) VALUES 
(1, '20202020-2020-2020-2020-202020202020', 'd4e5f6a7-b8c9-7d0e-1f2a-3b4c5d6e7f8a', 'Đã hoàn thành phần JWT TokenProvider và CustomUserDetailsService.', NOW()),
(2, '20202020-2020-2020-2020-202020202020', 'b2c3d4e5-f6a7-5b6c-9d0e-1f2a3b4c5d6e', 'Cần chú ý xử lý Exception khi token bị hết hạn nhé.', NOW())
ON CONFLICT (id) DO NOTHING;

-- Chèn dữ liệu Workspace Logs (Lịch sử hoạt động)
INSERT INTO workspace_logs (id, workspace_id, task_id, user_id, action_type, old_value, new_value, log_message, created_at) VALUES 
(1, '11111111-2222-3333-4444-555555555555', '20202020-2020-2020-2020-202020202020', 'd4e5f6a7-b8c9-7d0e-1f2a-3b4c5d6e7f8a', 'STATUS_CHANGE', 'TODO', 'IN_PROGRESS', 'Người dùng dev_duy đã chuyển trạng thái Task sang IN_PROGRESS', NOW())
ON CONFLICT (id) DO NOTHING;
