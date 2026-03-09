package simulator;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Manages configuration for Simulator
 * Reads from simulator.properties or uses default values
 */
public class SimulatorConfig {
    private static final Properties properties = new Properties();

    // Default values
    private static final long DEFAULT_INTERVAL_MS = 1000; // 1 second
    private static final String DEFAULT_ENDPOINT = "https://unplanked-inculpably-malorie.ngrok-free.dev/api/orders";
    private static final boolean DEFAULT_ENABLE_LOG = true;
    private static final boolean DEFAULT_ENABLE_ORDER = true;
    private static final boolean DEFAULT_ENABLE_INVENTORY = true;
    private static final boolean DEFAULT_ENABLE_MEDICINE = true;
    private static final int DEFAULT_MIN_QUANTITY = 50;
    private static final int DEFAULT_MAX_QUANTITY = 100;
    private static final int DEFAULT_INITIAL_QUANTITY = 100;

    static {
        loadProperties();
    }

    /**
     * Load configuration from simulator.properties file
     */
    private static void loadProperties() {
        try (FileInputStream fis = new FileInputStream("simulator.properties")) {
            properties.load(fis);
            System.out.println("[INFO] Loaded simulator.properties successfully");
        } catch (IOException e) {
            System.out.println("[INFO] simulator.properties not found, using default configuration");
        }
    }

    /**
     * Get configuration value or default
     */
    private static String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    private static long getLong(String key, long defaultValue) {
        try {
            String val = properties.getProperty(key, String.valueOf(defaultValue));
            return Long.parseLong(val);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static boolean getBoolean(String key, boolean defaultValue) {
        String val = properties.getProperty(key, String.valueOf(defaultValue));
        return Boolean.parseBoolean(val);
    }

    private static int getInt(String key, int defaultValue) {
        try {
            String val = properties.getProperty(key, String.valueOf(defaultValue));
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // Getter methods
    public static long getIntervalMs() {
        return getLong("simulator.interval.ms", DEFAULT_INTERVAL_MS);
    }

    public static String getEndpoint() {
        return getString("simulator.endpoint", DEFAULT_ENDPOINT);
    }

    public static boolean isEnableLog() {
        return getBoolean("simulator.enable.log", DEFAULT_ENABLE_LOG);
    }

    public static boolean isEnableOrderSimulation() {
        return getBoolean("simulator.enable.order", DEFAULT_ENABLE_ORDER);
    }

    public static boolean isEnableInventoryUpdate() {
        return getBoolean("simulator.enable.inventory", DEFAULT_ENABLE_INVENTORY);
    }

    public static boolean isEnableMedicineAddition() {
        return getBoolean("simulator.enable.medicine", DEFAULT_ENABLE_MEDICINE);
    }

    public static int getMinQuantity() {
        return getInt("simulator.inventory.min.quantity", DEFAULT_MIN_QUANTITY);
    }

    public static int getMaxQuantity() {
        return getInt("simulator.inventory.max.quantity", DEFAULT_MAX_QUANTITY);
    }

    public static int getInitialQuantity() {
        return getInt("simulator.medicine.initial.quantity", DEFAULT_INITIAL_QUANTITY);
    }

    /**
     * Print current configuration
     */
    public static void printConfig() {
        System.out.println("====================================================");
        System.out.println("   SIMULATOR CONFIGURATION");
        System.out.println("====================================================");
        System.out.println("  Interval: " + getIntervalMs() + "ms");
        System.out.println("  Endpoint: " + getEndpoint());
        System.out.println("  Enable Order Simulation: " + isEnableOrderSimulation());
        System.out.println("  Enable Inventory Update: " + isEnableInventoryUpdate());
        System.out.println("  Enable Medicine Addition: " + isEnableMedicineAddition());
        System.out.println("  Min Quantity: " + getMinQuantity());
        System.out.println("  Max Quantity: " + getMaxQuantity());
        System.out.println("====================================================");
        System.out.println();
    }
}
