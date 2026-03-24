package core_app.scheduler;

import core_app.dao.BatchDAO;
import core_app.dao.BatchDAO.BatchInfo;
import core_app.util.EmailUtil;
import core_app.util.LatestNotification;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@WebListener
public class NotificationSchedulerListener implements ServletContextListener {

    private ScheduledExecutorService scheduler;
    private final BatchDAO batchDAO = new BatchDAO();
    private static final String ADMIN_EMAIL = "annguyenbinh325@gmail.com";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        core_app.util.SchemaUpdate.updateSchema();

        scheduler = Executors.newSingleThreadScheduledExecutor();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = calculateNextRunTime(now);

        long initialDelayMinutes = Duration.between(now, nextRun).toMinutes();
        if (initialDelayMinutes < 0) {
            initialDelayMinutes = 0;
        }

        System.out.println("==================================================");
        System.out.println("[NotificationScheduler] Next notification scheduled at: "
                + nextRun.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println("[NotificationScheduler] Initial delay in minutes: " + initialDelayMinutes);
        System.out.println("==================================================");

        scheduler.scheduleAtFixedRate(this::checkAndNotify, initialDelayMinutes, 12 * 60, TimeUnit.MINUTES);
    }

    private LocalDateTime calculateNextRunTime(LocalDateTime now) {
        LocalDateTime run9AM = now.with(LocalTime.of(9, 0, 0));
        LocalDateTime run9PM = now.with(LocalTime.of(21, 0, 0));

        if (now.isBefore(run9AM)) {
            return run9AM;
        } else if (now.isBefore(run9PM)) {
            return run9PM;
        } else {
            return run9AM.plusDays(1);
        }
    }

    private void checkAndNotify() {
        try {
            System.out.println("[NotificationScheduler] Running expiration check task...");
            List<BatchInfo> unsellable = batchDAO.getUnsellableBatches();
            List<BatchInfo> expired = batchDAO.getExpiredBatches();

            if (unsellable.isEmpty() && expired.isEmpty()) {
                System.out.println(
                        "[NotificationScheduler] No expired or near-expired medicines found. No notification sent.");
                LatestNotification.setMessage(null);
                return;
            }

            int totalUnsellable = unsellable.size();
            int totalExpired = expired.size();

            String subject = "🔔 Warning: The medicine is expired/about to expire - Github Pharmacy";
            StringBuilder bodyText = new StringBuilder();

            bodyText.append("The system detects batches of medicine that need processing.:\n\n");

            if (totalExpired > 0) {
                bodyText.append(String.format("🔴 Expired: %d batches.\n", totalExpired));
            }
            if (totalUnsellable > 0) {
                bodyText.append(String.format("🟠 Removed from sale (<= 15 days): %d batches.\n", totalUnsellable));
            }
            bodyText.append("\nPlease log in to the admin system to view details and handle them in time.\n\n");
            bodyText.append("Sincerely,\nGithub Pharmacy System");

            String uiMessage = "The system detects batches of medicine that need processing: ";
            if (totalExpired > 0)
                uiMessage += totalExpired + " batches expired. ";
            if (totalUnsellable > 0)
                uiMessage += totalUnsellable + " batches about to expire.";
            LatestNotification.setMessage(uiMessage.trim());

            EmailUtil.sendEmail(ADMIN_EMAIL, subject, bodyText.toString());

        } catch (Exception e) {
            System.err.println("[NotificationScheduler] Error during execution:");
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
            System.out.println("[NotificationScheduler] Scheduler shutdown successfully.");
        }
    }
}
