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

    private FusedLocationProviderClient mFusedLocationClient;

    @SuppressWarnings("FieldCanBeLocal")
    private LocationRequest mLocationRequest;
    private DeferredLocationCallback mDeferredLocationCallback;

    public LocationsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        buildLocationRequest(context);
    }

    @SuppressLint("MissingPermission")
    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "work start");

        Result res;
        try {
            sendPing();
            mFusedLocationClient.flushLocations();
            res = Result.success();
        } catch (Exception exception) {
            Log.e(TAG, "worker error", exception);
            res = Result.failure();
        }

        Log.d(TAG, "work end");
        return res;
    }

    @SuppressLint("MissingPermission")
    private void buildLocationRequest(Context context) {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(Constants.LOCATION_UPDATE_INTERVAL_MS);
        mLocationRequest.setFastestInterval(Constants.LOCATION_UPDATE_INTERVAL_MS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setMaxWaitTime(Constants.WORKER_RUN_INTERVAL_MS);

        mDeferredLocationCallback = new DeferredLocationCallback();

        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest,
                mDeferredLocationCallback,
                Looper.getMainLooper()
        );
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

    private class DeferredLocationCallback extends LocationCallback {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            // получаем накопленное и отписываемся от обновлений до след. запуска
            List<Location> locations = locationResult.getLocations();
            mFusedLocationClient.removeLocationUpdates(mDeferredLocationCallback);

            Log.d(TAG, "locations size: " + locations.size());

            for (Location location : locations) {
                sendLocation(location);
            }
        }

        @Override
        public void onLocationAvailability(LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);
        }
    }
}
