package com.pqixing.shell.ui

import android.app.Activity
import android.os.Bundle

class AppListActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(AppListView(this))
    }
}