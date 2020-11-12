package com.pqixing.shell.ui

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import com.pqixing.shell.R
import kotlinx.android.synthetic.main.drawer_content.view.*

class AppListView(context: Context) : FrameLayout(context) {
    init {
        View.inflate(context, R.layout.ui_app_list, this)


    }
}