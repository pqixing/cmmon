package com.pqixing.shell.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pqixing.shell.R
import com.pqixing.space.database.Group
import com.pqixing.shell.ui.MainActivity

class MainMenuAdapter(val ui: MainActivity,val groups: List<Group>) : RecyclerView.Adapter<VH>() {

    override fun getItemId(p0: Int): Long = p0.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            LayoutInflater.from(parent.context).inflate(R.layout.ui_main_menu_item, parent, false)
        )
    }

    override fun getItemCount(): Int = groups.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.title.text = groups[position].name
        holder.itemView.setOnClickListener { ui.changeGroup(groups[position]) }
        holder.itemView.setOnLongClickListener { ui.toEditEmu(groups[position].uid);true }
    }
}

class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val title: TextView = itemView as TextView
}

