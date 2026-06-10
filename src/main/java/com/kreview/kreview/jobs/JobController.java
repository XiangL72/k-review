package com.kreview.kreview.jobs;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

  private final JobStatusService jobStatusService;

  public JobController(JobStatusService jobStatusService) {
    this.jobStatusService = jobStatusService;
  }

  @GetMapping("/{jobId}/status")
  public ResponseEntity<?> getStatus(@PathVariable String jobId) {
    String status = jobStatusService.getStatus(jobId);
    if (status == null) {
      return ResponseEntity.status(404).body(Map.of(
          "error", "Job not found",
          "jobId", jobId
      ));
    }
    return ResponseEntity.ok(Map.of(
        "jobId", jobId,
        "status", status
    ));
  }
}