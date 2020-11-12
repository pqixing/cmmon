package com.pqixing.space.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "apps")
class AppItem(
    @PrimaryKey @ColumnInfo val packageName: String,
    @ColumnInfo var enable: Boolean = true,
    @ColumnInfo var exclude: Boolean = false,
    @ColumnInfo var launch: Boolean = false,
    @ColumnInfo var system: Boolean = false,
    @ColumnInfo var name: String = "",
    @ColumnInfo var matchKey: String = ""
) {
    var unInstall = false
}

