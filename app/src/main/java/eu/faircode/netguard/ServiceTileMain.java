package eu.faircode.netguard;

/*
    This file is part of NetGuard.

    NetGuard is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    NetGuard is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with NetGuard.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2015-2019 by Marcel Bokhorst (M66B)
*/


import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.franklinwireless.android.jexkids.R;

import java.util.Date;

@TargetApi(Build.VERSION_CODES.N)
public class ServiceTileMain extends TileService implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "NetGuard.TileMain";

    public void onStartListening() {
        Log.i(TAG, "Start listening");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
        update();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if ("enabled".equals(key))
            update();
    }

    private void update() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean enabled = prefs.getBoolean("enabled", false);
        Tile tile = getQsTile();
        if (tile != null) {
            tile.setState(enabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
            tile.setIcon(Icon.createWithResource(this, enabled ? R.drawable.ic_security_white_24dp : R.drawable.ic_security_white_24dp_60));
            tile.updateTile();
        }
    }

    public void onStopListening() {
        Log.i(TAG, "Stop listening");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onClick() {
        Log.i(TAG, "Click");

        // Cancel set alarm
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(WidgetAdmin.INTENT_ON);
        intent.setPackage(getPackageName());
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        am.cancel(pi);

        // Check state
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean enabled = !prefs.getBoolean("enabled", false);
        prefs.edit().putBoolean("enabled", enabled).apply();
        if (enabled)
            ServiceSinkhole.start("tile", this);
        else {
            ServiceSinkhole.stop("tile", this, false);

            // Auto enable
            int auto = Integer.parseInt(prefs.getString("auto_enable", "0"));
            if (auto > 0) {
                Log.i(TAG, "Scheduling enabled after minutes=" + auto);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                    am.set(AlarmManager.RTC_WAKEUP, new Date().getTime() + auto * 60 * 1000L, pi);
                else
                    am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, new Date().getTime() + auto * 60 * 1000L, pi);
            }
        }
    }
}
