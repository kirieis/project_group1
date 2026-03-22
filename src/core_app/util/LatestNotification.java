package core_app.util;

public class LatestNotification {
    private static String message = null;
    private static long timestamp = 0; // epoch millis when notification was set

    public static synchronized String getMessage() {
        return message;
    }

    public static synchronized long getTimestamp() {
        return timestamp;
    }

    public static synchronized void setMessage(String msg) {
        message = msg;
        timestamp = System.currentTimeMillis();
    }
}
