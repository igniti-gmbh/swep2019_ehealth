package com.example.measure_movement;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "steps")
public class StepEntity {


    @PrimaryKey
    private long time;

    private int amount;


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

    public String toString(){
        return "time: " + getTime() + " steps: "+ getAmount();
    }
}
