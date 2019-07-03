package tatar.ru.simpletracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainService";
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ssZ";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private WorkManager mWorkManager;
    private MessageReceiver mMessageReceiver;

    private TextView mTextViewPoints;
    private File mFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextViewPoints = findViewById(R.id.textview_points);
        mMessageReceiver = new MessageReceiver();
        mFile = new File(
                android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS).getAbsoluteFile(),
                "coordinates.log"
        );

        subscribeToMessage();
        startService();
        startWorker();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!checkPlayServices()) {
            appendLog("You need to install Google Play Services to use the App properly");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unsubscribeFromMessage();
        stopService();
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
        mWorkManager = WorkManager.getInstance();
        mWorkManager.enqueue(
                new PeriodicWorkRequest.Builder(
                        LocationsWorker.class,
                        LocationsWorker.RUN_INTERVAL_SECONDS,
                        TimeUnit.SECONDS
                ).build()
        );
    }

    private void subscribeToMessage() {
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(Constants.ACTION_MESSAGE_BROADCAST));
    }

    private void unsubscribeFromMessage() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    private void appendLog(String s) {
        String line = "\n" + s;
        writeToFile(line);

        mTextViewPoints.append(line + "\n");
    }

    private void writeToFile(String s) {
        FileOutputStream stream;
        try {
            stream = new FileOutputStream(mFile, true);
            try {
                stream.write(s.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
            } else {
                finish();
            }

            return false;
        }

        return true;
    }

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(Constants.EXTRA_MESSAGE);
            appendLog(message);
        }
    }
}
