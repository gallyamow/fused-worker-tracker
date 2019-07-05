package tatar.ru.simpletracker.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PositionDao {

    @Insert
    void insert(Position position);


    @Query("SELECT * FROM position WHERE source = 'fused'")
    List<Position> getFused();

    @Query("SELECT * FROM position WHERE source = 'native'")
    List<Position> getNative();

    @Query("DELETE FROM position WHERE source = 'fused'")
    void deleteFused(Position position);

    @Query("DELETE FROM position WHERE source = 'native'")
    void deleteNative(Position position);
}