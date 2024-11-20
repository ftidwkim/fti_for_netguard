package com.franklinwireless.android.jexkids.locker.model;

public class SystemModel {
    public boolean blocked;
    public SystemModel(boolean blocked)
    {
        this.blocked = blocked;
    }
    public boolean getBlocked() {
        return blocked;
    }
    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }
}
