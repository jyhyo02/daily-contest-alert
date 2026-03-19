import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Scraper {
    private static final String HISTORY_FILE = "sent_ids.txt";
    private static final String API_URL = "https://api2.campuspick.com/find/activity/list";
    private static final String JSON_BODY = """
            {
                "target": 1,
                "limit": 20,
                "offset": 0,
                "categoryId": 108
            }
            """;
    private static final Pattern TITLE_PATTERN = Pattern.compile("\\\"title\\\":\\\"([^\\\"]+)\\\"");
    private static final Pattern ID_PATTERN = Pattern.compile("\\\"id\\\":(\\d+)");

    public record ContestInfo(String id, String title, String link) {}

    public static String scrapeCampuspick() throws Exception {
        List<ContestInfo> latestContests = fetchLatestContests();
        Set<String> sentIds = loadSentIds();

        StringBuilder message = new StringBuilder();
        message.append("🚀 **캠퍼스픽 공모전 새 소식** 🚀\n\n");

        int count = 0;
        List<String> currentIds = new ArrayList<>();

        for (ContestInfo contest : latestContests) {
            String title = contest.title();
            String id = contest.id();

            if (sentIds.contains(id)) continue; // 이미 보낸 ID는 건너뜀

            message.append("📌 **").append(title).append("**\n");
            message.append("🔗 링크: <").append(contest.link()).append(">\n");
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

    public static List<ContestInfo> fetchLatestContests() throws Exception {
        String jsonResponse = requestCampuspick();
        return parseContests(jsonResponse);
    }

    private static String requestCampuspick() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                .POST(HttpRequest.BodyPublishers.ofString(JSON_BODY))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    private static List<ContestInfo> parseContests(String jsonResponse) {
        List<ContestInfo> contests = new ArrayList<>();
        Matcher titleMatcher = TITLE_PATTERN.matcher(jsonResponse);
        Matcher idMatcher = ID_PATTERN.matcher(jsonResponse);

        while (titleMatcher.find() && idMatcher.find()) {
            String title = titleMatcher.group(1);
            String id = idMatcher.group(1);
            String link = "https://www.campuspick.com/activity/view?id=" + id;
            contests.add(new ContestInfo(id, title, link));
        }

        return contests;
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