package com.pqixing.space.database;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AppDao {

    @Query("SELECT * FROM apps")
    List<AppItem> loadAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<AppItem> user);

    @Update
    int update(List<AppItem> item);

    @Delete
    int delete(List<AppItem> item);

}
