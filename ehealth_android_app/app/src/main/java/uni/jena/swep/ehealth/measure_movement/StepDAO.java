package uni.jena.swep.ehealth.measure_movement;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface StepDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insert(StepEntity... items);
    @Update
    public void update(StepEntity... items);
    @Delete
    public void delete(StepEntity item);
    @Delete
    public void deleteMultiple(List<StepEntity> items);

    @Query(" SELECT * FROM steps WHERE is_synchronized=:is_synchronized")
    public List<StepEntity> getSynchronizedSteps(boolean is_synchronized);
    @Query("SELECT * FROM steps")
    public List<StepEntity> getItems();
    @Query("SELECT * FROM steps WHERE time BETWEEN :min_time AND :max_time")
    public List<StepEntity> getStepsFromTimeInterval(long min_time, long max_time);
    @Query("SELECT * FROM steps WHERE time=( SELECT MAX(time) from steps)")
    public StepEntity getLastStep();
}
