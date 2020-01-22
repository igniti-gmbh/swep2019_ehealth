package uni.jena.swep.ehealth;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class UserData {
    @PrimaryKey(autoGenerate = true)
    private int userId;

    private String email_address;
    private String username;
    private String password;
    private String roomId;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getEmail_address() {
        return email_address;
    }

    public void setEmail_address(String email_address) {
        this.email_address = email_address;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}
