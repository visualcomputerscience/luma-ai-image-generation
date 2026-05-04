package org.example.lumaai.service;

import reactor.core.publisher.Mono;

import java.util.List;

public interface ImageGenerationService {

    Mono<String> generate(String prompt, String aspectRatio);

    Mono<String> generateWithReferences(String prompt, String aspectRatio, List<String> referenceUrls);

    Mono<String> editImage(String sourceUrl, String editPrompt);

    byte[] downloadImage(String imageUrl);
}
