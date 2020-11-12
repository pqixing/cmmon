package com.pqixing.shell.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.RadioButton
import com.pqixing.shell.R
import com.pqixing.shell.adapter.AppFilter
import com.pqixing.shell.adapter.AppItemAdapter
import com.pqixing.shell.services.AppService
import com.pqixing.space.database.AppItem
import com.pqixing.space.database.Group
import com.pqixing.space.database.GroupDao
import com.pqixing.space.utils.*
import kotlinx.android.synthetic.main.ui_edit_content.*
import kotlinx.android.synthetic.main.ui_edit_filter.*

class EditMenuActivity : Activity() {
    var groupId: String? = null
    lateinit var dao: GroupDao
    val filter: AppFilter = AppFilter().apply {
        system = false
        userFilter = true
        exclude = false
        enable = true
    }
    var adapter = AppItemAdapter(null, filter).apply { selectMode = true }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ui_edit_menu)
        groupId = intent.getStringExtra(Constans.IT_KEY_NAME_MENU)
        dao = AppService.my().dataBase.groupDao()
        if (groupId != null) {
            val group = dao.queryById(groupId)
            etTitle.setText(group.name)
            filter.initByCmd(group.filterStr, null, group.pkgs.list())
        }
        syncConditionAndUi(false)
        val list = AppService.my().filterApps(filter) { adapter.notifyDataSetChanged() }
        initUiByItems(list)

        cbFilter.setOnCheckedChangeListener { _, _ -> syncConditionAndUi() }
        tvCount.text = filter.pkgs.size.toString()

        gvAppList.setOnItemClickListener { _, _, i, _ ->
            val item = adapter.getItem(i)
            val allSelect = filter.pkgs.toMutableList()

            if (!allSelect.remove(item.packageName)) {
                allSelect.add(item.packageName)
            }
            filter.pkgs = allSelect

            tvCount.text = allSelect.size.toString()
            adapter.notifyDataSetChanged()
        }
        gvAppList.setOnItemLongClickListener { _, _, i, _ ->
            AppService.my().showAppDetail(adapter.getItem(i), this, adapter)
            true
        }
        ivDel.visibility = if (groupId != null) View.VISIBLE else View.GONE
        ivDel.setOnClickListener { onDelClick() }
        ivSave.setOnClickListener { saveGroup() }
        cbPkgInvert.setOnCheckedChangeListener { _, _ -> syncConditionAndUi() }
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                syncConditionAndUi(true)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        }
        etSearch.addTextChangedListener(watcher)
        etSearchPkg.addTextChangedListener(watcher)
    }

    private fun initUiByItems(datas: List<AppItem>) {
        adapter.datas = datas
        gvAppList.adapter = adapter

        rgEnable.setOnCheckedChangeListener { _, _ -> syncConditionAndUi() }
        rgExclude.setOnCheckedChangeListener { _, _ -> syncConditionAndUi() }
        rgSystem.setOnCheckedChangeListener { _, _ -> syncConditionAndUi() }
        rgLaunch.setOnCheckedChangeListener { _, _ -> syncConditionAndUi() }

    }

    /**
     * 同步
     */
    fun syncConditionAndUi(ui2filter: Boolean = true) {
        if (ui2filter) {
            filter.searchKey = etSearch.text.toString().trim()
            filter.enable = findViewById<RadioButton>(rgEnable.checkedRadioButtonId)?.text
                ?.toString()?.toBooleanOrNull()
            filter.system = findViewById<RadioButton>(rgSystem.checkedRadioButtonId)?.text
                ?.toString()?.toBooleanOrNull()

            filter.exclude = findViewById<RadioButton>(rgExclude.checkedRadioButtonId)?.text
                ?.toString()?.toBooleanOrNull()
            filter.launch = findViewById<RadioButton>(rgLaunch.checkedRadioButtonId)?.text
                ?.toString()?.toBooleanOrNull()
            filter.userFilter = cbFilter.isChecked

            filter.pkgRex = runOrNull {
                val r = etSearchPkg.text.toString().trim()
                if (r.isNotEmpty()) Regex(r) else null
            }
            filter.pkgInvert = cbPkgInvert.isChecked

            adapter.onConditionChange()
        } else {
            cbFilter.isChecked = filter.userFilter
            (rgEnable.getChildAt(if (filter.enable == null) 2 else if (filter.enable == true) 0 else 1) as RadioButton).isChecked =
                true
            (rgExclude.getChildAt(if (filter.exclude == null) 2 else if (filter.exclude == true) 0 else 1) as RadioButton).isChecked =
                true
            (rgSystem.getChildAt(if (filter.system == null) 2 else if (filter.system == true) 0 else 1) as RadioButton).isChecked =
                true
            (rgLaunch.getChildAt(if (filter.launch == null) 2 else if (filter.launch == true) 0 else 1) as RadioButton).isChecked =
                true
            etSearchPkg.setText(filter.pkgRex?.pattern)
            cbPkgInvert.isChecked = filter.pkgInvert
        }
    }

    private fun saveGroup() {
        val newGroupName = etTitle.text.toString().trim()
        if (newGroupName.isEmpty()) {
            "Title can not be empty !!!".toast()
            return
        }
        val group = if (groupId != null) dao.queryById(groupId) else Group()
        group.name = newGroupName
        group.filterStr = filter.toFilterStr()
        group.pkgs = filter.pkgs.toStr()
        if (groupId != null) dao.update(group) else dao.insert(group)

        setResult(RESULT_OK, Intent().putExtra(Constans.IT_KEY_NAME_MENU, group.uid))
        finish()
    }

    private fun onDelClick() {
        val groupName = etTitle.text.toString().trim()
        AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
            .setCustomTitle(KUtls.title("Delete $groupName ??", this))
            .setPositiveButton("Not") { d, i -> d.dismiss() }
            .setNegativeButton("Yes") { d, i ->
                d.dismiss()
                if (groupId != null) dao.delete(dao.queryById(groupId))
                setResult(RESULT_OK)
                finish()
            }.show()

    }

}
