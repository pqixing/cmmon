package com.pqixing.shell.ui

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.fastjson.JSON
import com.pqixing.shell.R
import com.pqixing.shell.adapter.AppItemAdapter
import com.pqixing.shell.adapter.MainMenuAdapter
import com.pqixing.shell.services.AppService
import com.pqixing.space.XSpaceApp
import com.pqixing.space.database.Group
import com.pqixing.space.ui.FloatTouchManager
import com.pqixing.space.utils.*
import kotlinx.android.synthetic.main.ui_main.*
import kotlin.system.exitProcess


class MainActivity : Activity() {
    lateinit var adapter: AppItemAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(0, 0);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ui_main)
        initMenu()
        initContent()
        etSearch.requestFocus()
        startService(Intent(this, AppService::class.java))
        XSpaceApp.uiHandler.postDelayed({ reloadGroups(null) }, 1500)

    }

    private fun initMenu() {
        lvMenu.layoutManager =
            LinearLayoutManager(applicationContext, RecyclerView.HORIZONTAL, false)
        lvMenu.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL)
                .also { it.setDrawable(ColorDrawable(resources.getColor(R.color.color_ui_line))) })
        cbFloatMode.isSelected = Constans.SP_KEY_FLOAT_MODE.getSpValue(false)
        cbFloatMode.setOnClickListener { _ ->
            val isCheck = !cbFloatMode.isSelected
            Constans.SP_KEY_FLOAT_MODE.setSpValue(isCheck)
            FloatTouchManager.onOnceFloat(isCheck)
            cbFloatMode.isSelected = isCheck
        }

        cbHorizontalMode.isSelected = Constans.SP_KEY_HORIZONTAL_MODE.getSpValue(false)
        cbHorizontalMode.setOnClickListener { _ ->
            val isCheck = !cbHorizontalMode.isSelected
            Constans.SP_KEY_HORIZONTAL_MODE.setSpValue(isCheck)
            FloatTouchManager.onHorizationFloat(isCheck)
            cbHorizontalMode.isSelected = isCheck
        }
        FloatTouchManager.onHorizationFloat(cbHorizontalMode.isSelected)
        btnNew.setOnClickListener { toEditEmu(null) }
        btnNew.setOnLongClickListener { changeGroup(null);true }

        flTop.setOnClickListener { finish() }
        flTop.setOnLongClickListener { exitProcess(0);true }

        btnConfig.setOnClickListener { onConfig(true) }
        btnConfig.setOnLongClickListener { onConfig(false);true }
    }

    /**
     * 配置管理
     */
    private fun onConfig(expore: Boolean) {
        val clip = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val dao = AppService.my().dataBase.groupDao()
        val allGroups = dao.loadAll()
        if (expore) {
            val jsonStr = JSON.toJSONString(allGroups)
            clip.setPrimaryClip(ClipData.newPlainText(null, jsonStr))
            "导出配置到粘贴板".toast()
            return
        }

        val text =
            clip.primaryClip?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.text?.toString() ?: ""
        val run = kotlin.runCatching {
            val groups = JSON.parseArray(text, Group::class.java)
            dao.delete(allGroups)
            dao.insert(groups)
            lvMenu.adapter = MainMenuAdapter(this, groups)
        }
        "导入${if (run.isFailure) "失败" else "成功"}:$text".toast()
    }

    fun toEditEmu(groupId: String?) {
        val intent = Intent(this, EditMenuActivity::class.java)
        intent.putExtra(Constans.IT_KEY_NAME_MENU, groupId)
        startActivityForResult(intent, Constans.REQ_EDIT_MENU)
    }

    fun changeGroup(group: Group?) {
        val filter = adapter.filter
        if (group == null) {
            filter.userFilter = false
            filter.pkgs = emptyList()
            adapter.onConditionChange()
            return
        }
        filter.initByCmd(group.filterStr, adapter.filter.searchKey, group.pkgs.list())
        adapter.onConditionChange()
    }

    private fun initContent() {
        adapter = AppItemAdapter(gvAppList)
        gvAppList.adapter = adapter
        gvAppList.numColumns =
            if (this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 10 else 5

        gvAppList.setOnItemLongClickListener { v, _, position, _ ->
            val a = v.adapter as AppItemAdapter
            AppService.my().showAppDetail(a.getItem(position), this, a);true
        }
        gvAppList.setOnItemClickListener { v, _, position, _ ->
            val a = v.adapter as AppItemAdapter
            val item = a.getItem(position)
            AppService.my().openOnce(this, item.packageName, item.exclude, true)
        }

        ivDone.setOnLongClickListener {
            AppService.my().passThrough(adapter, this, true);true
        }
        ivDone.setOnClickListener {
            AppService.my().passThrough(adapter, this, false)
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                adapter.filter.searchKey = s.toString().trim()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constans.REQ_EDIT_MENU && resultCode == RESULT_OK) {
            reloadGroups(data?.getStringExtra(Constans.IT_KEY_NAME_MENU))
        }
    }

    private fun reloadGroups(groupId: String?) = KUtls.exe {
        val allGroups = AppService.my().dataBase.groupDao().loadAll()
        runOnUiThread {
            changeGroup(allGroups.find { it.uid == groupId })
            lvMenu.adapter = MainMenuAdapter(this, allGroups)
        }
    }

}

