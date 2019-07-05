package tatar.ru.simpletracker.data;

import android.location.Location;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@SuppressWarnings("WeakerAccess")
@Entity
public class Position {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "lon")
    public double lon;

    @ColumnInfo(name = "lat")
    public double lat;

    @ColumnInfo(name = "alt")
    public double alt;

    @ColumnInfo(name = "provider")
    public String provider;

    @ColumnInfo(name = "time")
    public long time;

    @ColumnInfo(name = "source")
    public String source;

    public Position(double lon, double lat, double alt, String provider, long time, String source) {
        this.lon = lon;
        this.lat = lat;
        this.alt = alt;
        this.provider = provider;
        this.time = time;
        this.source = source;
    }

    public static Position fromLocation(@NonNull String source, @NonNull Location location) {
        return new Position(
                location.getLongitude(),
                location.getLatitude(),
                location.getAltitude(),
                location.getProvider(),
                location.getTime(),
                source
        );
    }
}
