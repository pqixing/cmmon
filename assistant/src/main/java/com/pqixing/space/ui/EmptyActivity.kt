package com.pqixing.space.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle

class EmptyActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifeCall?.onCreate(savedInstanceState, this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        lifeCall?.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifeCall?.onDestroy()
        lifeCall = null
    }

    companion object {
        var lifeCall: OnLifeCall? = null
        fun startEmptyUi(context: Context, lifeCall: OnLifeCall) {
            Companion.lifeCall = lifeCall
            context.startActivity(Intent(context, EmptyActivity::class.java).apply {
                if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }
}

interface OnLifeCall {
    fun onCreate(bundle: Bundle?, emptyUI: Activity) {}
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {}
    fun onDestroy() {}
}
