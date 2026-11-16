package com.proga.notification_service.service.impl;

import com.proga.notification_service.dto.EmailNotificationRequest;
import com.proga.notification_service.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Async
    @Override
    public void sendEmail(EmailNotificationRequest request) {
        sendEmail(request.getRecipientEmail(), request.getSubject(), request.getBody(), null, null, null);
    }

    @Async
    @Override
    public void sendEmail(String to, String subject, String body) {
        sendEmail(to, subject, body, null, null, null);
    }

    @Async
    @Override
    public void sendEmail(String to, String subject, String body, String spaceName, String taskCode, String actionUrl) {
        sendEmail(to, subject, body, spaceName, taskCode, actionUrl, null, null, null, null);
    }

    @Async
    @Override
    public void sendEmail(String to, String subject, String body, String spaceName, String taskCode, String actionUrl,
                          Long spaceId, Long taskId, String senderName, String replyToEmail) {
        if (mailSender == null) {
            log.warn("[EMAIL] JavaMailSender chưa được cấu hình. Bỏ qua gửi email tới [{}].", to);
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(to);
            String fullSubject = subject.startsWith("[IUH-PROGA]") ? subject : "[IUH-PROGA] " + subject;
            helper.setSubject(fullSubject);

            // Sender email & Personal Name
            String smtpUser = (mailUsername != null && !mailUsername.isBlank()) ? mailUsername : "luungoccaoson0401@gmail.com";
            String actorName = (senderName != null && !senderName.isBlank())
                    ? senderName
                    : "Lưu Ngọc Cao Sơn (Quản lý dự án)";

            // Tên hiển thị cạnh avatar người gửi ở ngoài Gmail (để tên bình thường, không kèm chức vụ)
            String cleanSenderName = actorName.replaceAll("\\(.*\\)", "").trim();
            if (cleanSenderName.isBlank()) cleanSenderName = "Lưu Ngọc Cao Sơn";

            String fromEmail = (replyToEmail != null && !replyToEmail.isBlank()) ? replyToEmail : smtpUser;
            try {
                helper.setFrom(fromEmail, cleanSenderName);
            } catch (Exception e) {
                helper.setFrom(smtpUser, cleanSenderName);
            }

            if (replyToEmail != null && !replyToEmail.isBlank()) {
                try {
                    helper.setReplyTo(replyToEmail, cleanSenderName);
                } catch (Exception e) {
                    helper.setReplyTo(replyToEmail);
                }
            }

            // Email Threading Headers (Hội thoại nhóm thư chung 1 luồng trong Gmail)
            if (spaceId != null && taskId != null) {
                String threadId = "<proga-space-" + spaceId + "-task-" + taskId + "@iuh-proga.vn>";
                mimeMessage.setHeader("Message-ID", "<proga-msg-" + spaceId + "-" + taskId + "-" + System.currentTimeMillis() + "@iuh-proga.vn>");
                mimeMessage.setHeader("In-Reply-To", threadId);
                mimeMessage.setHeader("References", threadId);
            }

            String displaySpace = (spaceName != null && !spaceName.isBlank()) ? spaceName : "PROGA";
            String displayCode = (taskCode != null && !taskCode.isBlank()) ? taskCode : "Task";
            String link = (actionUrl != null && !actionUrl.isBlank()) ? actionUrl : "http://localhost:3000";

            // Extract Task Title from subject (clean without [IUH-PROGA] or (Task-xx))
            String cleanTitle = subject.replaceFirst("^\\[IUH-PROGA\\]\\s*", "").replaceFirst("^\\(Task-\\d+\\)\\s*", "");

            String cleanNameForInitials = actorName.replaceAll("\\(.*\\)", "").trim();
            String[] parts = cleanNameForInitials.split("\\s+");
            String initials = parts.length >= 2 
                    ? ("" + parts[parts.length - 2].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase()
                    : (parts.length == 1 && parts[0].length() > 0 ? parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase() : "CS");

            String cleanBody = (body != null) ? body.trim() : "";

            String html = "<!DOCTYPE html>"
                    + "<html lang=\"vi\">"
                    + "<head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"></head>"
                    + "<body style=\"margin:0;padding:20px 10px;background-color:#F4F5F7;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;color:#172B4D;\">"
                    + "  <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"max-width:560px;margin:0 auto;background-color:#ffffff;border:1px solid #DFE1E6;border-radius:10px;box-shadow:0 2px 6px rgba(0,0,0,0.04);\">"
                    + "    <tr>"
                    + "      <td style=\"padding:20px 22px;\">"
                    + "        <div style=\"font-size:11px;font-weight:700;color:#5E6C84;text-transform:uppercase;margin:0 0 6px 0;letter-spacing:0.4px;\">"
                    + "          " + displaySpace + " / <span style=\"color:#0052CC;\">" + displayCode + "</span>"
                    + "        </div>"
                    + "        <h2 style=\"font-size:17px;font-weight:600;color:#0052CC;line-height:1.35;margin:0 0 14px 0;\">"
                    + "          " + cleanTitle + "</h2>"
                    + "        <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"margin:0 0 6px 0;\">"
                    + "          <tr>"
                    + "            <td style=\"width:28px;height:28px;background-color:#5E4DB2;color:#ffffff;font-size:11px;font-weight:bold;text-align:center;border-radius:50%;line-height:28px;\">"
                    + "              " + initials
                    + "            </td>"
                    + "            <td style=\"padding-left:8px;\">"
                    + "              <strong style=\"color:#172B4D;font-size:13px;\">" + actorName + "</strong>"
                    + "            </td>"
                    + "          </tr>"
                    + "        </table>"
                    + "        <div style=\"background-color:#F9FAFB;border-left:4px solid #0052CC;border-radius:4px;padding:8px 12px;font-size:13px;color:#172B4D;line-height:1.45;margin:6px 0 16px 0;\">"
                    + cleanBody
                    + "</div>"
                    + "        <div>"
                    + "          <a href=\"" + link + "\" style=\"display:inline-block;background-color:#0052CC;color:#ffffff;text-decoration:none;font-weight:600;font-size:13px;padding:9px 18px;border-radius:4px;\">"
                    + "            Xem chi tiết công việc"
                    + "          </a>"
                    + "        </div>"
                    + "      </td>"
                    + "    </tr>"
                    + "  </table>"
                    + "</body></html>";

            helper.setText(html, true);

            log.info("[EMAIL] Đang gửi email HTML tới: {} | Tiêu đề: {}", to, fullSubject);
            mailSender.send(mimeMessage);
            log.info("[EMAIL] Gửi email HTML thành công tới {}", to);
        } catch (Exception e) {
            log.error("[EMAIL] Thất bại khi gửi email tới {}: {}", to, e.getMessage(), e);
        }
    }

    @Override
    public String sendTestEmail(String to) {
        if (mailSender == null) {
            return "LỖI: JavaMailSender chưa được cấu hình. Vui lòng kiểm tra lại cấu hình spring.mail trong application.yml hoặc biến môi trường!";
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("[IUH-PROGA] (Task-8) Tự động lăn (Rollover) các Task chưa hoàn thành khi đóng Sprint");

            String sender = (mailUsername != null && !mailUsername.isBlank()) ? mailUsername : "luungoccaoson0401@gmail.com";
            try {
                helper.setFrom(sender, "Lưu Ngọc Cao Sơn");
            } catch (Exception e) {
                helper.setFrom(sender);
            }
            helper.setReplyTo("luungoccaoson0401@gmail.com");

            // Email Threading Headers
            String threadId = "<proga-space-1-task-8@iuh-proga.vn>";
            mimeMessage.setHeader("Message-ID", "<proga-msg-1-8-" + System.currentTimeMillis() + "@iuh-proga.vn>");
            mimeMessage.setHeader("In-Reply-To", threadId);
            mimeMessage.setHeader("References", threadId);

            String html = "<!DOCTYPE html>"
                    + "<html lang=\"vi\">"
                    + "<head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"></head>"
                    + "<body style=\"margin:0;padding:20px 10px;background-color:#F4F5F7;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;color:#172B4D;\">"
                    + "  <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"max-width:560px;margin:0 auto;background-color:#ffffff;border:1px solid #DFE1E6;border-radius:10px;box-shadow:0 2px 6px rgba(0,0,0,0.04);\">"
                    + "    <tr>"
                    + "      <td style=\"padding:20px 22px;\">"
                    + "        <div style=\"font-size:11px;font-weight:700;color:#5E6C84;text-transform:uppercase;margin:0 0 6px 0;letter-spacing:0.4px;\">"
                    + "          Dự Án Khóa Luận Tốt Nghiệp IUH-PROGA / <span style=\"color:#0052CC;\">Task-8</span>"
                    + "        </div>"
                    + "        <h2 style=\"font-size:17px;font-weight:600;color:#0052CC;line-height:1.35;margin:0 0 14px 0;\">"
                    + "          Tự động lăn (Rollover) các Task chưa hoàn thành khi đóng Sprint"
                    + "        </h2>"
                    + "        <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"margin:0 0 6px 0;\">"
                    + "          <tr>"
                    + "            <td style=\"width:28px;height:28px;background-color:#5E4DB2;color:#ffffff;font-size:11px;font-weight:bold;text-align:center;border-radius:50%;line-height:28px;\">"
                    + "              CS"
                    + "            </td>"
                    + "            <td style=\"padding-left:8px;\">"
                    + "              <strong style=\"color:#172B4D;font-size:13px;\">Lưu Ngọc Cao Sơn (Quản lý dự án)</strong>"
                    + "            </td>"
                    + "          </tr>"
                    + "        </table>"
                    + "        <div style=\"background-color:#F9FAFB;border-left:4px solid #0052CC;border-radius:4px;padding:8px 12px;font-size:13px;color:#172B4D;line-height:1.45;margin:6px 0 16px 0;\">"
                    + "Công việc đã được Quản lý dự án duyệt nghiệm thu hoàn thành (ĐÃ XONG)."
                    + "</div>"
                    + "        <div>"
                    + "          <a href=\"http://localhost:3000\" style=\"display:inline-block;background-color:#0052CC;color:#ffffff;text-decoration:none;font-weight:600;font-size:13px;padding:9px 18px;border-radius:4px;\">"
                    + "            Xem chi tiết công việc"
                    + "          </a>"
                    + "        </div>"
                    + "      </td>"
                    + "    </tr>"
                    + "  </table>"
                    + "</body></html>";

            helper.setText(html, true);

            log.info("[TEST-EMAIL] Bắt đầu gửi email test HTML tới: {}", to);
            mailSender.send(mimeMessage);
            log.info("[TEST-EMAIL] Gửi email test HTML thành công tới {}", to);
            return "THÀNH CÔNG: Đã gửi email kiểm tra thành công đến " + to + ". Vui lòng kiểm tra Hộp thư đến (Inbox) hoặc Thư rác (Spam)!";
        } catch (Exception e) {
            log.error("[TEST-EMAIL] Thất bại khi gửi email test tới {}: {}", to, e.getMessage(), e);
            return "LỖI GỬI EMAIL: " + e.getMessage();
        }
    }
}
