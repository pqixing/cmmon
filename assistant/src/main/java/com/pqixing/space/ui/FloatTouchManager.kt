package com.pqixing.space.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.TextView
import com.pqixing.shell.R
import com.pqixing.shell.services.AppService
import com.pqixing.shell.ui.MainActivity
import com.pqixing.space.utils.*
import com.pqixing.space.XSpaceApp

object FloatTouchManager {
    val floatViews: HashMap<String, View> = HashMap()

    fun onHorizationFloat(show: Boolean) =
        onFloatChange(
            Constans.VIEW_HORIZONTAL,
            show
        ) { wm, p ->
            p.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            View(XSpaceApp.app)
        }

    fun onOnceFloat(show: Boolean) =
        onFloatChange(
            Constans.VIEW_ONLIST,
            show
        ) { wm, p ->
            p.gravity = Gravity.END or Gravity.TOP
            p.width = 36.dp2px()
            p.height = 36.dp2px()
            p.y = 60.dp2px()
            p.flags =
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL

            val l = Constans.ITEM_OPEN_ONCE.getSpValue(emptySet())!!.toList()
            val onClear = { disable: Boolean ->
                //            str.toast()
                Constans.ITEM_OPEN_ONCE.removeSpValue()
                if (disable) AppService.my()
                    .pass(false, l) {}
                onFloatChange(
                    Constans.VIEW_FLOAT_CLOSE,
                    false
                )
            }
            val onClick = { view: View ->
                val context = view.context
                context.startActivity(
                    Intent(
                        context,
                        MainActivity::class.java
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )

//            val dialog = AlertDialog.Builder(ShellApp.app).setCustomTitle(
//                KUtls.alertTitle(
//                    "Open App List : ${l.size}",
//                    ShellApp.app
//                )
//            ).setItems(l.toTypedArray()) { d, i ->
//                val clip =
//                    ShellApp.app.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//                clip.setPrimaryClip( ClipData.newPlainText(null, l[i]))
//                "Copy : ${l[i]}".toast()
//
//            }.setNegativeButton("Disable") { _, _ -> onClear(true) }
//                .setPositiveButton("Clear") { _, _ -> onClear(false) }.create()
//            dialog.window?.setType(if (Build.VERSION.SDK_INT < 26) WindowManager.LayoutParams.TYPE_SYSTEM_ALERT else WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
//            dialog.show()
            }


            TextView(XSpaceApp.app).apply {
                isFocusable = true
                isClickable = true
                textSize = 14f
                gravity = Gravity.CENTER
                text =
                    Constans.ITEM_OPEN_ONCE.getSpValue(emptySet())!!.toMutableSet().size.toString()
                setTextColor(Color.BLACK)
                setBackgroundResource(R.drawable.circle_half_bg)
                val l = object : GestureDetector.SimpleOnGestureListener() {
                    override fun onSingleTapUp(e: MotionEvent?): Boolean {
                        onClick(this@apply)
                        onFloatChange(
                            Constans.VIEW_ONLIST,
                            false
                        )
                        return true
                    }

                    override fun onScroll(
                        e1: MotionEvent?,
                        e2: MotionEvent,
                        distanceX: Float,
                        distanceY: Float
                    ): Boolean {
                        p.y = (e2.rawY - p.height / 2).toInt()
                        wm.updateViewLayout(this@apply, p)
                        return true
                    }
                }
                val g = GestureDetector(context, l, XSpaceApp.uiHandler)
                setOnTouchListener { v, event -> g.onTouchEvent(event) }

            }
        }

    @Synchronized
    public fun onFloatChange(
        name: String,
        show: Boolean,
        getView: (wm: WindowManager, param: WindowManager.LayoutParams) -> View? = { _, _ -> null }
    ) = runOrNull {
        if (show == (floatViews[name] != null)) return@runOrNull
        val context = XSpaceApp.app
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (!show) return@runOrNull wm.removeView(floatViews.remove(name))

        runAfterGetPermission {

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT < 26) WindowManager.LayoutParams.TYPE_SYSTEM_ALERT else WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.RGBA_8888
            )
//            params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            params.gravity = Gravity.START or Gravity.BOTTOM
            val view = getView(wm, params) ?: return@runAfterGetPermission
            floatViews[name] = view
            wm.addView(view, params)
        }
    }

    fun runAfterGetPermission(block: () -> Unit) {
        val context = XSpaceApp.app

        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(context)) EmptyActivity.startEmptyUi(
            context, object : OnLifeCall {
                override fun onCreate(bundle: Bundle?, emptyUI: Activity) {
                    super.onCreate(bundle, emptyUI)
                    val i = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + context.packageName)
                    )
                    //启动Activity让用户授权
                    emptyUI.startActivityForResult(i, 1)
                }

                override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
                    super.onActivityResult(requestCode, resultCode, data)
                    if (Settings.canDrawOverlays(context)) block()
                }
            }) else block()
    }
}