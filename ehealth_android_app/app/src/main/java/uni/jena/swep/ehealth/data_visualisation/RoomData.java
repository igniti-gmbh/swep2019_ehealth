package uni.jena.swep.ehealth.data_visualisation;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.threeten.bp.LocalDate;

@Entity(tableName = "roomdata")
public class RoomData {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private double temp;
    private double pressure;
    private double gas;
    private double humidity;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getTemp() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public double getPressure() {
        return pressure;
    }

    public void setPressure(double pressure) {
        this.pressure = pressure;
    }

    public double getGas() {
        return gas;
    }

    public void setGas(double gas) {
        this.gas = gas;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }
}
