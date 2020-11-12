package com.pqixing.space.utils

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.pqixing.shell.AppCommand
import com.pqixing.shell.R
import com.pqixing.space.XSpaceApp
import com.pqixing.space.database.AppItem
import com.stericson.RootShell.RootShell
import java.util.concurrent.Executors

fun String?.list(): List<String> =
    if (this?.trim()?.isNotEmpty() != true) emptyList() else this.split(",,,").toList()

fun Collection<String>?.toStr(): String = this?.joinToString(",,,") ?: ""
fun String?.getSpValue(defValue: String?) = XSpaceApp.getDefSp().getString(this, defValue)
fun String?.getSpValue(defValue: Boolean) = XSpaceApp.getDefSp().getBoolean(this, defValue)
fun String?.getSpValue(defValue: Int) = XSpaceApp.getDefSp().getInt(this, defValue)
fun String?.removeSpValue() = XSpaceApp.getDefSp().edit().remove(this).apply()
fun String?.toBooleanOrNull(): Boolean? =
    if ("true" == this) true else if ("false" == this) false else null

fun String?.toast() =
    XSpaceApp.uiHandler.post { Toast.makeText(
        XSpaceApp.app, this, Toast.LENGTH_SHORT).show() }

fun String?.log(tag: String = "Shell") = Log.d(tag, this)
fun String?.setSpValue(defValue: Boolean) =
    XSpaceApp.getDefSp().edit().putBoolean(this, defValue).apply()

fun String?.setSpValue(defValue: Int) =
    XSpaceApp.getDefSp().edit().putInt(this, defValue).apply()

fun String?.setSpValue(defValue: String?) =
    XSpaceApp.getDefSp().edit().putString(this, defValue).apply()

fun String?.getSpValue(defValue: Set<String>?) = XSpaceApp.getDefSp().getStringSet(this, defValue)
fun String?.setSpValue(defValue: Set<String>?) =
    XSpaceApp.getDefSp().edit().putStringSet(this, defValue).apply()

fun Int.dp2px(): Int = (XSpaceApp.app.resources.displayMetrics.density * this).toInt()

fun <R> runOrNull(block: () -> R?): R? = try {
    block()
} catch (e: Exception) {
    null
}

typealias Cmd = () -> Unit

object KUtls {
    val threadPool = Executors.newSingleThreadExecutor()
    fun exe(cmd: Cmd) = threadPool.execute(cmd)


    fun title(title: String, context: Context) = TextView(context).apply {
        text = title
        textSize = 16f
        setTextColor(Color.WHITE)
        setBackgroundColor(resources.getColor(R.color.colorPrimary))
        val padding = (resources.displayMetrics.density * 10).toInt()
        setPadding(padding * 2, padding, padding, padding)
    }

    fun runCmd(cmd: AppCommand, root: Boolean = true): Boolean {
        if (root && !RootShell.isAccessGiven()) {
            "Not root permission".toast()
            return false
        }
        val shell = RootShell.getShell(root)
        shell.add(cmd)
        return true
    }




    fun toPkgDetail(packageName: String, context: Context) {
        val intent =
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun toUnInstall(packageName: String, context: Context) {
        val intent = Intent(Intent.ACTION_DELETE, Uri.parse("package:$packageName"))
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }


    fun unInstallApp(context: Context, item: AppItem, after: () -> Unit) {
        val builder = AlertDialog.Builder(context);
        builder.setCustomTitle(title(item.name + " ( " + item.packageName + " )", context))
        builder.setMessage("Make sure you want to uninstall this app?")
        builder.setOnCancelListener { it.dismiss() }
        builder.setNegativeButton("No") { d, i -> d.dismiss() }
        builder.setPositiveButton("Yes") { d, i ->
            if (!item.system) toUnInstall(item.packageName, context) else runCmd(
                AppCommand(
                    arrayOf("pm uninstall --user 0 ${item.packageName}"),
                    AppCommand.Result { c, _ -> if (c) after() })
            )
            d.dismiss()
        }.show()
    }
}



