-- =============================================================================
-- SEED DATA & DDL FOR IUH-PROGA SYSTEM (Updated Workspaces & Spaces)
-- Auto-executed on docker compose down -v && docker compose up -d
-- =============================================================================

-- =============================================================================
-- 1. CONNECT TO DATABASE: proga_auth_db
-- =============================================================================
\c proga_auth_db;

-- Tạo các bảng cho proga_auth_db nếu chưa tồn tại
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(500),
    phone_number VARCHAR(50),
    full_name VARCHAR(255),
    display_name VARCHAR(255),
    job_title VARCHAR(255),
    bio TEXT,
    is_admin BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

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

-- Tạo các bảng cho proga_workspace_db nếu chưa tồn tại
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
    role_id BIGINT NOT NULL,
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
    role_id BIGINT NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (space_id, user_id)
);

CREATE TABLE IF NOT EXISTS sprints (
    id BIGSERIAL PRIMARY KEY,
    space_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    goal TEXT,
    status VARCHAR(20) NOT NULL,
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
    status VARCHAR(20),
    priority VARCHAR(10),
    owner_id BIGINT,
    start_date TIMESTAMP,
    due_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

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

-- Chèn 10 Workspaces mẫu (Đại diện cho Công ty / Trường học / Tổ chức lớn)
INSERT INTO workspaces (id, name, description, owner_id, created_at) VALUES 
(1, 'Trường Đại Học Công Nghiệp TP.HCM (IUH)', 'Tổ chức giáo dục & Quản lý đồ án sinh viên khoa CNTT IUH', 2, NOW()),
(2, 'Công Ty Công Nghệ PROGA Solutions', 'Tổ chức phát triển phần mềm Doanh nghiệp & Giải pháp Microservices', 3, NOW()),
(3, 'Tập Đoàn Tài Chính VietFintech Corporation', 'Tổ chức quản lý các giải pháp Ngân hàng số & Ví điện tử bảo mật', 2, NOW()),
(4, 'Tập Đoàn Bán Lẻ E-Mart Global', 'Tổ chức điều hành chuỗi siêu thị bán lẻ & Sàn thương mại điện tử', 3, NOW()),
(5, 'Viện Nghiên Cứu Trí Tuệ Nhân Tạo AI Research Lab', 'Tổ chức nghiên cứu & phát triển mô hình AI Agents / LLM RAG', 2, NOW()),
(6, 'Công Ty Phần Mềm FPT Software Services', 'Tổ chức gia công phần mềm quốc tế & Giải pháp Cloud Transformation', 3, NOW()),
(7, 'Tổng Công Ty Logistics & Vận Tải FastDelivery', 'Tổ chức điều hành mạng lưới kho vận & Giao hàng chặng cuối', 2, NOW()),
(8, 'Bệnh Viện Đa Khoa Quốc Tế MedCare', 'Tổ chức y tế quản lý phòng khám & Hồ sơ bệnh án điện tử', 3, NOW()),
(9, 'Tập Đoàn Giáo Dục SmartLearning Group', 'Tổ chức quản lý các nền tảng học trực tuyến LMS & Đào tạo từ xa', 2, NOW()),
(10, 'Công Ty Truyền Thông & Streaming MediaFlix', 'Tổ chức cung cấp dịch vụ giải trí & Truyền hình trực tuyến HLS', 3, NOW())
ON CONFLICT (id) DO NOTHING;

-- Chèn Workspace Members (Bảng workspace_members) với trạng thái ACCEPTED & PENDING
INSERT INTO workspace_members (workspace_id, user_id, role_id, status, joined_at) VALUES 
(1, 2, 2, 'ACCEPTED', NOW()), -- pm_sonluu (PM/Owner)
(1, 4, 3, 'ACCEPTED', NOW()), -- dev_duy (MEMBER ACCEPTED)
(1, 5, 3, 'ACCEPTED', NOW()), -- tester_hoa (MEMBER ACCEPTED)
(1, 6, 3, 'ACCEPTED', NOW()), -- dev_minhtri (MEMBER ACCEPTED)
(1, 7, 3, 'PENDING', NOW()),  -- ba_tuan (LỜI MỜI PENDING)
(2, 3, 2, 'ACCEPTED', NOW()), -- pm_lananh (PM/Owner)
(2, 4, 3, 'ACCEPTED', NOW()), -- dev_duy (MEMBER ACCEPTED)
(2, 6, 3, 'PENDING', NOW()),  -- dev_minhtri (LỜI MỜI PENDING)
(3, 2, 2, 'ACCEPTED', NOW()), -- pm_sonluu (Owner)
(3, 4, 3, 'ACCEPTED', NOW()), -- dev_duy (Member)
(4, 3, 2, 'ACCEPTED', NOW()), -- pm_lananh (Owner)
(4, 5, 3, 'ACCEPTED', NOW()), -- tester_hoa (Member)
(5, 2, 2, 'ACCEPTED', NOW()), -- pm_sonluu (Owner)
(5, 6, 3, 'ACCEPTED', NOW()), -- dev_minhtri (Member)
(6, 3, 2, 'ACCEPTED', NOW()), -- pm_lananh (Owner)
(7, 2, 2, 'ACCEPTED', NOW()), -- pm_sonluu (Owner)
(8, 3, 2, 'ACCEPTED', NOW()), -- pm_lananh (Owner)
(9, 2, 2, 'ACCEPTED', NOW()), -- pm_sonluu (Owner)
(10, 3, 2, 'ACCEPTED', NOW())  -- pm_lananh (Owner)
ON CONFLICT (workspace_id, user_id) DO NOTHING;

-- Chèn 10 Spaces mẫu (Các Dự án nằm trong từng Workspace tương ứng)
INSERT INTO spaces (id, workspace_id, name, start_date, end_date, is_private, created_at) VALUES 
(1, 1, 'Dự Án Khóa Luận Tốt Nghiệp IUH-PROGA', '2026-07-01 08:00:00', '2026-08-31 18:00:00', false, NOW()),
(2, 1, 'Dự Án Quản Lý Ký Túc Xá Sinh Viên', '2026-08-01 08:00:00', '2026-09-30 18:00:00', false, NOW()),
(3, 1, 'Dự Án Đăng Ký Tín Chỉ & Thời Khóa Biểu (Private)', '2026-09-01 08:00:00', '2026-10-31 18:00:00', true, NOW()),
(4, 2, 'Dự Án Sàn Thương Mại Điện Tử Microservices', '2026-07-01 08:00:00', '2026-08-31 18:00:00', false, NOW()),
(5, 2, 'Dự Án Hệ Thống Quản Lý Nhân Sự Smart HRM', '2026-08-01 08:00:00', '2026-09-30 18:00:00', false, NOW()),
(6, 3, 'Dự Án App Mobile Banking & Sinh Trắc Học', '2026-07-01 08:00:00', '2026-08-31 18:00:00', false, NOW()),
(7, 3, 'Dự Án Cổng Thanh Toán Mã QR VietQR (Private)', '2026-08-01 08:00:00', '2026-09-30 18:00:00', true, NOW()),
(8, 4, 'Dự Án Chuỗi Cung Ứng & Quản Lý Kho WMS', '2026-07-01 08:00:00', '2026-08-31 18:00:00', false, NOW()),
(9, 5, 'Dự Án Chatbot AI CSKH Bằng Mô Hình LLM', '2026-07-01 08:00:00', '2026-08-31 18:00:00', false, NOW()),
(10, 5, 'Dự Án Phân Tích Dữ Liệu Khách Hàng BigData (Private)', '2026-09-01 08:00:00', '2026-10-31 18:00:00', true, NOW())
ON CONFLICT (id) DO NOTHING;

-- Chèn Space Members (Bảng space_members)
INSERT INTO space_members (space_id, user_id, role_id, joined_at) VALUES
(1, 2, 2, NOW()), -- pm_sonluu trong Space 1 (PM)
(1, 4, 3, NOW()), -- dev_duy trong Space 1 (Member)
(1, 5, 3, NOW()), -- tester_hoa trong Space 1 (Member)
(1, 6, 3, NOW()), -- dev_minhtri trong Space 1 (Member)
(2, 2, 2, NOW()), -- pm_sonluu trong Space 2 (PM)
(2, 4, 3, NOW()), -- dev_duy trong Space 2 (Member)
(3, 2, 2, NOW()), -- pm_sonluu trong Space 3 (PM)
(4, 3, 2, NOW()), -- pm_lananh trong Space 4 (PM)
(4, 4, 3, NOW()), -- dev_duy trong Space 4 (Member)
(5, 3, 2, NOW()), -- pm_lananh trong Space 5 (PM)
(6, 2, 2, NOW()), -- pm_sonluu trong Space 6 (PM)
(7, 2, 2, NOW()), -- pm_sonluu trong Space 7 (PM)
(8, 3, 2, NOW()), -- pm_lananh trong Space 8 (PM)
(9, 2, 2, NOW()), -- pm_sonluu trong Space 9 (PM)
(10, 2, 2, NOW()) -- pm_sonluu trong Space 10 (PM)
ON CONFLICT (space_id, user_id) DO NOTHING;

-- Chèn Sprints mẫu cho các Space
INSERT INTO sprints (id, space_id, name, goal, status, start_date, end_date, created_at) VALUES 
(1, 1, 'Sprint 1: Baseline & Auth Microservices', 'Thiết kế cơ sở dữ liệu và triển khai Authentication JWT Gateway', 'CLOSED', '2026-07-01 08:00:00', '2026-07-14 18:00:00', NOW()),
(2, 1, 'Sprint 2: Workspace & Backlog Management UI', 'Xây dựng REST APIs Workspace, Space Public/Private và UI Backlog List', 'CLOSED', '2026-07-15 08:00:00', '2026-07-28 18:00:00', NOW()),
(3, 1, 'Sprint 3: Bảng Kanban & Đồng Bội Real-time WebSocket', 'Triển khai View Bảng Kanban 3 cột, Kéo thả Task và WebSocket STOMP Sync', 'ACTIVE', '2026-07-29 08:00:00', '2026-08-11 18:00:00', NOW()),
(4, 1, 'Sprint 4: Spring AI & Vector DB RAG Integration', 'Tích hợp Spring AI, PGVector Store và xây dựng 3 AI Agents (PO, PM, Tech Lead)', 'FUTURE', '2026-08-12 08:00:00', '2026-08-25 18:00:00', NOW()),
(5, 1, 'Sprint 5: Testing, CI/CD & Final Presentation', 'Viết Unit Test, tối ưu hiệu năng API Gateway và hoàn thiện báo cáo KLTN', 'FUTURE', '2026-08-26 08:00:00', '2026-09-08 18:00:00', NOW()),
(6, 4, 'Sprint 1: Product Catalog & ElasticSearch', 'Xây dựng dịch vụ danh mục sản phẩm và tìm kiếm ElasticSearch', 'ACTIVE', '2026-07-01 08:00:00', '2026-07-20 18:00:00', NOW()),
(7, 4, 'Sprint 2: Payment Gateway VNPAY & Momo', 'Tích hợp thanh toán trực tuyến qua VNPAY và ví MoMo', 'FUTURE', '2026-07-21 08:00:00', '2026-08-10 18:00:00', NOW()),
(8, 6, 'Sprint 1: Core Banking & Biometric Auth', 'Xác thực sinh trắc học FaceID và giao dịch tài chính', 'ACTIVE', '2026-07-01 08:00:00', '2026-07-30 18:00:00', NOW())
ON CONFLICT (id) DO NOTHING;

-- Chèn 22 Tasks mẫu chi tiết phân bổ qua 5 Sprints của Space 1
INSERT INTO tasks (id, space_id, sprint_id, title, description, status, priority, owner_id, start_date, due_date, created_at) VALUES 
-- Sprint 1 (Closed)
(1, 1, 1, 'Thiết kế sơ đồ ERD & PostgreSQL Schema', 'Xác định các bảng users, workspaces, spaces, sprints, tasks và mối quan hệ FK.', 'DONE', 'URGENT', 2, '2026-07-01 08:00:00', '2026-07-04 18:00:00', NOW()),
(2, 1, 1, 'Xây dựng Auth-Service & Cấu hình Security JWT Gateway', 'Triển khai đăng ký, đăng nhập và cấp JWT Token cho toàn hệ thống.', 'DONE', 'HIGH', 4, '2026-07-05 08:00:00', '2026-07-09 18:00:00', NOW()),
(3, 1, 1, 'Cấu hình Docker Compose cho 5 Microservices', 'Đóng gói PostgreSQL, Redis, Eureka, API Gateway và Auth Service.', 'DONE', 'HIGH', 6, '2026-07-08 08:00:00', '2026-07-11 18:00:00', NOW()),
(4, 1, 1, 'Cấu hình Spring Cloud OpenFeign Inter-service Communication', 'Kết nối Feign Client từ API Gateway và AI-Service tới Workspace-Service.', 'DONE', 'MEDIUM', 4, '2026-07-10 08:00:00', '2026-07-14 18:00:00', NOW()),

-- Sprint 2 (Closed)
(5, 1, 2, 'Phát triển REST APIs CRUD Workspace & Phân quyền User', 'Tạo controller và service quản lý Workspace kèm kiểm tra quyền Owner.', 'DONE', 'HIGH', 2, '2026-07-15 08:00:00', '2026-07-18 18:00:00', NOW()),
(6, 1, 2, 'Triển khai tính năng Space Public / Private', 'Bổ sung trường is_private cho bảng spaces và lọc quyền truy cập theo User ID.', 'DONE', 'HIGH', 4, '2026-07-19 08:00:00', '2026-07-22 18:00:00', NOW()),
(7, 1, 2, 'Xây dựng Giao diện Sprint Accordion & Backlog List View', 'Tạo UI hiển thị các Accordions chứa danh sách Task phân nhóm theo Sprint.', 'DONE', 'HIGH', 4, '2026-07-22 08:00:00', '2026-07-25 18:00:00', NOW()),
(8, 1, 2, 'Xây dựng Drawer Panel xem & chỉnh sửa Task bên phải', 'Tạo slider drawer mở bên phải cho phép sửa tên, ngày, ưu tiên và gán người làm.', 'DONE', 'MEDIUM', 5, '2026-07-26 08:00:00', '2026-07-28 18:00:00', NOW()),

-- Sprint 3 (Active)
(9, 1, 3, 'Xây dựng View Bảng Kanban 3 Cột (TODO, IN_PROGRESS, DONE)', 'Hiển thị các card công việc tương ứng theo trạng thái của Sprint Active.', 'DONE', 'HIGH', 4, '2026-07-29 08:00:00', '2026-07-31 18:00:00', NOW()),
(10, 1, 3, 'Cấu hình WebSocket STOMP Endpoint & Broker Handler', 'Thiết lập WebSocket server trong Workspace-Service để phát broadcast real-time.', 'IN_PROGRESS', 'URGENT', 6, '2026-08-01 08:00:00', '2026-08-04 18:00:00', NOW()),
(11, 1, 3, 'Triển khai Kéo thả Drag & Drop Task giữa Kanban & Sprint', 'Dùng dnd-kit trên React/Next.js hỗ trợ kéo thả đổi cột và đổi Sprint.', 'IN_PROGRESS', 'URGENT', 4, '2026-08-03 08:00:00', '2026-08-07 18:00:00', NOW()),
(12, 1, 3, 'Tự động chuyển Task chưa xong sang Sprint mới khi Đóng Sprint', 'Xử lý logic tự động rollback task TODO/IN_PROGRESS sang Sprint tiếp theo.', 'TODO', 'HIGH', 2, '2026-08-07 08:00:00', '2026-08-09 18:00:00', NOW()),
(13, 1, 3, 'Tự động đánh dấu nhãn Trễ Hạn (Overdue Badge)', 'Kiểm tra ngày kết thúc của Task so với hiện tại để hiển thị badge cảnh báo.', 'TODO', 'MEDIUM', 5, '2026-08-09 08:00:00', '2026-08-11 18:00:00', NOW()),

-- Sprint 4 (Future)
(14, 1, 4, 'Cấu hình Spring AI & PGVector Database Extension', 'Kích hoạt PGVector trong PostgreSQL và cấu hình VectorStore trong ai-service.', 'TODO', 'URGENT', 6, '2026-08-12 08:00:00', '2026-08-15 18:00:00', NOW()),
(15, 1, 4, 'Tạo Kho Tri Thức RAG 12 Bộ Dữ Liệu Phân Rã Mẫu', 'Nạp dữ liệu vector của 12 dự án phần mềm mẫu phục vụ Similarity Search.', 'TODO', 'HIGH', 7, '2026-08-15 08:00:00', '2026-08-17 18:00:00', NOW()),
(16, 1, 4, 'Phát triển Requirement Agent Phân rã bài toán tự động', 'Viết API /agents/decompose nhận yêu cầu bài toán và sinh task chuẩn JSON.', 'TODO', 'HIGH', 4, '2026-08-17 08:00:00', '2026-08-20 18:00:00', NOW()),
(17, 1, 4, 'Phát triển PM Agent Tóm tắt tiến độ & Dự báo rủi ro trễ deadline', 'Tự động gọi Feign Client tổng hợp tiến độ và cảnh báo rủi ro cho Space.', 'TODO', 'HIGH', 2, '2026-08-20 08:00:00', '2026-08-23 18:00:00', NOW()),
(18, 1, 4, 'Phát triển Technical Advisor Agent Tư vấn giải pháp code/bug', 'Phân tích mô hình lỗi kỹ thuật và đề xuất hướng xử lý từng bước.', 'TODO', 'MEDIUM', 4, '2026-08-23 08:00:00', '2026-08-25 18:00:00', NOW()),

-- Sprint 5 (Future)
(19, 1, 5, 'Viết Unit Test & Integration Test cho AI-Service', 'Tạo test cases bao phủ 85% logic RAG và AI Agent controllers.', 'TODO', 'MEDIUM', 5, '2026-08-26 08:00:00', '2026-08-29 18:00:00', NOW()),
(20, 1, 5, 'Tối ưu hóa hiệu năng API Gateway Rate Limiting & Response Caching', 'Giảm độ trễ phản hồi API Gateway dưới 50ms.', 'TODO', 'LOW', 6, '2026-08-29 08:00:00', '2026-08-31 18:00:00', NOW()),
(21, 1, 5, 'Đóng gói ứng dụng & Chuẩn bị Báo cáo Khóa luận Tốt nghiệp', 'Tổng hợp tài liệu thiết kế hệ thống, sơ đồ kiến trúc và kết quả thực nghiệm.', 'TODO', 'HIGH', 2, '2026-09-01 08:00:00', '2026-09-06 18:00:00', NOW()),
(22, 1, 5, 'Quay Video Demo & Kiểm thử toàn bộ hệ thống', 'Kiểm thử khép kín từ Đăng ký, Tạo Workspace, Kéo thả Kanban tới AI Agents.', 'TODO', 'HIGH', 4, '2026-09-06 08:00:00', '2026-09-08 18:00:00', NOW())
ON CONFLICT (id) DO NOTHING;

-- Tasks bổ sung cho Space 4 & Space 6
INSERT INTO tasks (id, space_id, sprint_id, title, description, status, priority, owner_id, start_date, due_date, created_at) VALUES 
(23, 4, 6, 'Thiết kế Data Schema cho Product Catalog', 'Tạo bảng products, categories, attributes', 'DONE', 'HIGH', 4, '2026-07-01 08:00:00', '2026-07-05 18:00:00', NOW()),
(24, 4, 6, 'Phát triển ElasticSearch Indexing Service', 'Đồng bộ danh mục sản phẩm lên ElasticSearch', 'IN_PROGRESS', 'URGENT', 4, '2026-07-06 08:00:00', '2026-07-12 18:00:00', NOW()),
(25, 6, 8, 'Phát triển Xác thực Sinh trắc học FaceID', 'Tích hợp OpenCV & FaceID SDK cho App Mobile', 'IN_PROGRESS', 'URGENT', 2, '2026-07-01 08:00:00', '2026-07-15 18:00:00', NOW())
ON CONFLICT (id) DO NOTHING;

-- Chèn dữ liệu Task Notes (Ghi chú/Bình luận trên Task)
INSERT INTO task_notes (id, task_id, author_id, note_content, created_at) VALUES 
(1, 10, 6, 'Đã hoàn thành cấu hình WebSocket Broker trên cổng 8082.', NOW()),
(2, 11, 4, 'Đã tích hợp dnd-kit và xử lý sự kiện drag-end cập nhật status tức thì.', NOW())
ON CONFLICT (id) DO NOTHING;

-- Chèn dữ liệu Workspace Logs (Lịch sử hoạt động)
INSERT INTO workspace_logs (id, workspace_id, task_id, user_id, action_type, old_value, new_value, log_message, created_at) VALUES 
(1, 1, 10, 6, 'STATUS_CHANGE', 'TODO', 'IN_PROGRESS', 'Người dùng Trí Backend đã chuyển trạng thái Task sang IN_PROGRESS', NOW())
ON CONFLICT (id) DO NOTHING;

-- Reset sequences cho proga_workspace_db
SELECT setval(pg_get_serial_sequence('workspaces', 'id'), COALESCE(MAX(id), 1)) FROM workspaces;
SELECT setval(pg_get_serial_sequence('spaces', 'id'), COALESCE(MAX(id), 1)) FROM spaces;
SELECT setval(pg_get_serial_sequence('sprints', 'id'), COALESCE(MAX(id), 1)) FROM sprints;
SELECT setval(pg_get_serial_sequence('tasks', 'id'), COALESCE(MAX(id), 1)) FROM tasks;
SELECT setval(pg_get_serial_sequence('task_notes', 'id'), COALESCE(MAX(id), 1)) FROM task_notes;
SELECT setval(pg_get_serial_sequence('workspace_logs', 'id'), COALESCE(MAX(id), 1)) FROM workspace_logs;
