package com.example.measure_movement;


import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface LocationDAO {
    @Insert
    public void insert(LocationEntity... items);
    @Update
    public void update(LocationEntity... items);
    @Delete
    public void delete(LocationEntity item);

    @Query("SELECT * FROM locations")
    public List<LocationEntity> getItems();
}
