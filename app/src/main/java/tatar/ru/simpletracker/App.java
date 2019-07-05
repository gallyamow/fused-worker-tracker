package tatar.ru.simpletracker;

import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class App extends Application {
    private static final String TAG = App.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        // важно запускать в app
        startService();
        startWorker();
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

}
