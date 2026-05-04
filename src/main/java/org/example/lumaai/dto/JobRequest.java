package org.example.lumaai.dto;

import java.util.List;

public record JobRequest(
        String type,
        String prompt,
        String aspectRatio,
        List<String> referenceUrls,
        String sourceUrl
) {
}
