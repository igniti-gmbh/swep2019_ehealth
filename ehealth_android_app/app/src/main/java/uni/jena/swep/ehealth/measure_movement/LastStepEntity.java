package uni.jena.swep.ehealth.measure_movement;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "laststeps")
public class LastStepEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int step_offset;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStep_offset() {
        return step_offset;
    }

    public void setStep_offset(int step_offset) {
        this.step_offset = step_offset;
    }
}
