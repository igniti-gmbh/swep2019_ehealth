package uni.jena.swep.ehealth.data_visualisation;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "stepgoal")
public class StepGoal {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private int number_steps;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNumber_steps() {
        return number_steps;
    }

    public void setNumber_steps(int number_steps) {
        this.number_steps = number_steps;
    }
}
