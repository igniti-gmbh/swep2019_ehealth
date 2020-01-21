package uni.jena.swep.ehealth.data_visualisation;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.threeten.bp.LocalDate;

@Entity(tableName = "totalstepsdaily")
public class TotalStepDaily {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private int number_steps;
    private LocalDate timestamp;

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

    public LocalDate getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDate timestamp) {
        this.timestamp = timestamp;
    }
}
