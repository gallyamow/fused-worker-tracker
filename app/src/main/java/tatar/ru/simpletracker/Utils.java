package tatar.ru.simpletracker;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.text.format.DateFormat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Date;

@SuppressWarnings("WeakerAccess")
public class Utils {
    static String locationToString(@NonNull Location location) {
        StringBuilder sb = new StringBuilder();

        sb.append("time: ");
        sb.append(Utils.formatDate(new Date()));
        sb.append("; ");

        sb.append("provider: ");
        sb.append(location.getProvider());
        sb.append("; ");

        sb.append("signal time: ");
        sb.append(location.getTime());
        sb.append("-");
        sb.append(Utils.formatDate(new Date(location.getTime())));
        sb.append("; ");

        sb.append("acc: ");
        sb.append(location.getAccuracy());
        sb.append("; ");

        return sb.toString();
    }

    public static String formatDate(@NonNull Date date) {
        return (String) DateFormat.format("yyyy-MM-dd hh:mm:ss", date);
        //return (String) DateFormat.getDateTimeInstance(DATE_FORMAT, date);
    }

    public static void sendMessage(Context context, String message) {
        Log.d("MESSAGE", message);

        Intent intent = new Intent(Constants.ACTION_MESSAGE_BROADCAST);
        intent.putExtra(Constants.EXTRA_MESSAGE, message);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
