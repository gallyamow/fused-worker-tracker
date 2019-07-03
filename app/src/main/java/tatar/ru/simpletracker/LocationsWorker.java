package tatar.ru.simpletracker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.Date;
import java.util.List;

public class LocationsWorker extends Worker {
    private static final String TAG = LocationsWorker.class.getSimpleName();

    /**
     * Must be greater than or equal to PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS (900 000)
     */
    public static int RUN_INTERVAL_SECONDS = 1000;

    @SuppressWarnings("FieldCanBeLocal")
    private static int LOCATION_UPDATE_INTERVAL = 5000;

    private GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderClient mFusedLocationClient;

    @SuppressWarnings("FieldCanBeLocal")
    private LocationRequest mLocationRequest;

    public LocationsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        buildLocationRequest(context);
    }

    @SuppressLint("MissingPermission")
    @NonNull
    @Override
    public Result doWork() {
        try {
            // mGoogleApiClient.connect();
            sendPing();

            mFusedLocationClient.requestLocationUpdates(mLocationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    List<Location> locations = locationResult.getLocations();
                    Log.d(TAG, "locations size: " + locations.size());

                    for (Location location : locations) {
                        sendLocation(location);
                    }
                }

                @Override
                public void onLocationAvailability(LocationAvailability locationAvailability) {
                    super.onLocationAvailability(locationAvailability);
                }
            }, Looper.getMainLooper());

            return Result.success();
        } catch (Exception exception) {
            Log.e(TAG, "worker error", exception);
            return Result.failure();
        }
    }


    // @Override
    // public void onConnectionSuspended(int i) {
    //     Log.d(TAG, "onConnectionSuspended");
    //
    // }
    //
    // @Override
    // public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    //     Log.d(TAG, "onConnectionFailed");
    // }
    //
    // @Override
    // public void onLocationChanged(Location location) {
    //     Log.d(TAG, "onLocationChanged");
    // }
    //
    // @Override
    // public void onStatusChanged(String provider, int status, Bundle extras) {
    //     Log.d(TAG, "onStatusChanged");
    // }
    //
    // @Override
    // public void onProviderEnabled(String provider) {
    //     Log.d(TAG, "onProviderEnabled");
    // }
    //
    // @Override
    // public void onProviderDisabled(String provider) {
    //
    // }

    private void buildLocationRequest(Context context) {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        // mGoogleApiClient = new GoogleApiClient.Builder(context)
        //         .addApi(LocationServices.API)
        //         .addConnectionCallbacks(this)
        //         .addOnConnectionFailedListener(this)
        //         .build();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(LOCATION_UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(LOCATION_UPDATE_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setMaxWaitTime(RUN_INTERVAL_SECONDS);

        // mGoogleApiClient.connect();
    }

    private void unsubscribe() {
        // if (mGoogleApiClient.isConnected()) {
        //     LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        //     mGoogleApiClient.disconnect();
        // }
    }

    private void sendPing() {
        String message = "WORKER PING: " + Utils.formateDate(new Date());
        sendMessage(message);
    }

    private void sendLocation(@NonNull Location location) {
        String message = "WORKER LOCATION: " + Utils.locationToString(location);
        sendMessage(message);
    }

    private void sendMessage(String message) {
        Log.d(TAG, message);

        Intent intent = new Intent(Constants.ACTION_MESSAGE_BROADCAST);
        intent.putExtra(Constants.EXTRA_MESSAGE, message);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }
}
