# SYSTEM PROMPT & ARCHITECTURE SPECIFICATION (PROGA)
> **Target Audience:** AI Coding Assistants (Claude 3.5 Sonnet, Cursor, Anysphere/Antigravity)
> **Project Name:** PROGA - Hệ Thống Quản Lý Tiến Độ Dự Án Tích Hợp AI
> **Architecture Pattern:** Spring Cloud Microservices Architecture

---

## 1. TỔNG QUAN HỆ THỐNG & TẦM NHÌN (SYSTEM OVERVIEW)
Hệ thống **PROGA** là nền tảng quản lý dự án thế hệ mới dành cho các đội ngũ phát triển phần mềm và làm việc nhóm. Hệ thống phân rã cấu trúc dự án theo mô hình phân tầng: **Workspace -> Space -> Task/Kanban**. Điểm phá phá của PROGA là việc tích hợp **3 AI Agents chuyên biệt** (Requirement Agent, PM Agent, Technical Advisor Agent) hỗ trợ tự động hóa phân rã công việc, dự báo rủi ro tiến độ và tư vấn kỹ thuật trực tiếp trong quá trình vận hành dự án.

---

## 2. KIẾN TRÚC KỸ THUẬT & TECH STACK (TECHNICAL ARCHITECTURE)

### 2.1 Backend Microservices Ecosystem (Spring Boot 3.x / Java 17-21)
Hệ thống bao gồm **6 Microservices độc lập**, quản lý cơ sở dữ liệu riêng (Database per Service) và giao tiếp qua API Gateway:

1. **`eureka-server` (Port 8761):** Spring Cloud Netflix Eureka làm Service Registry & Discovery.
2. **`api-gateway` (Port 8080):** Spring Cloud Gateway làm Cửa ngõ duy nhất (Single Entry Point), xử lý Routing, CORS, Rate Limiting và JWT Authentication filter.
3. **`auth-service` (Port 8081):** Quản lý Người dùng (`users`), Vai trò hệ thống (`roles`), JWT Provider, RBAC Authentication.
4. **`workspace-service` (Port 8082):** Core Business Service quản lý `workspaces`, `project_members`, `spaces`, `tasks`, `task_notes`, và `workspace_logs` (Activity Log).
5. **`ai-service` (Port 8083):** AI Agent Orchestrator tích hợp OpenAI Assistants API / LangChain4j, quản lý `ai_threads` và `ai_chat_messages`.
6. **`notification-service` (Port 8084):** Quản lý `notifications`, `notification_types`, phát tín hiệu Realtime qua WebSocket STOMP / SockJS & Email SMTP.

### 2.2 Shared Infrastructure & Storage
* **Relational Database:** PostgreSQL (Đã chia schema/database theo từng Service).
* **Caching & In-Memory Storage:** Redis Cluster (Lưu Session, Rate Limiting, Cache Dashboard Analytics & Task Progress).
* **Communication:** Inter-service call đồng bộ qua **Spring Cloud OpenFeign**.

### 2.3 Frontend Ecosystem
* **Web App:** Next.js (React), Tailwind CSS, Shadcn UI / React Query, Zustand.
* **Mobile App:** React Native (Expo framework), TypeScript, React Native Paper.

---

## 3. MÔ HÌNH DỮ LIỆU BẮT BUỘC (ERD SPECIFICATION)

Khi sinh code Entity, Migration (Flyway/Liquibase) hoặc SQL Query, AI phải tuân thủ chính xác các bảng và kiểu dữ liệu sau:

### 3.1 Domain Auth & User (`auth-service` DB)
* **`users`**:
  * `id`: `uuid` [PK]
  * `username`: `varchar(50)` [UNIQUE, NOT NULL]
  * `email`: `varchar(100)` [UNIQUE, NOT NULL]
  * `password`: `varchar(255)` [NOT NULL]
  * `is_admin`: `boolean` [DEFAULT false]
  * `created_at`: `timestamp`
* **`roles`**:
  * `id`: `bigint` [PK]
  * `name`: `varchar(20)` [UNIQUE, NOT NULL] *(Master Data: `ADMIN`, `PM`, `MEMBER`)*

### 3.2 Domain Workspace & Task (`workspace-service` DB)
* **`workspaces`**:
  * `id`: `uuid` [PK]
  * `name`: `varchar(100)` [NOT NULL]
  * `description`: `text`
  * `owner_id`: `uuid` [FK -> `users.id`]
  * `created_at`: `timestamp`
* **`project_members`**:
  * `workspace_id`: `uuid` [PK, FK -> `workspaces.id`]
  * `user_id`: `uuid` [PK, FK -> `users.id`]
  * `role_id`: `bigint` [PK, FK -> `roles.id`]
  * `joined_at`: `timestamp`
* **`spaces`**:
  * `id`: `uuid` [PK]
  * `workspace_id`: `uuid` [FK -> `workspaces.id`]
  * `name`: `varchar(100)` [NOT NULL]
  * `start_date`: `timestamp`
  * `end_date`: `timestamp`
  * `created_at`: `timestamp`
* **`tasks`**:
  * `id`: `uuid` [PK]
  * `space_id`: `uuid` [FK -> `spaces.id`]
  * `title`: `varchar(150)` [NOT NULL]
  * `description`: `text`
  * `status`: `varchar(20)` *(Enums: `TODO`, `IN_PROGRESS`, `REVIEW`, `DONE`)*
  * `priority`: `varchar(10)` *(Enums: `LOW`, `MEDIUM`, `HIGH`, `URGENT`)*
  * `owner_id`: `uuid` [FK -> `users.id`] *(Assignee)*
  * `start_date`: `timestamp`
  * `due_date`: `timestamp`
  * `created_at`: `timestamp`
* **`task_notes`**:
  * `id`: `bigint` [PK, AUTO_INCREMENT]
  * `task_id`: `uuid` [FK -> `tasks.id`]
  * `author_id`: `uuid` [FK -> `users.id`]
  * `note_content`: `text`
  * `created_at`: `timestamp`
* **`workspace_logs`**:
  * `id`: `bigint` [PK, AUTO_INCREMENT]
  * `workspace_id`: `uuid` [FK -> `workspaces.id`]
  * `task_id`: `uuid` [FK -> `tasks.id`, NULLABLE]
  * `user_id`: `uuid` [FK -> `users.id`]
  * `action_type`: `varchar(50)`
  * `old_value`: `text`
  * `new_value`: `text`
  * `log_message`: `text`
  * `created_at`: `timestamp`

### 3.3 Domain AI Agent (`ai-service` DB)
* **`ai_threads`**:
  * `id`: `uuid` [PK]
  * `openai_thread_id`: `varchar(100)`
  * `space_id`: `uuid` [FK -> `spaces.id`]
  * `agent_type`: `varchar(30)` *(Enums: `REQUIREMENT`, `PM`, `TECHNICAL_ADVISOR`)*
  * `created_at`: `timestamp`
* **`ai_chat_messages`**:
  * `id`: `bigint` [PK, AUTO_INCREMENT]
  * `thread_id`: `uuid` [FK -> `ai_threads.id`]
  * `sender_type`: `varchar(20)` *(Enums: `USER`, `ASSISTANT`, `SYSTEM`)*
  * `message_content`: `text`
  * `json_payload`: `text` *(Dùng lưu cấu trúc Task tự động phân rã)*
  * `created_at`: `timestamp`

### 3.4 Domain Notification (`notification-service` DB)
* **`notification_types`**:
  * `id`: `smallint` [PK]
  * `code`: `varchar(30)` *(Master Data: `TASK_ASSIGNED`, `DEADLINE_WARNING`, `STATUS_CHANGED`, `MENTION`)*
  * `label`: `varchar(100)`
* **`notifications`**:
  * `id`: `bigint` [PK, AUTO_INCREMENT]
  * `user_id`: `uuid` [FK -> `users.id`]
  * `workspace_id`: `uuid` [FK -> `workspaces.id`]
  * `title`: `varchar(150)`
  * `content`: `text`
  * `is_read`: `boolean` [DEFAULT false]
  * `type_id`: `smallint` [FK -> `notification_types.id`]
  * `created_at`: `timestamp`

---

## 4. MA TRẬN PHÂN QUYỀN VÀ ACTORS (USE CASE SPECIFICATION)

### 4.1 Danh sách Actors
1. **Admin (Hệ thống):** Quản trị toàn bộ người dùng, cấp quyền, cấu hình hệ thống.
2. **Project Manager (PM):** Quản lý Workspace, tạo Space, quản lý thành viên, phân quyền, xem thống kê tiến độ, tương tác với PM Agent & Requirement Agent.
3. **Member (Developer/Tester):** Vận hành bảng Kanban, cập nhật trạng thái Task, thêm ghi chú/comment, nhận thông báo tiến độ, nhận phân công Task.

### 4.2 Chi tiết Chức năng Nghiệp vụ theo Use Case
* **Phân hệ 1: Authentication & User Admin**
  * Đăng ký tài khoản, Đăng nhập hệ thống (Cấp JWT Access & Refresh Token).
  * Quản trị yêu cầu & Quản trị người dùng (Cấp quyền Role ADMIN/PM/MEMBER).
* **Phân hệ 2: Workspace & Member Management**
  * Tạo, chỉnh sửa, xóa Workspace.
  * Mời thành viên vào Workspace, xóa thành viên, phân quyền vai trò trong dự án.
* **Phân hệ 3: Space & Sub-project Management**
  * Tạo, chỉnh sửa thông tin Space (nhóm tiến độ/sprint/phân khu vực công việc) gắn liền với thời gian `start_date` -> `end_date`.
* **Phân hệ 4: Task & Kanban Management**
  * Tạo Task, gán người thực hiện (`owner_id`), cập nhật ưu tiên, ngày hết hạn.
  * Kéo thả chuyển trạng thái công việc trên bảng Kanban (`TODO` -> `IN_PROGRESS` -> `REVIEW` -> `DONE`).
  * Lọc Task theo người thực hiện hoặc trạng thái.
  * Thêm ghi chú/comment (`task_notes`) và ghi nhận Activity Log (`workspace_logs`).
* **Phân hệ 5: AI Agents & Automation**
  * **Requirement Agent:** Tự động phân rã Yêu cầu bài toán thành danh sách các Task nhỏ trong Space.
  * **PM Agent:** Tóm tắt tiến độ Workspace, dự báo rủi ro trễ deadline dựa trên lịch sử hoạt động.
  * **Technical Advisor Agent:** Tư vấn kiến trúc kỹ thuật và gợi ý giải pháp xử lý bug cho Task.
* **Phân hệ 6: Analytics & Realtime Notification**
  * Thống kê tiến độ các Task, thống kê năng suất theo thành viên (Có Redis Cache tối ưu tốc độ).
  * Gửi và nhận thông báo thời gian thực qua WebSocket STOMP khi có biến động công việc.

---

## 5. LỊCH TRÌNH THỰC HIỆN DỰ ÁN (PROJECT ROADMAP & MASTER DATA)

Dưới đây là mốc thời gian phát triển chi tiết làm căn cứ kiểm tra tiến độ (Vibe Coding Benchmark):

| STT | Chức năng / Hạng mục | Thời gian triển khai | Thời lượng |
| :--- | :--- | :--- | :--- |
| **1** | Quản lý tài khoản và xác thực người dùng (JWT, RBAC) | 24/06/2026 – 03/07/2026 | 10 ngày |
| **2** | Quản lý Workspace | 04/07/2026 – 08/07/2026 | 5 ngày |
| **3** | Quản lý Space | 09/07/2026 – 14/07/2026 | 6 ngày |
| **4** | Quản lý Task (CRUD, Assignment, Priority, Deadline) | 15/07/2026 – 28/07/2026 | 14 ngày |
| **5** | Bảng Kanban (Drag & Drop, Filter) | 29/07/2026 – 05/08/2026 | 8 ngày |
| **6** | Activity Log (`workspace_logs`) | 06/08/2026 – 10/08/2026 | 5 ngày |
| **7** | Dashboard thống kê + Redis Cache | 11/08/2026 – 20/08/2026 | 10 ngày |
| **8** | Requirement Agent (AI phân rã Task) | 21/08/2026 – 31/08/2026 | 11 ngày |
| **9** | PM Agent (AI dự báo rủi ro & tóm tắt) | 01/09/2026 – 10/09/2026 | 10 ngày |
| **10** | Technical Advisor Agent | 11/09/2026 – 20/09/2026 | 10 ngày |
| **11** | Thông báo thời gian thực (WebSocket STOMP) | 21/09/2026 – 26/09/2026 | 6 ngày |
| **12** | Tìm kiếm và lọc dữ liệu nâng cao | 27/09/2026 – 02/10/2026 | 6 ngày |
| **13** | Hoàn thiện YCPCN (Docker, REST API, Tối ưu hiệu năng, Responsive) | 03/10/2026 – 12/10/2026 | 10 ngày |
| **14** | Kiểm thử hệ thống (Unit Test, Integration Test, UAT, Fix Bug) | 13/10/2026 – 22/10/2026 | 10 ngày |
| **15** | Triển khai và nghiệm thu đồ án | 23/10/2026 – 27/10/2026 | 5 ngày |

---

## 6. QUY TẮC CODE BẮT BUỘC KHI XUẤT CODE (VIBE CODING RULES)

Khi bạn (AI Assistant) được yêu cầu viết code trong dự án này, hãy tuân thủ các quy tắc sau:

1. **Chuẩn Microservice Naming:**
   * Không sử dụng tên `project-service`. Bắt buộc dùng tên **`workspace-service`** cho Core Business.
   * Tất cả endpoint API phải bắt đầu bằng `/api/v1/{service-name}/...` (Ví dụ: `/api/v1/workspaces/...`, `/api/v1/auth/...`).
2. **Entity & DTO Layering:**
   * Sử dụng Lombok (`@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`).
   * Luôn tách biệt `Entity`, `CreateRequest`, `UpdateRequest`, và `Response` DTO. Không bao giờ trả về trực tiếp JPA Entity ra Controller.
3. **Database Constraints:**
   * Luôn định nghĩa đúng các kiểu UUID cho ID của `users`, `workspaces`, `spaces`, `tasks`.
   * Sử dụng `@Enumerated(EnumType.STRING)` cho các trường Enum (`TaskStatus`, `Priority`, `AgentType`).
4. **Exception Handling & Response standard:**
   * Tất cả API trả về cấu trúc thống nhất:
     ```json
     {
       "status": 200,
       "message": "Success",
       "data": { ... },
       "timestamp": "2026-07-21T12:00:00"
     }
     ```
   * Luôn có `@ControllerAdvice` / `@ExceptionHandler` để bắt lỗi nghiệp vụ (`ResourceNotFoundException`, `BadRequestException`).
5. **Feign Client Integration:**
   * Khi `ai-service` hoặc `notification-service` cần lấy dữ liệu từ `workspace-service`, phải sử dụng `@FeignClient(name = "workspace-service")`.
