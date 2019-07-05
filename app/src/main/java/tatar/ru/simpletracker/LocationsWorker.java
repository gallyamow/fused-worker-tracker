package tatar.ru.simpletracker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import tatar.ru.simpletracker.data.Position;
import tatar.ru.simpletracker.data.PositionDao;

public class LocationsWorker extends Worker {
    private static final String TAG = LocationsWorker.class.getSimpleName();

    private FusedLocationProviderClient mFusedLocationClient;

    @SuppressWarnings("FieldCanBeLocal")
    private LocationRequest mLocationRequest;
    private DeferredLocationCallback mDeferredLocationCallback;

    public LocationsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @SuppressLint("MissingPermission")
    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "work start");

        Result res;

        try {
            sendPing();
            requestLocationsAndWait();

            res = Result.success();
        } catch (ExecutionException | InterruptedException exception) {
            Log.e(TAG, "worker error", exception);
            res = Result.failure();
        }

        Log.d(TAG, "work end");
        return res;
    }

    @SuppressLint("MissingPermission")
    private void requestLocationsAndWait() throws ExecutionException, InterruptedException {
        Context context = getApplicationContext();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(Constants.LOCATION_UPDATE_INTERVAL_MS);
        mLocationRequest.setFastestInterval(Constants.LOCATION_UPDATE_INTERVAL_MS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setMaxWaitTime(Constants.WORKER_RUN_INTERVAL_MS);

        mDeferredLocationCallback = new DeferredLocationCallback();

        Task<Void> task = mFusedLocationClient.requestLocationUpdates(
                mLocationRequest,
                mDeferredLocationCallback,
                Looper.getMainLooper()
        );

        Tasks.await(task);
    }

    private void sendPing() {
        String message = "WORKER PING: " + Utils.formatDate(new Date());
        Utils.sendMessage(getApplicationContext(), message);
    }

    private void sendLocation(@NonNull Location location) {
        String message = "WORKER LOCATION: " + Utils.locationToString(location);
        Utils.sendMessage(getApplicationContext(), message);
    }

    private void saveLocations(List<Location> locations) {
        // todo: is App.instance accessible?
        PositionDao positionDao = App.getDatabase(getApplicationContext()).positionDao();

        for (Location location : locations) {
            positionDao.insert(Position.fromLocation("worker", location));
        }
    }

    private class DeferredLocationCallback extends LocationCallback {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            // получаем накопленное и отписываемся от обновлений до след. запуска
            List<Location> locations = locationResult.getLocations();
            mFusedLocationClient.flushLocations();
            mFusedLocationClient.removeLocationUpdates(mDeferredLocationCallback);

            Log.d(TAG, "locations size: " + locations.size());

            saveLocations(locations);
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
