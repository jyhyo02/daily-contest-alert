import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DiscordService {
    private static final String WEBHOOK_ENV_KEY = "DISCORD_WEBHOOK_URL";

    public static void sendMessage(String message) throws Exception {
        String webhookUrl = resolveWebhookUrl();

        // 1. JSON 내 특수문자 에러 방지 (이스케이프 처리)
        // 줄바꿈이나 큰따옴표가 있으면 JSON 형식이 깨지는 것을 막아줍니다.
        String safeMessage = message.replace("\\", "\\\\")
                                    .replace("\"", "\\\"")
                                    .replace("\n", "\\n");

        String json = "{\"content\": \"" + safeMessage + "\"}";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

        // 2. 전송 및 응답 확인 (매우 중요)
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // 디버깅을 위한 상태 코드 출력
        System.out.println("디스코드 응답 코드: " + response.statusCode());
        if (response.statusCode() != 204) {
            System.out.println("전송 실패 사유: " + response.body());
        } else {
            System.out.println("디스코드 메시지 전송 성공!");
        }
    }

    private static String resolveWebhookUrl() {
        String webhookUrl = System.getenv(WEBHOOK_ENV_KEY);
        if (webhookUrl == null || webhookUrl.isBlank()) {
            webhookUrl = System.getProperty("discord.webhook.url", "");
        }

        if (webhookUrl.isBlank()) {
            throw new IllegalStateException(
                    "디스코드 웹훅 URL이 없습니다. 환경변수 DISCORD_WEBHOOK_URL 또는 -Ddiscord.webhook.url 설정이 필요합니다.");
        }

        return webhookUrl;
    }
}