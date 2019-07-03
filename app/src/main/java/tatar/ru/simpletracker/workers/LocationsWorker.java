package tatar.ru.simpletracker.workers;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Date;

import tatar.ru.simpletracker.Constants;
import tatar.ru.simpletracker.Utils;

public class LocationsWorker extends Worker implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final String TAG = LocationsWorker.class.getSimpleName();

    public static int RUN_INTERVAL = 5;

    @SuppressWarnings("FieldCanBeLocal")
    private static int LOCATION_UPDATE_INTERVAL = 5000;

    private GoogleApiClient mGoogleApiClient;

    @SuppressWarnings("FieldCanBeLocal")
    private LocationRequest mLocationRequest;

    public LocationsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        buildLocationRequest(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            mGoogleApiClient.connect();

            //stop();
            ping();

            return Result.success();
        } catch (Exception exception) {
            Log.e(TAG, "Unable to save image to Gallery", exception);
            return Result.failure();
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended");

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed");
    }


    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void buildLocationRequest(Context context) {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(LOCATION_UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(LOCATION_UPDATE_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setMaxWaitTime(RUN_INTERVAL);

        mGoogleApiClient.connect();
    }

    private void unsubscribe() {
        // if (mGoogleApiClient.isConnected()) {
        //     LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        //     mGoogleApiClient.disconnect();
        // }
    }

    private void ping() {
        String message = "WORKER PING: " + Utils.formateDate(new Date());
        sendMessage(message);
    }

    private void sendMessage(String message) {
        Log.d(TAG, message);

        Intent intent = new Intent(Constants.ACTION_MESSAGE_BROADCAST);
        intent.putExtra(Constants.EXTRA_MESSAGE, message);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }
}
