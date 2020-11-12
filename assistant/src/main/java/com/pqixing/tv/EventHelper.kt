package com.pqixing.tv

import android.view.KeyEvent
import android.view.View

class EventHelper(private var curFocus: View) {
    private val binds: MutableMap<View, MutableMap<Int, View>> = mutableMapOf()
    private val moveEvents = arrayOf(
        KeyEvent.KEYCODE_DPAD_LEFT,
        KeyEvent.KEYCODE_DPAD_RIGHT,
        KeyEvent.KEYCODE_DPAD_DOWN,
        KeyEvent.KEYCODE_DPAD_UP
    )
    private val clickEvents = arrayOf(
        KeyEvent.KEYCODE_DPAD_CENTER,
        KeyEvent.KEYCODE_ENTER
    )

    /**
     *
     * @return 返回上次绑定的View
     */
    fun bindView(bind: View, target: View, vararg keyCode: Int) {
        val oldMaps = binds[bind] ?: mutableMapOf()
        for (key in keyCode) oldMaps[key] = target
        binds[bind] = oldMaps
    }

    fun onKey(keyCode: Int, event: KeyEvent): Boolean {
        if (!moveEvents.contains(keyCode) && !clickEvents.contains(keyCode)) return false
        val handle = curFocus.dispatchKeyEvent(event)
        //抬起时判断是否需要切换焦点,只有移动时才需要切换焦点
        if (!handle && event.action == KeyEvent.ACTION_UP && moveEvents.contains(keyCode)) {
            if (moveEvents.contains(keyCode)) findTarget(curFocus, keyCode)?.also { new ->
                if (new != curFocus) {
                    curFocus.isSelected = false
                    new.isSelected = true
                }
                curFocus = new
            } else if (clickEvents.contains(keyCode)) {//模拟点击事件
                curFocus.performClick()
            }
        }
        return handle
    }

    fun findTarget(cur: View, keyCode: Int): View? = binds[cur]?.get(keyCode)
}