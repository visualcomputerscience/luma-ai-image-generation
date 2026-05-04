package org.example.lumaai.controller;

import org.example.lumaai.dto.JobRequest;
import org.example.lumaai.dto.JobResponse;
import org.example.lumaai.model.JobStatus;
import org.example.lumaai.service.JobService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    public ResponseEntity<JobResponse> submit(@RequestBody JobRequest request) {
        var job = jobService.submitJob(request);
        return ResponseEntity.accepted().body(JobResponse.from(job));
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<JobResponse> getStatus(@PathVariable String jobId) {
        var job = jobService.getJob(jobId);
        if (job == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(JobResponse.from(job));
    }

    @GetMapping(value = "/{jobId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<JobResponse>> stream(@PathVariable String jobId) {
        var job = jobService.getJob(jobId);
        if (job == null) {
            return Flux.empty();
        }
        return job.statusStream()
                .map(status -> ServerSentEvent.<JobResponse>builder()
                        .event("status")
                        .data(JobResponse.from(job))
                        .build());
    }

    @GetMapping("/{jobId}/image")
    public ResponseEntity<byte[]> getImage(@PathVariable String jobId) {
        var job = jobService.getJob(jobId);
        if (job == null) {
            return ResponseEntity.notFound().build();
        }
        if (job.getStatus() != JobStatus.COMPLETED) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        var imageBytes = jobService.downloadJobImage(job);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header("X-Image-Url", job.getImageUrl())
                .body(imageBytes);
    }
}
