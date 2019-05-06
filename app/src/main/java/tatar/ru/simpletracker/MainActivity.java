package tatar.ru.simpletracker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.Date;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private TextView mTextViewPoints;
    private LocationManager mLocationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mTextViewPoints = findViewById(R.id.textview_points);

        startTracking();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTracking();

    }

    @SuppressLint("MissingPermission")
    private void startTracking() {
        mTextViewPoints.append((new Date()).toLocaleString());

        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
    }

    private void stopTracking() {
        mLocationManager.removeUpdates(this);
    }

    public void onLocationChanged(Location location) {
        mTextViewPoints.append("\n" + locationDescription(location));
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public void onProviderEnabled(String provider) {
    }

    public void onProviderDisabled(String provider) {
    }

    private String locationDescription(Location location) {
        // StringBuilder sb = new StringBuilder();
        // sb.append(location.getLongitude());
        // sb.append("/");
        // sb.append(location.getLatitude());
        // sb.append(location.getAccuracy());
        return location.toString();
    }
}
