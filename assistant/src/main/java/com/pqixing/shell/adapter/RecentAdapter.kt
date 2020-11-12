//package com.pqixing.shell.adapter
//
//import com.pqixing.space.utils.Constans
//import com.pqixing.space.utils.KUtls
//import com.pqixing.space.utils.getSpValue
//import com.pqixing.space.utils.list
//
//class RecentAdapter : AppItemAdapter() {
//    override fun onConditionChange() {
//        val pkgs = Constans.ITEM_OPEN_RECENT.getSpValue("").list()
//        datas = KUtls.allItems.filter { pkgs.contains(it.packageName) }.sortedBy { pkgs.indexOf(it.packageName) }
//        notifyDataSetChanged()
//    }
//}