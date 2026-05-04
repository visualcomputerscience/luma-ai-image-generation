# Luma AI Image Generation API

A Spring Boot REST API that wraps the [Luma AI](https://lumalabs.ai/) image generation service, providing an asynchronous job-based workflow with real-time status streaming via Server-Sent Events (SSE).

## Features

- **Text-to-image generation** — generate images from a text prompt with configurable aspect ratio
- **Reference-based generation** — guide image creation using one or more reference image URLs
- **Image editing** — modify an existing image using a text prompt
- **Async job tracking** — submit a job and poll or stream its status until completion
- **SSE streaming** — receive real-time status updates via Server-Sent Events
- **Image download** — retrieve the generated image directly as PNG bytes

## Tech Stack

- Java 21
- Spring Boot 3.4
- Spring WebFlux (reactive `WebClient` for Luma API calls and SSE streaming)

## Configuration

Set the following properties in `application.properties` or via environment variables:

| Property | Description |
|---|---|
| `luma.api.key` | Your Luma AI API key |
| `luma.api.base-url` | Luma API base URL (default: `https://agents.lumalabs.ai/v1`) |

## API Endpoints

### Submit a job

```
POST /api/jobs
Content-Type: application/json
```

**Request body:**

```json
{
  "type": "generate | reference | edit",
  "prompt": "A sunset over the ocean",
  "aspectRatio": "16:9",
  "referenceUrls": ["https://example.com/ref.png"],
  "sourceUrl": "https://example.com/source.png"
}
```

- `type=generate` — requires `prompt`, optional `aspectRatio`
- `type=reference` — requires `prompt` and `referenceUrls`, optional `aspectRatio`
- `type=edit` — requires `sourceUrl` and `prompt`

**Response (202 Accepted):**

```json
{
  "jobId": "uuid",
  "status": "PROCESSING",
  "imageUrl": null,
  "errorMessage": null,
  "createdAt": "2025-01-01T00:00:00Z"
}
```

### Get job status

```
GET /api/jobs/{jobId}
```

### Stream job status (SSE)

```
GET /api/jobs/{jobId}/stream
Accept: text/event-stream
```

### Download generated image

```
GET /api/jobs/{jobId}/image
```

Returns the image as `image/png` when the job is completed. Returns `409 Conflict` if the job is still in progress.

## Running

```bash
mvn spring-boot:run
```
