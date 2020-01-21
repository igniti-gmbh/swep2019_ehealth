package uni.jena.swep.ehealth.measure_movement;


import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities={LocationEntity.class,StepEntity.class},version=3, exportSchema = false)
public abstract class MovementDatabase extends RoomDatabase {
    public abstract LocationDAO getLocationDAO();
    public abstract StepDAO getStepDAO();
}
