package com.franklinwireless.android.jexkids.home.model

class EarnModel(title: String?, token: String?) {
    private var title: String
    private var token: String
    init {
        this.title = title!!
        this.token = token!!
    }
    fun getTitle(): String? {
        return title
    }
    fun setTitle(name: String?) {
        title = name!!
    }
    fun getToken(): String? {
        return token
    }
    fun setToken(token: String?) {
        this.token = token!!
    }
}