package tatar.ru.simpletracker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private TextView mTextViewPoints;
    private LocationManager mLocationManager;
    private File mFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mTextViewPoints = findViewById(R.id.textview_points);

        mFile = new File(getApplication().getFilesDir(), "coordinates.log");

        startTracking();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopTracking();
    }

    @SuppressLint("MissingPermission")
    private void startTracking() {
        appendLog("started - " + (new Date()).toLocaleString());

        long minTime = 5 * 1000;

        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, 0, this);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, 0, this);
        // mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, minTime, 0, this);
    }

    private void stopTracking() {
        mLocationManager.removeUpdates(this);
    }

    public void onLocationChanged(Location location) {
        appendLog(locationDescription(location));
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public void onProviderEnabled(String provider) {
    }

    public void onProviderDisabled(String provider) {
    }

    private void appendLog(String s) {
        String line = "\n" + s;
        writeToFile(line);
        mTextViewPoints.append(line);
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
}
