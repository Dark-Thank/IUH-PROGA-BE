# ⏱️ TIÊU CHUẨN KHÁCH QUAN VỀ THỜI GIAN SPRINT TRONG CÔNG NGHỆ PHẦN MỀM (SPRINT DURATION STANDARDS)

Tài liệu này giải thích cơ sở lý luận kỹ thuật và các tiêu chuẩn quốc tế về việc lựa chọn độ dài chu kỳ Sprint trong quản lý tiến độ dự án phần mềm.

---

## 1. VÌ SAO CHUẨN SCRUM AGILE KHUYÊN DÙNG SPRINT 1 TUẦN?

Trong khung quản lý Agile Scrum hiện đại (Scrum Guide & PMBOK 7th Edition), chu kỳ **Sprint 1 tuần (5-7 ngày làm việc)** được coi là "Nhịp tim" (Heartbeat) chuẩn cho các dự án phát triển phần mềm vì 4 lý do kỹ thuật sau:

1. **Phản Hồi Nhanh (Fast Feedback Loop)**:
   - Cuối mỗi tuần, sản phẩm có thể Demo phiên bản chạy được cho Product Owner / Khách hàng. Nếu có sai lệch về nghiệp vụ, chi phí sửa đổi ở tuần tiếp theo là cực kỳ nhỏ.
2. **Kiểm Soát Rủi Ro Thấp (Low Risk Horizon)**:
   - Rủi ro bị trễ hạn hoặc đi sai hướng chỉ nằm trong phạm vi 5-7 ngày thay vì kéo dài hàng tháng.
3. **Ước Lượng Khối Lượng Công Việc Chính Xác Hơn**:
   - Con người ước lượng công việc trong phạm vi 1 tuần chính xác hơn rất nhiều so với việc ước lượng cho 1 tháng.
4. **Tạo Động Lực Hoàn Thành (Sprint Goal Focus)**:
   - Đội ngũ phát triển tập trung cao độ để hoàn thành mục tiêu ngắn hạn trong tuần.

---

## 2. KHI NÀO 1 SPRINT NÊN KÉO DÀI HƠN 1 TUẦN (2 - 3 TUẦN)?

Không phải lúc nào 1 Sprint cũng cứng nhắc 1 tuần. Trong thực tế công nghiệp phần mềm, một Sprint sẽ được điều chỉnh kéo dài sang **2 tuần hoặc 3 tuần** dựa trên các căn cứ thực tế và lý do khách quan sau:

### 🔹 Lý do 1: Công việc R&D Kiến trúc Lõi & Mã hóa Database Lớn
- **Tình huống**: Xây dựng Core Framework, thiết kế Schema Database đa dịch vụ, hoặc migrate dữ liệu hàng triệu bản ghi.
- **Giải thích**: Các hạng mục này đòi hỏi nghiên cứu chuyên sâu, không thể cắt nhỏ thành phiên bản Demo trong 1 tuần mà cần 2 tuần để hoàn thiện và kiểm thử toàn vẹn (Data Integrity).

### 🔹 Lý do 2: Tích Hợp Thiết Bị Phần Cứng & Bên Thứ Ba (Hardware / IoT / External SDK)
- **Tình huống**: Tích hợp thiết bị đọc thẻ RFID, máy quét sinh trắc học FaceID tại cửa, hoặc chờ phía Ngân hàng / Cổng thanh toán duyệt Sandbox môi trường UAT.
- **Giải thích**: Tiến độ phụ thuộc vào thời gian phản hồi của đối tác bên ngoài hoặc thiết bị phần cứng thực tế, cần đệm thời gian 2-3 tuần để tránh nghẽn luồng Sprint.

### 🔹 Lý do 3: Tích Hợp Mô Hình AI / Machine Learning & Training Data
- **Tình huống**: Huấn luyện mô hình AI (Fine-tuning LLM / Computer Vision) và thu thập bộ dữ liệu chuẩn.
- **Giải thích**: Quá trình gán nhãn dữ liệu (Data Labeling) và chạy Training Epochs trên GPU mất nhiều ngày liên tục.

### 🔹 Lý do 4: Đánh Giá Bảo Mật & Kiểm Thử Thâm Nhập (Penetration Testing / Security Audit)
- **Tình huống**: Kiểm thử thâm nhập hệ thống tài chính/y tế theo tiêu chuẩn ISO 27001 / PCI-DSS trước khi Golive.
- **Giải thích**: Đội ngũ Security Auditor cần 2-3 tuần để quét lỗ hổng mã nguồn và xác nhận biên bản an toàn thông tin.

---

## 3. CÁCH AI AGENT PHÂN BỔ SPRINT THEO NĂNG LỰC NHÂN SỰ THỰC TẾ

Khi phân rã công việc WBS, AI Agent sẽ tính toán dựa trên **Năng lực nhân sự thực tế (Team Capacity)**:
$$\text{Tổng số ngày công khả dụng / Sprint} = \text{Số thành viên} \times \text{Số ngày làm việc/tuần}$$

- **Ví dụ**: Nhóm phát triển gồm **3 người** (1 Backend, 1 Frontend, 1 QA), làm việc 5 ngày/tuần:
  - Khả năng hoàn thành tối đa: $\approx 15 \text{ ngày công / Sprint}$.
  - Nếu tổng khối lượng dự án là $60 \text{ ngày công}$, AI Agent sẽ **tự động phân bổ đều ra 4 - 5 Sprint** thay vì gượng ép trong 3 Sprint, đảm bảo tính khả thi và tránh kiệt sức (Burnout) cho team.
