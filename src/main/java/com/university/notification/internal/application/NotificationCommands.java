package com.university.notification.internal.application;

import com.university.grade.api.GradesPublished;
import com.university.identity.api.IdentityDirectory;
import com.university.notification.api.NotificationMessageView;
import com.university.notification.internal.domain.NotificationMessage;
import com.university.notification.internal.persistence.NotificationMessageRepository;
import com.university.shared.exception.BusinessException;
import com.university.shared.security.*;
import com.university.tuition.api.TuitionIssued;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class NotificationCommands {
    private final NotificationMessageRepository repository;
    private final IdentityDirectory identity;
    private final TechnicalAudit audit;
    private final com.university.enrollment.api.EnrollmentRegistry enrollment;

    public NotificationCommands(
            NotificationMessageRepository repository,
            IdentityDirectory identity,
            TechnicalAudit audit,
            com.university.enrollment.api.EnrollmentRegistry enrollment) {
        this.repository = repository;
        this.identity = identity;
        this.audit = audit;
        this.enrollment = enrollment;
    }

    @EventListener
    public void schedule(com.university.timetable.api.ScheduleChanged e) {
        for (var student : enrollment.activeStudentIds(e.teachingClassId()))
            send(student, "Thay đổi lịch học", e.reason(), "timetable", e.timetableSessionId());
    }

    @EventListener
    public void tuition(TuitionIssued e) {
        send(e.studentId(), "Học phí và thanh toán", e.message(), "tuition", e.feeId());
    }

    @EventListener
    public void grades(GradesPublished e) {
        for (var student : e.studentIds())
            send(
                    student,
                    "Kết quả học tập",
                    "Bảng điểm lớp đã được công bố.",
                    "grade",
                    e.classId());
    }

    private void send(String student, String title, String message, String module, String source) {
        identity.userIdForStudent(student)
                .ifPresent(
                        user ->
                                repository.save(
                                        new NotificationMessage(
                                                UUID.randomUUID().toString(),
                                                user,
                                                title,
                                                message,
                                                module,
                                                source)));
    }

    public NotificationMessageView read(String id) {
        var n =
                repository
                        .findById(id)
                        .orElseThrow(() -> BusinessException.missing("Notification"));
        Access.requireSelf("USER", n.getRecipientId());
        n.markRead();
        audit.record("notification", id, "READ", "Read by recipient");
        return new NotificationMessageView(
                n.getId(),
                n.getRecipientId(),
                n.getTitle(),
                n.getContent(),
                n.getReferenceModule(),
                n.getReferenceId(),
                n.isRead(),
                n.getCreatedAt());
    }
}
