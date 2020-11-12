package com.pqixing.shell.adapter

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class RunLogAdapter(
    val cmds: List<String>,
    val runRecords: List<String>,
    val block: (txt: String) -> Unit
) : BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView = convertView as? TextView ?: TextView(parent.context).apply {
            setTextColor(Color.WHITE)
            maxLines = 1
            textSize = 15f
        }
        itemView.text = getItem(position)
        itemView.setOnClickListener { block(getItem(position)) }
        return itemView
    }

    override fun getItem(position: Int): String =
        if (position < cmds.size) cmds[position] else if (position == cmds.size) "---------------------------------------------------" else runRecords[position - cmds.size - 1]

    override fun getItemId(position: Int): Long = getItem(position).hashCode().toLong()

    override fun getCount(): Int = cmds.size + runRecords.size + 1

}