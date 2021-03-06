package uni.jena.swep.ehealth.measure_movement;


import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface LocationDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insert(LocationEntity... items);
    @Update
    public void update(LocationEntity... items);
    @Delete
    public void delete(LocationEntity item);
    @Delete
    public void deleteMultiple(List<LocationEntity> items);

    @Query(" SELECT * FROM locations WHERE is_synchronized=:is_synchronized")
    public List<LocationEntity> getSynchronizedLocations(boolean is_synchronized);
    @Query("SELECT * FROM locations")
    public List<LocationEntity> getItems();
}
