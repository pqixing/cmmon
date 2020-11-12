package com.pqixing.tv.interfaces

import android.view.KeyEvent
import android.view.View
import android.widget.LinearLayout
import com.pqixing.space.XSpaceApp

typealias OnMove = (v: View, old: Int, new: Int) -> Unit

class MoveHandle(val move: OnMove) : View.OnKeyListener {

    var index: Int = 0
    var size: Int = 0
    var orientation: Int = LinearLayout.VERTICAL
    var grid: Int = 1

    var firstDownIndex = -1
    fun setCount(
        size: Int,
        index: Int = 0,
        orientation: Int = LinearLayout.VERTICAL,
        grid: Int = 1
    ) {
        this.size = size
        this.index = index
        this.orientation = orientation
        this.grid = grid
    }

    override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
        val downAction = event.action == KeyEvent.ACTION_DOWN
        if (downAction && firstDownIndex == -1) {
            firstDownIndex = index
        }
        if (downAction) tryMove(keyCode, event)

        return downAction || firstDownIndex != index
    }

    /**
     * 尝试移动
     */
    private fun tryMove(keyCode: Int, event: KeyEvent) {
        val oldIndex = index
        when(keyCode){

        }

        //如果需要移动
        XSpaceApp.uiHandler.post {  }
    }

}