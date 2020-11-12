package com.pqixing.space.database;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface GroupDao {

    @Query("SELECT * FROM groups")
    List<Group> loadAll();

    @Query("SELECT * FROM groups WHERE uid=:groupId")
    Group queryById(String groupId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Group group);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<Group> group);

    @Update
    int update(Group group);

    @Delete
    int delete(Group group);
    @Delete
    int delete(List<Group> group);

}
