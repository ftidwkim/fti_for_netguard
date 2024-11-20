package com.franklinwireless.android.jexkids.home.model

class TimeModel(appname: String?, usedtime: Int?,app_percentage:Double?) {
    private var appname: String
    private var usedtime: Int
    private var app_percentage: Double
    init {
        this.appname = appname!!
        this.usedtime = usedtime!!
        this.app_percentage = app_percentage!!
    }
    fun getAppName(): String? {
        return appname
    }
    fun setAppName(name: String?) {
        appname = name!!
    }
    fun getUsedtime(): Int? {
        return usedtime
    }
    fun setUsedtime(usedtime: Int?) {
        this.usedtime = usedtime!!
    }
    fun getAppPercentage(): Double? {
        return app_percentage
    }
    fun setAppPercentage(app_percentage: Double?) {
        this.app_percentage = app_percentage!!
    }
}