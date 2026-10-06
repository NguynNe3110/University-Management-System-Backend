package com.university.enrollment.internal.application;

import com.university.enrollment.api.EnrollmentRegistry;
import com.university.enrollment.api.EnrollmentView;
import com.university.enrollment.internal.domain.StudentEnrollment;
import com.university.enrollment.internal.persistence.StudentEnrollmentRepository;
import com.university.teachingclass.api.TeachingClassDirectory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class EnrollmentRegistryImpl implements EnrollmentRegistry {

    private final StudentEnrollmentRepository studentEnrollmentRepository;
    private final TeachingClassDirectory teachingClassDirectory;

    public EnrollmentRegistryImpl(StudentEnrollmentRepository studentEnrollmentRepository, TeachingClassDirectory teachingClassDirectory) {
        this.studentEnrollmentRepository = studentEnrollmentRepository;
        this.teachingClassDirectory = teachingClassDirectory;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EnrollmentView> findById(String id) {
        return studentEnrollmentRepository.findById(id).map(this::mapToView);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentView> findByStudentId(String studentId) {
        return studentEnrollmentRepository.findByStudentId(studentId).stream().map(this::mapToView).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentView> findByTeachingClassId(String teachingClassId) {
        return studentEnrollmentRepository.findByTeachingClassId(teachingClassId).stream().map(this::mapToView).toList();
    }

    @Override
    public EnrollmentView enroll(String studentId, String teachingClassId) {
        Optional<StudentEnrollment> existing = studentEnrollmentRepository.findByStudentIdAndTeachingClassId(studentId, teachingClassId);
        if (existing.isPresent()) {
            StudentEnrollment enrollment = existing.get();
            if ("ENROLLED".equalsIgnoreCase(enrollment.getStatus())) {
                return mapToView(enrollment);
            }
        }

        int capacity = teachingClassDirectory.getCapacity(teachingClassId);
        long activeCount = studentEnrollmentRepository.countByTeachingClassIdAndStatus(teachingClassId, "ENROLLED");
        if (capacity > 0 && activeCount >= capacity) {
            throw new IllegalStateException("Lớp học phần đã đủ sĩ số tối đa (" + capacity + "). Không thể đăng ký thêm.");
        }

        StudentEnrollment newEnrollment = new StudentEnrollment(
            UUID.randomUUID().toString(),
            studentId,
            teachingClassId,
            "ENROLLED"
        );
        StudentEnrollment saved = studentEnrollmentRepository.save(newEnrollment);
        return mapToView(saved);
    }

    @Override
    public EnrollmentView cancel(String enrollmentId) {
        StudentEnrollment enrollment = studentEnrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bản ghi đăng ký: " + enrollmentId));
        enrollment.cancel();
        return mapToView(enrollment);
    }

    private EnrollmentView mapToView(StudentEnrollment e) {
        return new EnrollmentView(e.getId(), e.getStudentId(), e.getTeachingClassId(), e.getStatus(), e.getEnrolledAt());
    }
}
