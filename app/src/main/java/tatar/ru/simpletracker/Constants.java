package tatar.ru.simpletracker;

@SuppressWarnings("WeakerAccess")
public class Constants {
    public static final String ACTION_MESSAGE_BROADCAST = "tatar.ru.simpletracker.message";
    public static final String EXTRA_MESSAGE = "message";

    public static final int PING_DELAY_MS = 60000;
    public static final int LOCATION_UPDATE_INTERVAL_MS = 5000;
    /**
     * Must be greater than or equal to PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS (900 000)
     */
    public static final int WORKER_RUN_INTERVAL_MS = 900000;
}
