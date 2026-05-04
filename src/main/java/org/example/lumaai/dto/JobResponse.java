package org.example.lumaai.dto;

import org.example.lumaai.model.ImageJob;
import org.example.lumaai.model.JobStatus;

import java.time.Instant;

public record JobResponse(
        String jobId,
        JobStatus status,
        String imageUrl,
        String errorMessage,
        Instant createdAt
) {
    public static JobResponse from(ImageJob job) {
        return new JobResponse(
                job.getId(),
                job.getStatus(),
                job.getImageUrl(),
                job.getErrorMessage(),
                job.getCreatedAt()
        );
    }
}
