package org.example.lumaai.service;

import org.example.lumaai.dto.JobRequest;
import org.example.lumaai.model.ImageJob;
import org.example.lumaai.model.JobStatus;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JobService {

    private final Map<String, ImageJob> jobs = new ConcurrentHashMap<>();
    private final ImageGenerationService imageGenerationService;

    public JobService(ImageGenerationService imageGenerationService) {
        this.imageGenerationService = imageGenerationService;
    }

    public ImageJob submitJob(JobRequest request) {
        var job = new ImageJob(UUID.randomUUID().toString());
        jobs.put(job.getId(), job);
        job.setStatus(JobStatus.PROCESSING);

        var mono = switch (request.type()) {
            case "generate" -> imageGenerationService.generate(
                    request.prompt(),
                    request.aspectRatio() != null ? request.aspectRatio() : "16:9"
            );
            case "reference" -> imageGenerationService.generateWithReferences(
                    request.prompt(),
                    request.aspectRatio() != null ? request.aspectRatio() : "16:9",
                    request.referenceUrls()
            );
            case "edit" -> imageGenerationService.editImage(
                    request.sourceUrl(),
                    request.prompt()
            );
            default -> throw new IllegalArgumentException("Unknown job type: " + request.type());
        };

        mono.subscribe(
                imageUrl -> {
                    job.setImageUrl(imageUrl);
                    job.setStatus(JobStatus.COMPLETED);
                },
                error -> {
                    job.setErrorMessage(error.getMessage());
                    job.setStatus(JobStatus.FAILED);
                }
        );

        return job;
    }

    public ImageJob getJob(String jobId) {
        return jobs.get(jobId);
    }

    public byte[] downloadJobImage(ImageJob job) {
        return imageGenerationService.downloadImage(job.getImageUrl());
    }
}
