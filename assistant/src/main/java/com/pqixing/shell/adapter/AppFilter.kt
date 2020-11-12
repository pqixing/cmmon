package com.pqixing.shell.adapter

import com.pqixing.space.database.AppItem
import com.pqixing.space.utils.runOrNull
import com.pqixing.space.utils.toBooleanOrNull

class AppFilter() {
    var pkgRex: Regex? = null
    var enable: Boolean? = null
    var system: Boolean? = null
    var exclude: Boolean? = null
    var launch: Boolean? = null

    var searchKey: String? = null
        set(value) {
            field = value
            adapter?.onConditionChange()
        }

    var adapter: AppItemAdapter? = null

    var userFilter: Boolean = false
    var pkgInvert: Boolean = false

    var pkgs = listOf<String>()

    fun toFilterStr(): String =
        "userFilter:$userFilter,enable:$enable,pkg:${pkgRex?.pattern
            ?: ""},exclude:$exclude,system:$system,pkgInvert:$pkgInvert"


    fun initByCmd(cmd: String?, searchKey: String? = null, pkgs: List<String> = listOf()) {
        cmd ?: return
        val fm = cmd.split(",").map { m ->
            val temp = m.trim().split(":")
            Pair(temp.first(), temp.getOrNull(1))
        }.toMap()
        pkgRex = runOrNull {
            val r = fm["pkg"] ?: ""
            if (r == "null" || r.isEmpty()) null else Regex(r)
        }
        enable = fm["enable"]?.toBooleanOrNull()
        system = fm["system"]?.toBooleanOrNull()
        exclude = fm["exclude"]?.toBooleanOrNull()
        userFilter = fm["userFilter"]?.toBooleanOrNull() ?: false
        pkgInvert = fm["pkgInvert"]?.toBooleanOrNull() ?: false

        this.pkgs = pkgs
        this.searchKey = searchKey
        adapter?.onConditionChange()
    }


    fun match(item: AppItem): Boolean = if (searchKey?.isNotEmpty() == true) {
        item.matchKey.contains(searchKey!!)
    } else (userFilter
            && (enable == null || enable == item.enable)
            && (system == null || system == item.system)
            && (exclude == null || exclude == item.exclude)
            && (launch == null || launch == item.launch)
            && pkgRex?.matches(item.packageName)?.let { if (pkgInvert) !it else it } ?: true)
            || pkgs.contains(item.packageName)
}