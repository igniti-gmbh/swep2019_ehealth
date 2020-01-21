package uni.jena.swep.ehealth.measure_movement;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "steps")
public class StepEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private long time;

    private int amount;

    private boolean is_synchronized;

    public boolean isIs_synchronized() {
        return is_synchronized;
    }

    public void setIs_synchronized(boolean is_synchronized) {
        this.is_synchronized = is_synchronized;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String toString() {
        return "time: " + getTime() + " steps: " + getAmount();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
