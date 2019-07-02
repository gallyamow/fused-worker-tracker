package tatar.ru.simpletracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainService";

    private LocationReceiver mLocationReceiver;
    private TextView mTextViewPoints;
    private File mFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLocationReceiver = new LocationReceiver();
        mTextViewPoints = findViewById(R.id.textview_points);

        mFile = new File(
                android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS).getAbsoluteFile(),
                "coordinates.log"
        );

        // mFile = new File(getApplication().getFilesDir(), "coordinates.log");

        subscribeToLocationChanges();
        startService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unsubscribeToLocationChanges();
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

    private void subscribeToLocationChanges() {
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocationReceiver,
                new IntentFilter(MainService.ACTION_BROADCAST));
    }

    private void unsubscribeToLocationChanges() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocationReceiver);
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

    private String locationDescription(Location location) {
        StringBuilder sb = new StringBuilder();

        sb.append("clock: ");
        sb.append((new Date()).toLocaleString());
        sb.append("; ");

        sb.append("getProvider: ");
        sb.append(location.getProvider());
        sb.append("; ");

        sb.append("getTime: ");
        sb.append(location.getTime());
        sb.append("-");
        sb.append((new Date(location.getTime())).toLocaleString());
        sb.append("; ");

        sb.append("elapsed: ");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            sb.append(location.getElapsedRealtimeNanos());
        }
        sb.append("; ");

        sb.append("lon: ");
        sb.append(location.getLongitude());
        sb.append("; ");

        sb.append("lat: ");
        sb.append(location.getLatitude());
        sb.append("; ");

        sb.append("acc: ");
        sb.append(location.getAccuracy());
        sb.append("; ");

        sb.append("bearing: ");
        sb.append(location.getBearing());
        sb.append("; ");


        sb.append("speed: ");
        sb.append(location.getSpeed());
        sb.append("; ");

        // sb.append("getExtras: ");
        // sb.append(location.getExtras().toString());
        // sb.append("; ");

        return sb.toString();
    }

    private class LocationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(MainService.EXTRA_LOCATION);
            if (location != null) {
                appendLog(locationDescription(location));
            }
        }
    }
}
