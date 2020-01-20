package uni.jena.swep.ehealth.data_visualisation;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.threeten.bp.LocalDateTime;

@Entity(tableName = "totalstepshourly")
public class TotalStepHourly {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private int number_steps;
    private LocalDateTime timestamp;

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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
