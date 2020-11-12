package com.pqixing.space.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pqixing.shell.adapter.AppFilter
import com.pqixing.space.utils.list
import java.util.*

@Entity(tableName = "groups")
class Group {
    @PrimaryKey
    @ColumnInfo
    var uid: String = UUID.randomUUID().toString()
    @ColumnInfo
    var name: String = ""
    @ColumnInfo
    var filterStr: String = ""
    @ColumnInfo
    var pkgs: String = ""
    fun getFilter(): AppFilter {
        return AppFilter().also { it.initByCmd(filterStr, pkgs = pkgs.list()) }
    }
}

