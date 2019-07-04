package tatar.ru.simpletracker;

import android.location.Location;
import android.text.format.DateFormat;

import androidx.annotation.NonNull;

import java.util.Date;

public class Utils {
    static String locationToString(@NonNull Location location) {
        StringBuilder sb = new StringBuilder();

        sb.append("time: ");
        sb.append(Utils.formateDate(new Date()));
        sb.append("; ");

        sb.append("provider: ");
        sb.append(location.getProvider());
        sb.append("; ");

        sb.append("signal time: ");
        sb.append(location.getTime());
        sb.append("-");
        sb.append(Utils.formateDate(new Date(location.getTime())));
        sb.append("; ");

        sb.append("acc: ");
        sb.append(location.getAccuracy());
        sb.append("; ");

        return sb.toString();
    }

    public static String formateDate(@NonNull Date date) {
        return (String) DateFormat.format("yyyy-MM-dd hh:mm:ss", date);
        //return (String) DateFormat.getDateTimeInstance(DATE_FORMAT, date);
    }
}
