package com.pqixing.shell.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.pqixing.shell.R
import com.pqixing.space.database.AppItem
import com.pqixing.shell.services.AppService
import kotlinx.android.synthetic.main.grideview_main_item1.view.*


open class AppItemAdapter(val view: View?, val filter: AppFilter = AppFilter()) : BaseAdapter() {
    var selectMode = false
    var datas: List<AppItem> = listOf()

    init {
        filter.adapter = this
    }

    open fun onConditionChange() {
        datas = AppService.my().filterApps(filter) { notifyDataSetChanged() }
        notifyDataSetChanged()
    }

    override fun notifyDataSetChanged() {
        view?.visibility = if (datas.isEmpty()) View.GONE else View.VISIBLE
        super.notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val child = convertView ?: LayoutInflater.from(parent.context)
            .inflate(R.layout.grideview_main_item1, parent, false)

        val item = getItem(position)

        AppService.my().run { loadIcon(item.packageName, child.ivIcon) }

        child.ivIcon.isEnabled = item.exclude || item.system
        child.ivIcon.isSelected = item.exclude


        child.tvName.text = item.name.let { it.substring(0.coerceAtLeast(it.length - 6)) }
        child.tvName.paintFlags = if (item.enable) 0 else Paint.STRIKE_THRU_TEXT_FLAG

        child.cbSelect.visibility = if (selectMode) View.VISIBLE else View.GONE
        child.cbSelect.isSelected = selectMode && filter.pkgs.contains(item.packageName)

        return child
    }

    override fun getItem(position: Int) = datas[position]

    override fun getItemId(position: Int): Long = getItem(position).hashCode().toLong()

    override fun getCount(): Int = datas.size

}