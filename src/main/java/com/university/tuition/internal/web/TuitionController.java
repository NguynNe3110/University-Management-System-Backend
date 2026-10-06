package com.university.tuition.internal.web;

import com.university.tuition.api.TuitionFeeView;
import com.university.tuition.api.TuitionLedger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tuition")
public class TuitionController {

    private final TuitionLedger tuitionLedger;

    public TuitionController(TuitionLedger tuitionLedger) {
        this.tuitionLedger = tuitionLedger;
    }

    @GetMapping("/summary")
    public ResponseEntity<TuitionFeeView> getTuitionSummary(@RequestParam String studentId, @RequestParam String semesterId) {
        return tuitionLedger.findByStudentAndSemester(studentId, semesterId)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/by-student/{studentId}")
    public ResponseEntity<List<TuitionFeeView>> getTuitionByStudent(@PathVariable String studentId) {
        return ResponseEntity.ok(tuitionLedger.findByStudentId(studentId));
    }
}
