package tatar.ru.simpletracker.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Position.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract PositionDao positionDao();
}