package com.pqixing.shell.services

import android.app.Activity
import android.app.AlertDialog
import android.app.Service
import android.content.*
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.IBinder
import android.os.SystemClock
import android.provider.Settings
import android.util.LruCache
import android.widget.ImageView
import androidx.room.Room
import com.github.stuxuhai.jpinyin.PinyinFormat
import com.github.stuxuhai.jpinyin.PinyinHelper
import com.pqixing.shell.AppCommand
import com.pqixing.shell.adapter.AppFilter
import com.pqixing.shell.adapter.AppItemAdapter
import com.pqixing.shell.services.AppListener.OnAppChange
import com.pqixing.space.XSpaceApp
import com.pqixing.space.database.AppDataBase
import com.pqixing.space.database.AppItem
import com.pqixing.space.utils.Cmd
import com.pqixing.space.utils.KUtls
import com.pqixing.space.utils.toast
import java.util.*
import kotlin.Comparator

class AppService : Service(), OnAppChange {
    private val listener = AppListener().setAppChange(this)
    lateinit var emptyIcon: Drawable
    lateinit var pm: PackageManager
    lateinit var dataBase: AppDataBase
    private val icons = LruCache<String, Drawable?>(60)
    private val allApps: MutableList<AppItem> = mutableListOf();
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(listener)
        dataBase.close()
        appService = null
    }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_PACKAGE_ADDED)
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED)
        registerReceiver(listener, filter)

        pm = packageManager

        emptyIcon = getDrawable(android.R.drawable.ic_dialog_dialer)!!
        dataBase = Room
            .databaseBuilder(applicationContext, AppDataBase::class.java, "room-app-database")
            .allowMainThreadQueries().build()
        appService = this
    }

    fun loadIcon(pkg: String, image: ImageView) {
        icons[pkg]?.let { return image.setImageDrawable(it) }
        image.setImageDrawable(emptyIcon)
        KUtls.exe {
            val icon = pm.getApplicationIcon(pkg)
            icons.put(pkg, icon)
            image.post { image.setImageDrawable(icon) }
        }
    }

    var load = false
    fun filterApps(filter: AppFilter?, notify: Cmd? = null): List<AppItem> {
        val newApps = mutableListOf<AppItem>()
        newApps.addAll(allApps.filter { filter?.match(it) ?: false })
        load = true
        if (allApps.isEmpty()) KUtls.exe {
            val pkg = packageName
            val dao = dataBase.appDao()
            val oldData = dao.loadAll().map { it.packageName to it }.toMap().toMutableMap()
            val updates = mutableMapOf<AppItem, ApplicationInfo>()
            val inserts = mutableListOf<AppItem>()
            allApps.clear()
            allApps += pm.getInstalledApplications(0)
                .filter { it.packageName != pkg }
                .map { info ->
                    (oldData.remove(info.packageName)?.also { updates[it] = info }
                        ?: newItem(info).also { inserts.add(it) }).also {
                        it.enable = info.enabled
                        it.system = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                        it.launch = pm.getLaunchIntentForPackage(it.packageName) != null
                    }
                }.sortedWith(Comparator { o1, o2 -> o1.system.compareTo(o2.system) })

            newApps.clear()
            newApps.addAll(allApps.filter { filter?.match(it) ?: false })
            if (notify != null) XSpaceApp.uiHandler.post(notify)
            var hasChange = false
            for (it in updates) {
                val newName = pm.getApplicationLabel(it.value).toString()
                if (newName != it.key.name) {
                    hasChange = true
                    it.key.name = newName
                    it.key.matchKey = generatorKey(it.key)
                }
            }
            if (hasChange) if (notify != null) XSpaceApp.uiHandler.post(notify)
            //更新数据库
            dao.insert(inserts)
            dao.update(updates.keys.toList())
            dao.delete(oldData.values.toList())
            load = false
        } else load = false
        return newApps
    }

    private fun newItem(info: ApplicationInfo): AppItem = AppItem(info.packageName).also {
        it.name = info.loadLabel(pm).toString()
        it.matchKey = generatorKey(it)
        it.exclude = false
        it.launch = pm.getLaunchIntentForPackage(it.packageName) != null
        it.matchKey = generatorKey(it)
    }

    fun updateStates(save: Boolean) {
        allApps.forEach { it.enable = pm.getApplicationInfo(it.packageName, 0).enabled }
        if (save) dataBase.appDao().update(allApps)
    }

    fun passThrough(adapter: AppItemAdapter, ui: Activity, enable: Boolean) {
        if (allApps.isEmpty()) {
            filterApps(null) { passThrough(adapter, ui, enable) }
            return
        }

        updateStates(false)
        val curApps = adapter.datas.filter { it.enable != enable && !it.exclude }
        val allMatchApps = {
            //所有的过滤器
            val filter = dataBase.groupDao().loadAll().map { it.getFilter() }
            allApps.filter { item -> item.enable == !enable && filter.find { f -> f.match(item) } != null }
        }
        val allApps = allMatchApps()

        val tryRun = { apps: List<AppItem> ->
            pass(enable, apps.map { it.packageName }) {
                ui.runOnUiThread { adapter.notifyDataSetChanged() }
                updateStates(true)
                "change ${apps.filter { it.enable == enable }.size} package status".toast()
            }
        }

        AlertDialog.Builder(ui, android.R.style.Theme_Material_Dialog_Alert).setCustomTitle(
            KUtls.title("${if (enable) "释放" else "隐藏"}应用", this)
        ).setItems(arrayOf("全部区域:${allApps.size}", "当前显示:${curApps.size}")) { d, i ->
            tryRun(if (i == 0) allApps else curApps)
        }.show()
    }


    override fun onPackage(add: Boolean, pkg: String) = KUtls.threadPool.execute {
        if (add) {
            val info = packageManager.getApplicationInfo(pkg, 0)
            val item = newItem(info)
            allApps.add(item)
            allApps.sortWith(Comparator { o1, o2 -> o1.system.compareTo(o2.system) })
            dataBase.appDao().insert(listOf(item))
        } else allApps.find { it.packageName == pkg }?.let { item ->
            allApps.remove(item)
            item.unInstall = true
            dataBase.appDao().delete(listOf(item))
        }
    }

    fun generatorKey(item: AppItem): String {
        val pinyin = PinyinHelper.convertToPinyinString(item.name, ",", PinyinFormat.WITHOUT_TONE)
            .split(",")
            .let { "${it.joinToString("") { s -> s[0].toString() }},${it.joinToString("")}" }
        return "$pinyin,${item.name},${item.packageName}".toLowerCase(Locale.CHINA)
    }

    fun updateItem(item: AppItem) {
        dataBase.appDao().update(listOf(item))
    }

    fun showAppDetail(item: AppItem, context: Context, adapter: AppItemAdapter) {
        val builder = AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert)
        builder.setCustomTitle(
            KUtls.title("${item.name}  (${item.packageName})", context).apply {
                isClickable = true
                setOnClickListener {
                    val clip =
                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clip.setPrimaryClip(ClipData.newPlainText(null, item.packageName))
                    item.packageName.toast()
                }

            })
        val data = arrayOf(
            "打开",
            if (item.enable) "隐藏" else "释放",
            "详细",
            if (item.exclude) "解锁" else "锁定",
            "卸载"

        )
        builder.setItems(data) { d, i ->
            val packageName = item.packageName

            when (data[i]) {
                "打开" -> openOnce(context, item.packageName, item.exclude, false)
                "隐藏" -> pass(
                    false,
                    Collections.singletonList(item.packageName)
                ) {
                    item.enable = false
                    adapter.notifyDataSetChanged()
                }
                "释放" -> pass(
                    true,
                    Collections.singletonList(item.packageName)
                ) {
                    item.enable = true
                    updateItem(item)
                    adapter.notifyDataSetChanged()
                }
                "详细" -> {
                    KUtls.toPkgDetail(packageName, context)
                }
                "解锁" -> {
                    item.exclude = false
                    updateItem(item)
                    adapter.notifyDataSetChanged()
                }
                "锁定" -> {
                    item.exclude = true
                    updateItem(item)
                    adapter.notifyDataSetChanged()
                }
                "卸载" -> {
                    KUtls.unInstallApp(context, item) {
                        adapter.datas = adapter.datas.toMutableList().apply { remove(item) }
                        allApps.remove(item)
                        dataBase.appDao().delete(listOf(item))
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }
        builder.show()
    }

    fun pass(enable: Boolean, pkgs: List<String>, notify: Cmd): Boolean {
        val cmd =
            AppCommand(pkgs.map { "pm ${if (enable) "enable" else "disable"} $it" }
                .toTypedArray(),
                AppCommand.Result { c, _ -> if (c) notify() })
        return KUtls.runCmd(cmd)
    }

    fun openOnce(context: Context, pkg: String, exclude: Boolean, once: Boolean) {
        val pm = context.packageManager
        val openBlock = {
            val forPackage = pm.getLaunchIntentForPackage(pkg)
            if (forPackage != null) {
                "Open $pkg".toast()
                if (context !is Activity) forPackage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(forPackage)
            } else "Not intent to start".toast()
//            if (once) addToOnceList(pkg, exclude)
        }
        try {
            if (pm.getApplicationInfo(pkg, 0).enabled) {
                openBlock()
                return
            }
            if (!pass(true, Collections.singletonList(pkg)) { openBlock() }) {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .addCategory(Intent.CATEGORY_DEFAULT)
                intent.data = Uri.parse("package:$pkg")
                context.startActivity(intent)
                "Not root permission,Please enable app befor open!!".toast()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    companion object {
        private var appService: AppService? = null
        fun my(): AppService {
            if (appService == null) {
                XSpaceApp.app.startService(
                    Intent(
                        XSpaceApp.app, AppService::class.java
                    )
                )
                while (appService == null) SystemClock.sleep(100)
            }
            return appService!!
        }

    }
}