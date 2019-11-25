package com.example.measure_movement;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface StepDAO {
    @Insert
    public void insert(StepEntity... items);
    @Update
    public void update(StepEntity... items);
    @Delete
    public void delete(StepEntity item);

    @Query("SELECT * FROM steps")
    public List<StepEntity> getItems();
}
