package org.example.lumaai.model;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Instant;

public class ImageJob {

    private final String id;
    private final Instant createdAt;
    private final Sinks.Many<JobStatus> statusSink = Sinks.many().replay().latest();
    private volatile JobStatus status;
    private volatile String imageUrl;
    private volatile String errorMessage;

    public ImageJob(String id) {
        this.id = id;
        this.createdAt = Instant.now();
        this.status = JobStatus.PENDING;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
        statusSink.tryEmitNext(status);
        if (status == JobStatus.COMPLETED || status == JobStatus.FAILED) {
            statusSink.tryEmitComplete();
        }
    }

    public String getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public JobStatus getStatus() {
        return status;
    }

    public Flux<JobStatus> statusStream() {
        return statusSink.asFlux();
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
