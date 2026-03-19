import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Scraper {
    private static final String HISTORY_FILE = "sent_ids.txt";

    public static String scrapeCampuspick() throws Exception {
        String url = "https://api2.campuspick.com/find/activity/list";
        
        // 1. 확인하신 Payload 값을 정확히 JSON으로 변환
        String jsonBody = """
                {
                    "target": 1,
                    "limit": 20,
                    "offset": 0,
                    "categoryId": 108
                }
                """; 
        Set<String> sentIds = loadSentIds();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String jsonResponse = response.body();

        // 결과가 오는지 콘솔에 출력
        System.out.println("응답 데이터: " + jsonResponse);

        StringBuilder message = new StringBuilder();
        message.append("🚀 **캠퍼스픽 공모전 새 소식** 🚀\n\n");

        // JSON에서 title과 id를 추출하는 정규표현식
        Pattern titlePattern = Pattern.compile("\"title\":\"([^\"]+)\"");
        Pattern idPattern = Pattern.compile("\"id\":(\\d+)");
        
        Matcher titleMatcher = titlePattern.matcher(jsonResponse);
        Matcher idMatcher = idPattern.matcher(jsonResponse);

        int count = 0;

        List<String> currentIds = new ArrayList<>();

        while (titleMatcher.find() && idMatcher.find()) {
            String title = titleMatcher.group(1);
            String id = idMatcher.group(1);

            if (sentIds.contains(id)) continue; // 이미 보낸 ID는 건너뜀
            String link = "https://www.campuspick.com/activity/view?id=" + id;

            message.append("📌 **").append(title).append("**\n");
            message.append("🔗 링크: <").append(link).append(">\n");
            message.append("-----------------------------\n\n");
            currentIds.add(id);
            count++;
        }

        if (count > 0) {
            DiscordService.sendMessage(message.toString());
            saveSentIds(currentIds);
            return "새로운 공고 " + count + "건 전송 완료!";
        } else {
            return "새로운 공고가 없습니다.";
        }
    }
    
    private static Set<String> loadSentIds() {
        Set<String> ids = new HashSet<>();
        try {
            if (Files.exists(Paths.get(HISTORY_FILE))){
                ids.addAll(Files.readAllLines(Paths.get(HISTORY_FILE)));
            }
        } catch (IOException e) {
            System.out.println("ID 파일을 읽는 중 오류: " + e.getMessage());
        }
        return ids;
    }

    private static void saveSentIds(List<String> newIds){
        try (FileWriter fw = new FileWriter(HISTORY_FILE, true);
            PrintWriter out = new PrintWriter(fw)){
                for (String id : newIds){
                    out.println(id);
                }
            }catch (IOException e){
                System.out.println("ID 파일을 저장하는 중 오류: " + e.getMessage());
            }
    }
}