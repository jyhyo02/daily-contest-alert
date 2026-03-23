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
        int index = 0;

        for (Scraper.ContestInfo contest : contests) {
            index++;
            String title = normalizeTitle(contest.title());
            cards.append("""
                <li class=\"item\">
                  <span class=\"item-no\">#%d</span>
                  <h3 class=\"item-title\">%s</h3>
                  <div class=\"item-meta\">
                    <span>ID %s</span>
                    <span>campuspick</span>
                  </div>
                </li>
                """.formatted(
                    index,
                    escapeHtml(title),
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
              <link rel=\"preconnect\" href=\"https://fonts.googleapis.com\" />
              <link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin />
              <link href=\"https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;600;700&family=Noto+Sans+KR:wght@400;500;700&display=swap\" rel=\"stylesheet\" />
              <style>
                :root {
                  --bg: #f3efe7;
                  --paper: #fffdf7;
                  --ink: #171717;
                  --muted: #5c5b57;
                  --brand: #0f766e;
                  --brand-strong: #115e59;
                  --line: #dbd5ca;
                }
                * { box-sizing: border-box; }
                body {
                  margin: 0;
                  font-family: "Space Grotesk", "Noto Sans KR", sans-serif;
                  color: var(--ink);
                  background:
                    radial-gradient(circle at 8%% 12%%, #fff8df 0%%, rgba(255, 248, 223, 0) 45%%),
                    radial-gradient(circle at 92%% 5%%, #d7f5ee 0%%, rgba(215, 245, 238, 0) 40%%),
                    linear-gradient(170deg, #f7f3ea 0%%, var(--bg) 100%%);
                }
                body::before {
                  content: "";
                  position: fixed;
                  inset: 0;
                  pointer-events: none;
                  background-image: linear-gradient(rgba(0, 0, 0, 0.02) 1px, transparent 1px), linear-gradient(90deg, rgba(0, 0, 0, 0.02) 1px, transparent 1px);
                  background-size: 36px 36px;
                  mask-image: linear-gradient(to bottom, rgba(0, 0, 0, 0.35), rgba(0, 0, 0, 0));
                }
                .wrap {
                  max-width: 1080px;
                  margin: 0 auto;
                  padding: 28px 18px 64px;
                }
                .hero {
                  position: relative;
                  overflow: hidden;
                  margin-bottom: 20px;
                  padding: 26px 24px;
                  border-radius: 22px;
                  border: 1px solid var(--line);
                  background: linear-gradient(140deg, #fffef9 0%%, #fff8e9 45%%, #eefaf6 100%%);
                  box-shadow: 0 16px 30px rgba(0, 0, 0, 0.07);
                }
                .hero::after {
                  content: "";
                  position: absolute;
                  width: 180px;
                  height: 180px;
                  right: -50px;
                  top: -60px;
                  border-radius: 999px;
                  background: radial-gradient(circle, rgba(15, 118, 110, 0.25) 0%%, rgba(15, 118, 110, 0) 70%%);
                }
                .kicker {
                  display: inline-flex;
                  align-items: center;
                  gap: 8px;
                  margin-bottom: 12px;
                  padding: 6px 11px;
                  border-radius: 999px;
                  font-size: 12px;
                  font-weight: 700;
                  letter-spacing: 0.08em;
                  text-transform: uppercase;
                  color: var(--brand-strong);
                  background: #ffffff;
                  border: 1px solid #cbe4df;
                }
                .dot {
                  width: 7px;
                  height: 7px;
                  border-radius: 999px;
                  background: var(--brand);
                }
                h1 {
                  margin: 0;
                  font-size: clamp(30px, 6vw, 56px);
                  line-height: 1.04;
                  letter-spacing: -0.035em;
                }
                .hero-sub {
                  margin: 12px 0 0;
                  color: var(--muted);
                  font-family: "Noto Sans KR", sans-serif;
                  font-size: clamp(14px, 2vw, 17px);
                }
                .hero-cta {
                  display: inline-flex;
                  align-items: center;
                  justify-content: center;
                  margin-top: 16px;
                  padding: 11px 16px;
                  border-radius: 12px;
                  background: linear-gradient(135deg, var(--brand) 0%%, #0a8d83 100%%);
                  color: #ffffff;
                  text-decoration: none;
                  font-size: 14px;
                  font-weight: 700;
                  letter-spacing: 0.01em;
                  box-shadow: 0 10px 18px rgba(15, 118, 110, 0.25);
                  transition: transform 180ms ease, box-shadow 180ms ease;
                }
                .hero-cta:hover {
                  transform: translateY(-2px);
                  box-shadow: 0 14px 20px rgba(15, 118, 110, 0.30);
                }
                .stats {
                  margin-top: 16px;
                  display: flex;
                  flex-wrap: wrap;
                  gap: 10px;
                }
                .stat {
                  padding: 9px 12px;
                  font-size: 13px;
                  font-weight: 600;
                  border: 1px solid var(--line);
                  border-radius: 11px;
                  background: #fff;
                  color: #2a2a2a;
                }
                .list {
                  list-style: none;
                  margin: 0;
                  padding: 0;
                  display: grid;
                  grid-template-columns: repeat(12, minmax(0, 1fr));
                  gap: 12px;
                }
                .item {
                  grid-column: span 6;
                  position: relative;
                  background: var(--paper);
                  border: 1px solid var(--line);
                  border-radius: 15px;
                  padding: 14px 14px 12px;
                  box-shadow: 0 10px 22px rgba(0, 0, 0, 0.06);
                  animation: rise 420ms ease both;
                  transition: transform 220ms ease, box-shadow 220ms ease, border-color 220ms ease;
                }
                .item:nth-child(odd) { animation-delay: 40ms; }
                .item:nth-child(even) { animation-delay: 100ms; }
                .item:hover {
                  transform: translateY(-3px);
                  box-shadow: 0 16px 28px rgba(0, 0, 0, 0.08);
                  border-color: #c8e6df;
                }
                .item-no {
                  display: inline-block;
                  margin-bottom: 8px;
                  padding: 3px 8px;
                  border-radius: 999px;
                  background: #edf9f6;
                  color: var(--brand-strong);
                  font-size: 11px;
                  font-weight: 700;
                }
                .item-title {
                  margin: 0;
                  color: var(--ink);
                  font-family: "Noto Sans KR", sans-serif;
                  font-size: 17px;
                  font-weight: 700;
                  line-height: 1.4;
                }
                .item-meta {
                  margin-top: 11px;
                  display: flex;
                  justify-content: space-between;
                  gap: 8px;
                  color: var(--muted);
                  font-size: 12px;
                }
                .item-meta span:last-child { text-transform: uppercase; letter-spacing: 0.05em; }
                @keyframes rise {
                  from { transform: translateY(10px); opacity: 0; }
                  to { transform: translateY(0); opacity: 1; }
                }
                @media (max-width: 860px) {
                  .item { grid-column: span 12; }
                }
              </style>
            </head>
            <body>
              <main class=\"wrap\">
                <section class=\"hero\">
                  <span class=\"kicker\"><span class=\"dot\"></span> Daily Contest Alert</span>
                  <h1>오늘의 공모전 소식</h1>
                  <p class=\"hero-sub\">캠퍼스픽 기준 최신 공모전 목록을 매일 자동 갱신합니다.</p>
                  <a class=\"hero-cta\" href=\"https://www.campuspick.com/contest\" target=\"_blank\" rel=\"noopener noreferrer\">캠퍼스픽 공모전 페이지 열기</a>
                  <div class=\"stats\">
                    <span class=\"stat\">최근 갱신 %s (KST)</span>
                    <span class=\"stat\">총 %d건</span>
                  </div>
                </section>
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

    private static String normalizeTitle(String value) {
      return value
          .replace("&lt;", "<")
          .replace("&gt;", ">")
          .replace("&amp;", "&");
    }
}
