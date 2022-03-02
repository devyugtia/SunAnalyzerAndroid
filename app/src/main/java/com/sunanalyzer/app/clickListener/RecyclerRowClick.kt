package com.sunanalyzer.app.clickListener

interface RecyclerRowClick {
    fun rowClick(pos: Int, flag: Int)
    fun loadMore(pos: Int)
}