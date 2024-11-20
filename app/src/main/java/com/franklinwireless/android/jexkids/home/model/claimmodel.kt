package com.franklinwireless.android.jexkids.home.model

class claimmodel(title: String?, reward: String?) {
    private var title: String
    private var reward: String
    init {
        this.title = title!!
        this.reward = reward!!
    }
    fun getTitle(): String? {
        return title
    }
    fun setTitle(name: String?) {
        title = name!!
    }
    fun getReward(): String? {
        return reward
    }
    fun setReward(reward: String?) {
        this.reward = reward!!
    }
}