package uni.jena.swep.ehealth.measure_movement;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "locations")
public class LocationEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private long time;

    private double latitude;

    private double longitude;

    private double altitude;

    private boolean is_synchronized;

    public boolean isIs_synchronized() {
        return is_synchronized;
    }

    public void setIs_synchronized(boolean is_synchronized) {
        this.is_synchronized = is_synchronized;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String toString(){
        return "time: "+getTime()+" lat: "+getLatitude()+" long: "+getLongitude()+" alt: "+getAltitude();

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
