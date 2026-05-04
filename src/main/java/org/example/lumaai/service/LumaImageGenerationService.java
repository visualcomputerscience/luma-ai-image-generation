package org.example.lumaai.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class LumaImageGenerationService implements ImageGenerationService {

    private final WebClient lumaClient;
    private final WebClient downloadClient;

    public LumaImageGenerationService(@Value("${luma.api.base-url}") String baseUrl,
                                      @Value("${luma.api.key}") String apiKey) {
        this.lumaClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
        this.downloadClient = WebClient.builder()
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(c -> c.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                        .build())
                .build();
    }

    @Override
    public Mono<String> generate(String prompt, String aspectRatio) {
        return submitGeneration(Map.of(
                "prompt", prompt,
                "aspect_ratio", aspectRatio
        )).flatMap(this::pollUntilComplete);
    }

    @Override
    public Mono<String> generateWithReferences(String prompt, String aspectRatio,
                                               List<String> referenceUrls) {
        var refs = referenceUrls.stream()
                .map(url -> Map.of("url", url))
                .toList();

        return submitGeneration(Map.of(
                "prompt", prompt,
                "aspect_ratio", aspectRatio,
                "image_ref", refs
        )).flatMap(this::pollUntilComplete);
    }

    @Override
    public Mono<String> editImage(String sourceUrl, String editPrompt) {
        return submitGeneration(Map.of(
                "type", "image_edit",
                "prompt", editPrompt,
                "source", Map.of("url", sourceUrl)
        )).flatMap(this::pollUntilComplete);
    }

    @Override
    public byte[] downloadImage(String imageUrl) {
        return downloadClient.get()
                .uri(URI.create(imageUrl))
                .retrieve()
                .bodyToMono(byte[].class)
                .block();
    }

    private Mono<String> submitGeneration(Map<String, Object> body) {
        return lumaClient.post()
                .uri("/generations")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(response -> response.get("id").asText());
    }

    private Mono<String> pollUntilComplete(String generationId) {
        return Mono.defer(() -> lumaClient.get()
                        .uri("/generations/{id}", generationId)
                        .retrieve()
                        .bodyToMono(JsonNode.class)
                        .flatMap(result -> {
                            var state = result.get("state").asText();
                            if ("completed".equals(state)) {
                                return Mono.just(result.get("output").get(0).get("url").asText());
                            }
                            if ("failed".equals(state)) {
                                return Mono.error(new RuntimeException("Image generation failed"));
                            }
                            return Mono.empty();
                        }))
                .repeatWhenEmpty(companion -> companion.delayElements(Duration.ofSeconds(2)))
                .timeout(Duration.ofMinutes(5));
    }
}
