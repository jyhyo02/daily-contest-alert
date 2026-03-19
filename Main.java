import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    private static final String DEFAULT_DAILY_TIME = "09:00";
    private static final String RUN_ONCE_ENV = "RUN_ONCE";

    public static void main(String[] args) {
        if (Boolean.parseBoolean(System.getenv(RUN_ONCE_ENV))) {
            runOnce();
            return;
        }

        LocalTime dailyTime = parseDailyTime(System.getenv("DAILY_ALERT_TIME"));

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        Runtime.getRuntime().addShutdownHook(new Thread(scheduler::shutdown));

        long initialDelaySeconds = calculateInitialDelaySeconds(dailyTime, ZONE);
        long periodSeconds = TimeUnit.DAYS.toSeconds(1);

        System.out.println("자동 알림 스케줄러 시작");
        System.out.println("실행 시간: " + dailyTime + " (" + ZONE + ")");
        System.out.println("첫 실행까지 남은 시간(초): " + initialDelaySeconds);

        scheduler.scheduleAtFixedRate(() -> {
            try {
                String result = Scraper.scrapeCampuspick();
                System.out.println("[자동 실행 결과] " + result);
            } catch (Exception e) {
                System.out.println("[자동 실행 오류] " + e.getMessage());
                e.printStackTrace();
            }
        }, initialDelaySeconds, periodSeconds, TimeUnit.SECONDS);
    }

    private static LocalTime parseDailyTime(String configuredValue) {
        String value = (configuredValue == null || configuredValue.isBlank())
                ? DEFAULT_DAILY_TIME
                : configuredValue.trim();

        try {
            return LocalTime.parse(value);
        } catch (DateTimeParseException e) {
            System.out.println("DAILY_ALERT_TIME 형식이 올바르지 않아 기본값 " + DEFAULT_DAILY_TIME + " 사용");
            return LocalTime.parse(DEFAULT_DAILY_TIME);
        }
    }

    private static long calculateInitialDelaySeconds(LocalTime runTime, ZoneId zone) {
        LocalDateTime now = LocalDateTime.now(zone);
        LocalDateTime nextRun = now.withHour(runTime.getHour())
                .withMinute(runTime.getMinute())
                .withSecond(0)
                .withNano(0);

        if (!nextRun.isAfter(now)) {
            nextRun = nextRun.plusDays(1);
        }

        return Duration.between(now, nextRun).getSeconds();
    }

    private static void runOnce() {
        try {
            String result = Scraper.scrapeCampuspick();
            System.out.println("[1회 실행 결과] " + result);
        } catch (Exception e) {
            System.out.println("[1회 실행 오류] " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}