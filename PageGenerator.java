import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PageGenerator {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) throws Exception {
        List<Scraper.ContestInfo> contests = Scraper.fetchLatestContests();
        writeIndexPage(contests);
        System.out.println("페이지 생성 완료: docs/index.html (" + contests.size() + "건)");
    }

    private static void writeIndexPage(List<Scraper.ContestInfo> contests) throws Exception {
        Path docsDir = Path.of("docs");
        Files.createDirectories(docsDir);

        String updatedAt = LocalDateTime.now(KST).format(TIME_FORMAT);
        StringBuilder cards = new StringBuilder();

        for (Scraper.ContestInfo contest : contests) {
            cards.append("""
                <li class=\"item\">
                  <a href=\"%s\" target=\"_blank\" rel=\"noopener noreferrer\">%s</a>
                  <p>ID: %s</p>
                </li>
                """.formatted(
                    escapeHtml(contest.link()),
                    escapeHtml(contest.title()),
                    escapeHtml(contest.id())
            ));
        }

        String html = """
            <!doctype html>
            <html lang=\"ko\">
            <head>
              <meta charset=\"utf-8\" />
              <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />
              <title>Daily Contest Alert</title>
              <style>
                :root {
                  --bg: #f4f1ea;
                  --card: #fffaf2;
                  --ink: #222222;
                  --muted: #5b5b5b;
                  --accent: #d9480f;
                  --line: #e8dcc8;
                }
                * { box-sizing: border-box; }
                body {
                  margin: 0;
                  font-family: "Pretendard", "Noto Sans KR", sans-serif;
                  color: var(--ink);
                  background: radial-gradient(circle at 10%% 10%%, #fff8ec 0%%, var(--bg) 40%%, #efe9dc 100%%);
                }
                .wrap {
                  max-width: 980px;
                  margin: 0 auto;
                  padding: 32px 20px 56px;
                }
                h1 {
                  margin: 0 0 8px;
                  font-size: clamp(28px, 5vw, 48px);
                  letter-spacing: -0.03em;
                }
                .meta {
                  margin: 0 0 24px;
                  color: var(--muted);
                }
                .badge {
                  display: inline-block;
                  margin-bottom: 14px;
                  padding: 6px 12px;
                  border: 1px solid var(--line);
                  border-radius: 999px;
                  background: #fff;
                  color: var(--accent);
                  font-weight: 700;
                  font-size: 13px;
                }
                .list {
                  list-style: none;
                  margin: 0;
                  padding: 0;
                  display: grid;
                  gap: 12px;
                }
                .item {
                  background: var(--card);
                  border: 1px solid var(--line);
                  border-radius: 14px;
                  padding: 16px;
                  box-shadow: 0 8px 18px rgba(0, 0, 0, 0.05);
                  animation: rise 320ms ease both;
                }
                .item a {
                  color: var(--ink);
                  text-decoration: none;
                  font-weight: 700;
                  line-height: 1.4;
                }
                .item a:hover { color: var(--accent); }
                .item p {
                  margin: 10px 0 0;
                  color: var(--muted);
                  font-size: 13px;
                }
                @keyframes rise {
                  from { transform: translateY(6px); opacity: 0; }
                  to { transform: translateY(0); opacity: 1; }
                }
              </style>
            </head>
            <body>
              <main class=\"wrap\">
                <span class=\"badge\">CAMPUSPICK AUTO UPDATE</span>
                <h1>오늘의 공모전 소식</h1>
                <p class=\"meta\">최근 갱신: %s (KST) · 총 %d건</p>
                <ul class=\"list\">%s</ul>
              </main>
            </body>
            </html>
            """.formatted(escapeHtml(updatedAt), contests.size(), cards);

        Files.writeString(docsDir.resolve("index.html"), html, StandardCharsets.UTF_8);
    }

    private static String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
