package com.pqixing.space.database;


import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {AppItem.class,Group.class},version = 1,exportSchema = false)
public abstract class AppDataBase extends RoomDatabase {

    public abstract AppDao appDao();
    public abstract GroupDao groupDao();
}
