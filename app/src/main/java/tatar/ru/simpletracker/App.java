package tatar.ru.simpletracker;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.util.Log;

import androidx.room.Room;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

import tatar.ru.simpletracker.data.AppDatabase;

public class App extends Application {
    private static final String TAG = App.class.getSimpleName();

    public static App instance;
    private static AppDatabase mDatabase;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        instance = this;

        mDatabase = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "simple-tracker-database")
                .build();

        // важно запускать в app
        startService();
        startWorker();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public static App getInstance() {
        return instance;
    }

    private void startService() {
        Intent serviceIntent = new Intent(this, MainService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    private void stopService() {
        Intent serviceIntent = new Intent(this, MainService.class);
        stopService(serviceIntent);
    }

    private void startWorker() {
        Log.d(TAG, "startWorker");

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                LocationsWorker.class,
                Constants.WORKER_RUN_INTERVAL_MS,
                TimeUnit.MICROSECONDS,
                Math.round(Constants.WORKER_RUN_INTERVAL_MS * 0.75),
                TimeUnit.MICROSECONDS
        )
                .addTag(Constants.LOCATION_WORKER_NAME)
                .build();

        WorkManager mWorkManager = WorkManager.getInstance();
        mWorkManager.enqueueUniquePeriodicWork(
                Constants.LOCATION_WORKER_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
        );
    }

    /**
     * prevent having multiple instances of the database opened at the same time.
     */
    public static AppDatabase getDatabase(final Context context) {
        if (mDatabase == null) {
            synchronized (AppDatabase.class) {
                if (mDatabase == null) {
                    mDatabase = Room.databaseBuilder(context,
                            AppDatabase.class, "simple-tracker-database")
                            .build();
                }
            }
        }
        return mDatabase;
    }
}
