package com.proga.workspace_service.service;

import com.proga.workspace_service.client.AuthUserClient;
import com.proga.workspace_service.client.NotificationClient;
import com.proga.workspace_service.model.Space;
import com.proga.workspace_service.model.SpaceMember;
import com.proga.workspace_service.model.Sprint;
import com.proga.workspace_service.model.Task;
import com.proga.workspace_service.repository.SpaceMemberRepository;
import com.proga.workspace_service.repository.SpaceRepository;
import com.proga.workspace_service.repository.SprintRepository;
import com.proga.workspace_service.repository.TaskRepository;
import com.proga.workspace_service.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppNotificationService {

    private final NotificationClient notificationClient;
    private final AuthUserClient authUserClient;
    private final SpaceRepository spaceRepository;
    private final SprintRepository sprintRepository;
    private final TaskRepository taskRepository;
    private final SpaceMemberRepository spaceMemberRepository;
    private final WorkspaceRepository workspaceRepository;

    /**
     * Tính toán mã Task-xx tương ứng trong Space (theo thứ tự Sprint và Backlog),
     * đồng bộ hoàn toàn với thứ tự hiển thị trên Bảng Kanban (sprint.id tăng dần, task.id tăng dần).
     */
    public String resolveTaskCode(Task task) {
        if (task == null || task.getId() <= 0) return "Task";
        try {
            long spaceId = task.getSpaceId();
            List<Sprint> sprints = sprintRepository.findBySpaceIdOrderByIdAsc(spaceId);
            sprints.sort(Comparator.comparingLong(Sprint::getId));

            List<Task> tasks = taskRepository.findBySpaceIdAndIsDeletedFalseOrderByIdAsc(spaceId);
            tasks.sort(Comparator.comparingLong(Task::getId));

            Set<Long> sprintIds = sprints.stream().map(Sprint::getId).collect(Collectors.toSet());
            List<Task> orderedList = new ArrayList<>();
            for (Sprint sprint : sprints) {
                for (Task t : tasks) {
                    if (t.getSprintId() != null && t.getSprintId() == sprint.getId()) {
                        orderedList.add(t);
                    }
                }
            }
            for (Task t : tasks) {
                if (t.getSprintId() == null || !sprintIds.contains(t.getSprintId())) {
                    orderedList.add(t);
                }
            }

            for (int i = 0; i < orderedList.size(); i++) {
                if (orderedList.get(i).getId() == task.getId()) {
                    return "Task-" + (i + 1);
                }
            }
        } catch (Exception e) {
            log.warn("Could not calculate task code: {}", e.getMessage());
        }
        return "Task-" + task.getId();
    }

    /**
     * Lấy danh sách ID của PM (roleId=2 hoặc Owner) và Trợ lý ASSISTANT (roleId=4) trong Space
     */
    public Set<Long> getSpaceCoordinatorUserIds(long spaceId) {
        Set<Long> coordinators = new HashSet<>();
        try {
            spaceRepository.findById(spaceId).ifPresent(space -> {
                workspaceRepository.findById(space.getWorkspaceId()).ifPresent(ws -> {
                    coordinators.add(ws.getOwnerId());
                });
            });

            List<SpaceMember> members = spaceMemberRepository.findByIdSpaceId(spaceId);
            for (SpaceMember m : members) {
                if (m.getRoleId() != null && (m.getRoleId() == 2L || m.getRoleId() == 4L)) {
                    if (m.getId() != null) {
                        coordinators.add(m.getId().getUserId());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not resolve space coordinators: {}", e.getMessage());
        }
        return coordinators;
    }

    public Map<String, String> resolveUserInfo(Long userId) {
        Map<String, String> info = new HashMap<>();
        if (userId == null || userId <= 0) return info;
        try {
            Map<String, Object> res = authUserClient.getUserById(userId);
            if (res != null && res.get("data") instanceof Map) {
                Map<String, Object> data = (Map<String, Object>) res.get("data");
                String name = (String) data.get("fullName");
                if (name == null || name.isBlank()) name = (String) data.get("displayName");
                if (name != null) info.put("name", name);
                String email = (String) data.get("email");
                if (email != null) info.put("email", email);
            }
        } catch (Exception ignored) {}
        return info;
    }

    /**
     * Phân giải địa chỉ email gửi và nhận chính xác giữa PM/Assistant và Thành viên (Member).
     * Với tài khoản dữ liệu seed/mẫu (@proga.iuh.edu.vn hoặc proga.vn):
     * - PM / Assistant: ánh xạ tới luungoccaoson0401@gmail.com
     * - Member: ánh xạ tới sonarro1712@gmail.com
     */
    public String resolveUserEmail(Long userId, boolean isCoordinator) {
        String email = null;
        if (userId != null && userId > 0) {
            try {
                Map<String, Object> userRes = authUserClient.getUserById(userId);
                if (userRes != null && userRes.get("data") instanceof Map) {
                    Map<String, Object> userData = (Map<String, Object>) userRes.get("data");
                    email = (String) userData.get("email");
                }
            } catch (Exception ignored) {}
        }

        if (email == null || email.isBlank() || email.endsWith("@proga.iuh.edu.vn") || email.contains("proga.vn")) {
            return isCoordinator ? "luungoccaoson0401@gmail.com" : "sonarro1712@gmail.com";
        }
        return email;
    }

    /**
     * Thông báo khi được phân công công việc (TASK_ASSIGNED)
     */
    public void sendTaskAssigned(Task task, Long assignedByUserId, String assignedByName) {
        if (task == null || task.getOwnerId() == null) return;
        if (assignedByUserId != null && task.getOwnerId().equals(assignedByUserId)) return;

        String taskCode = resolveTaskCode(task);
        Map<String, String> senderInfo = resolveUserInfo(assignedByUserId);
        if (assignedByUserId == null) {
            spaceRepository.findById(task.getSpaceId()).ifPresent(sp -> {
                workspaceRepository.findById(sp.getWorkspaceId()).ifPresent(ws -> {
                    senderInfo.putAll(resolveUserInfo(ws.getOwnerId()));
                });
            });
        }
        String sender = (assignedByName != null && !assignedByName.isBlank())
                ? assignedByName
                : senderInfo.getOrDefault("name", "Lưu Ngọc Cao Sơn");
        String senderEmail = resolveUserEmail(assignedByUserId, true);

        dispatchNotification(
                task.getOwnerId(),
                task,
                "(" + taskCode + ") " + task.getTitle(),
                sender + " (Quản lý dự án) đã phân công công việc này cho bạn.",
                "TASK_ASSIGNED",
                taskCode,
                sender + " (Quản lý dự án)",
                senderEmail
        );
    }

    /**
     * Thông báo khi công việc được duyệt sang Đã xong (TASK_APPROVED)
     */
    public void sendTaskApproved(Task task, Long approvedByUserId, String approvedByName) {
        if (task == null || task.getOwnerId() == null) return;

        String taskCode = resolveTaskCode(task);
        Map<String, String> approverInfo = resolveUserInfo(approvedByUserId);
        if (approvedByUserId == null) {
            spaceRepository.findById(task.getSpaceId()).ifPresent(sp -> {
                workspaceRepository.findById(sp.getWorkspaceId()).ifPresent(ws -> {
                    approverInfo.putAll(resolveUserInfo(ws.getOwnerId()));
                });
            });
        }
        String approver = (approvedByName != null && !approvedByName.isBlank())
                ? approvedByName
                : approverInfo.getOrDefault("name", "Lưu Ngọc Cao Sơn");
        String senderEmail = resolveUserEmail(approvedByUserId, true);

        dispatchNotification(
                task.getOwnerId(),
                task,
                "(" + taskCode + ") " + task.getTitle(),
                "Công việc của bạn đã được " + approver + " (Quản lý dự án) duyệt nghiệm thu hoàn thành.",
                "TASK_APPROVED",
                taskCode,
                approver + " (Quản lý dự án)",
                senderEmail
        );
    }

    /**
     * Thông báo khi công việc bị từ chối nghiệm thu, trả về Đang làm (TASK_REJECTED)
     */
    public void sendTaskRejected(Task task, Long rejectedByUserId, String rejectedByName, String reason) {
        if (task == null || task.getOwnerId() == null) return;

        String taskCode = resolveTaskCode(task);
        Map<String, String> rejectorInfo = resolveUserInfo(rejectedByUserId);
        if (rejectedByUserId == null) {
            spaceRepository.findById(task.getSpaceId()).ifPresent(sp -> {
                workspaceRepository.findById(sp.getWorkspaceId()).ifPresent(ws -> {
                    rejectorInfo.putAll(resolveUserInfo(ws.getOwnerId()));
                });
            });
        }
        String rejector = (rejectedByName != null && !rejectedByName.isBlank())
                ? rejectedByName
                : rejectorInfo.getOrDefault("name", "Lưu Ngọc Cao Sơn");
        String detail = (reason != null && !reason.isBlank()) ? " (Lý do: " + reason + ")" : "";
        String senderEmail = resolveUserEmail(rejectedByUserId, true);

        dispatchNotification(
                task.getOwnerId(),
                task,
                "(" + taskCode + ") " + task.getTitle(),
                "Công việc chưa đạt nghiệm thu và được " + rejector + " (Quản lý dự án) chuyển về Đang làm để hoàn thiện thêm." + detail,
                "TASK_REJECTED",
                taskCode,
                rejector + " (Quản lý dự án)",
                senderEmail
        );
    }

    /**
     * Thông báo khi thành viên chuyển công việc sang Chờ duyệt (IN_REVIEW).
     * Gửi cho tất cả PM và Trợ lý ASSISTANT trong Space.
     */
    public void sendTaskSubmittedForReview(Task task, Long submitterId, String submitterName) {
        if (task == null) return;
        String taskCode = resolveTaskCode(task);
        Map<String, String> submitterInfo = resolveUserInfo(submitterId);
        String name = (submitterName != null && !submitterName.isBlank())
                ? submitterName
                : submitterInfo.getOrDefault("name", "Trần Duy");
        String senderEmail = resolveUserEmail(submitterId, false);
        Set<Long> coordinators = getSpaceCoordinatorUserIds(task.getSpaceId());

        for (Long coordId : coordinators) {
            if (submitterId != null && coordId.equals(submitterId)) continue;
            dispatchNotification(
                    coordId,
                    task,
                    "(" + taskCode + ") " + task.getTitle(),
                    name + " đã hoàn thành công việc và gửi yêu cầu nghiệm thu (Chờ duyệt). Vui lòng kiểm tra và duyệt hoàn thành.",
                    "TASK_IN_REVIEW",
                    taskCode,
                    name + " (Thành viên thực thi)",
                    senderEmail
            );
        }
    }

    /**
     * Thông báo khi thành viên cập nhật tiến độ công việc (ví dụ sang Đang làm).
     * Gửi cho PM và Trợ lý để nắm bắt tiến độ.
     */
    public void sendTaskStatusChanged(Task task, Long changedById, String changedByName, String oldStatusVn, String newStatusVn) {
        if (task == null) return;
        String taskCode = resolveTaskCode(task);
        Map<String, String> changerInfo = resolveUserInfo(changedById);
        String name = (changedByName != null && !changedByName.isBlank())
                ? changedByName
                : changerInfo.getOrDefault("name", "Trần Duy");
        String senderEmail = resolveUserEmail(changedById, false);
        Set<Long> coordinators = getSpaceCoordinatorUserIds(task.getSpaceId());

        for (Long coordId : coordinators) {
            if (changedById != null && coordId.equals(changedById)) continue;
            dispatchNotification(
                    coordId,
                    task,
                    "(" + taskCode + ") " + task.getTitle(),
                    name + " đã chuyển trạng thái công việc từ '" + oldStatusVn + "' sang '" + newStatusVn + "'.",
                    "STATUS_CHANGED",
                    taskCode,
                    name + " (Thành viên thực thi)",
                    senderEmail
            );
        }
    }

    /**
     * Thông báo khi có thảo luận mới trên công việc (TASK_COMMENTED)
     */
    public void sendTaskCommented(Task task, Long authorId, String authorName, String commentContent) {
        if (task == null || task.getOwnerId() == null) return;
        if (authorId != null && task.getOwnerId().equals(authorId)) return;

        String taskCode = resolveTaskCode(task);
        String snippet = commentContent != null ? commentContent.trim() : "";
        if (snippet.length() > 60) {
            snippet = snippet.substring(0, 57) + "...";
        }
        Map<String, String> authorInfo = resolveUserInfo(authorId);
        String commentator = (authorName != null && !authorName.isBlank())
                ? authorName
                : authorInfo.getOrDefault("name", "Trần Duy");
        Set<Long> coordinators = getSpaceCoordinatorUserIds(task.getSpaceId());
        boolean isAuthorCoordinator = coordinators.contains(authorId);
        String senderEmail = resolveUserEmail(authorId, isAuthorCoordinator);

        dispatchNotification(
                task.getOwnerId(),
                task,
                "(" + taskCode + ") " + task.getTitle(),
                commentator + ": \"" + snippet + "\"",
                "TASK_COMMENTED",
                taskCode,
                commentator + (isAuthorCoordinator ? " (Quản lý dự án)" : " (Thành viên thực thi)"),
                senderEmail
        );
    }

    private void dispatchNotification(Long userId, Task task, String title, String content, String typeCode, String taskCode, String senderName, String replyToEmail) {
        CompletableFuture.runAsync(() -> {
            try {
                Long workspaceId = null;
                String spaceName = "Không gian dự án";
                try {
                    Optional<Space> spOpt = spaceRepository.findById(task.getSpaceId());
                    if (spOpt.isPresent()) {
                        workspaceId = spOpt.get().getWorkspaceId();
                        spaceName = spOpt.get().getName();
                    }
                } catch (Exception ignored) {}

                Set<Long> coordinators = getSpaceCoordinatorUserIds(task.getSpaceId());
                boolean isRecipientCoordinator = coordinators.contains(userId);

                String recipientEmail = resolveUserEmail(userId, isRecipientCoordinator);

                String actionUrl = "http://localhost:3000/workspaces/" + (workspaceId != null ? workspaceId : 1)
                        + "/spaces/" + task.getSpaceId() + "?taskId=" + task.getId();

                Map<String, Object> req = new HashMap<>();
                req.put("userId", userId);
                if (workspaceId != null) req.put("workspaceId", workspaceId);
                req.put("spaceId", task.getSpaceId());
                req.put("taskId", task.getId());
                req.put("title", title);
                req.put("content", content);
                req.put("typeCode", typeCode);
                req.put("taskCode", taskCode);
                req.put("spaceName", spaceName);
                req.put("actionUrl", actionUrl);
                if (senderName != null) req.put("senderName", senderName);
                if (replyToEmail != null) req.put("replyToEmail", replyToEmail);

                if (recipientEmail != null && !recipientEmail.isBlank()) {
                    req.put("recipientEmail", recipientEmail);
                }

                notificationClient.sendNotification(req);
                log.info("Sent notification [{}] to user {} (taskCode: {}, email: {}, sender: {}, senderEmail: {})",
                        typeCode, userId, taskCode, recipientEmail, senderName, replyToEmail);
            } catch (Exception e) {
                log.warn("Could not dispatch notification to notification-service: {}", e.getMessage());
            }
        });
    }
}
