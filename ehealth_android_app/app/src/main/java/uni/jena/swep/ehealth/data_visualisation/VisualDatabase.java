package uni.jena.swep.ehealth.data_visualisation;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities={StepGoal.class, TotalStepDaily.class, TotalStepHourly.class, RoomData.class},version=3, exportSchema = false)
@TypeConverters({LocalDateConverter.class, LocalDateTimeConverter.class})
public abstract class VisualDatabase extends RoomDatabase {
    public abstract VisualDAO getVisualDAO();
}
