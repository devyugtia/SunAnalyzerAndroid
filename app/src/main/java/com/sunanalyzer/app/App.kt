package com.sunanalyzer.app

import android.app.Application
import android.content.Context
import io.paperdb.Paper

class App : Application() {
    private var mContext: Context? = null

    private var instance: App? = null
    fun getContext(): Context? {
        return mContext
    }

    fun setContext(mctx: Context?) {
        mContext = mctx
    }

    fun getInstance(): App? {
        return instance
    }


    private fun initApplication() {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()


        Paper.init(applicationContext)

    }


}