package uni.jena.swep.ehealth.data_visualisation;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import org.threeten.bp.LocalDate;

import java.util.List;

@Dao
public interface VisualDAO {
    // total daily steps
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insert(TotalStepDaily... items);
    @Update
    public void update(TotalStepDaily... items);
    @Delete
    public void deleteTotalStepDaily(List<TotalStepDaily> items);

    @Query("SELECT * FROM totalstepsdaily")
    public List<TotalStepDaily> getAllTotalStepsDaily();
    // TODO divide timestamp with 1000 because seconds needed?
    @Query("SELECT * FROM totalstepsdaily ORDER BY datetime(timestamp)")
    public List<TotalStepDaily> getActualDailySteps();

    // total hourly steps
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insert(TotalStepHourly... items);
    @Update
    public void update(TotalStepHourly... items);
    @Delete
    public void deleteTotalStepHourly(List<TotalStepHourly> items);

    @Query("SELECT * FROM totalstepshourly")
    public List<TotalStepHourly> getAllTotalStepsHourly();
    // TODO fix this query and use it instead of getAllSteps...
    @Query("SELECT * FROM totalstepshourly WHERE date(timestamp/1000, 'unixepoch')=:dateInSeconds")
    public List<TotalStepHourly> getHourlyStepsFromDay(long dateInSeconds);
    @Query("SELECT * FROM totalstepshourly WHERE timestamp=:time")
    public List<TotalStepHourly> getHourlyStepsByTimestamp(long time);

    // step goal
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insert(StepGoal item);

    @Query("SELECT * FROM stepgoal")
    public List<StepGoal> getAllStepGoals();

    @Delete
    public void deleteStepGoals(List<StepGoal> step_goals);

    // room data
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insert(RoomData item);

    @Query("SELECT * FROM roomdata")
    public List<RoomData> getAllRoomData();

    @Delete
    public void deleteRoomData(List<RoomData> room_data);
}
