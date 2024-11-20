package com.franklinwireless.android.jexkids.locker.adapters;

import android.annotation.SuppressLint;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.franklinwireless.android.jexkids.R;
import com.franklinwireless.android.jexkids.locker.model.CommLockInfo;
import com.franklinwireless.android.jexkids.locker.db.CommLockInfoManager;
import com.franklinwireless.android.jexkids.locker.model.SystemModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainViewHolder> {

    @NonNull
    private List<CommLockInfo> mLockInfos = new ArrayList<>();
    private List<SystemModel> msysInfos = new ArrayList<>();

    private PackageManager packageManager;
    private CommLockInfoManager mLockInfoManager;
    Context mContext;
    public MainAdapter(Context mContext) {
         this.mContext = mContext;
        packageManager = mContext.getPackageManager();
        mLockInfoManager = new CommLockInfoManager(mContext);
    }

    public void setLockInfos(@NonNull List<CommLockInfo> lockInfos, List<SystemModel> sysInfos) {
        mLockInfos.clear();
        msysInfos.clear();
        mLockInfos.addAll(lockInfos);
        msysInfos.addAll(sysInfos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main_list, parent, false);
        return new MainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MainViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        final CommLockInfo lockInfo = mLockInfos.get(position);
        initData(holder.mAppName,  holder.timespent,holder.mSwitchCompat, holder.mAppIcon, lockInfo,position);
        holder.mSwitchCompat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeItemLockStatus(holder.mSwitchCompat, lockInfo, position);
            }
        });
    }


    private void initData(TextView tvAppName, TextView tvtimespent,CheckBox switchCompat, ImageView mAppIcon, CommLockInfo lockInfo,int index) {
        tvAppName.setText(packageManager.getApplicationLabel(lockInfo.getAppInfo()));
        //switchCompat.setChecked(lockInfo.isLocked());
        switchCompat.setChecked(msysInfos.get(index).getBlocked());
        lockInfo.setLocked(msysInfos.get(index).getBlocked());
        if(msysInfos.get(index).getBlocked())
        {
            mLockInfoManager.lockCommApplication(lockInfo.getPackageName());
        }
        else
        {
            mLockInfoManager.unlockCommApplication(lockInfo.getPackageName());
        }
        ApplicationInfo appInfo = lockInfo.getAppInfo();
        mAppIcon.setImageDrawable(packageManager.getApplicationIcon(appInfo));

        String PackageName = "Nothing" ;

        long TimeInforground = 500 ;

        int minutes=0,seconds=0,hours=0 ;
        UsageStatsManager mUsageStatsManager = (UsageStatsManager)mContext.getSystemService(mContext.USAGE_STATS_SERVICE);

        long time = System.currentTimeMillis();

        Date installDate = null;
        String installDateString ="";

        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(lockInfo.getPackageName(), 0);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            installDateString = dateFormat.format( new Date( packageInfo.firstInstallTime ) );
        }
        catch (PackageManager.NameNotFoundException e) {
            // an error occurred, so display the Unix epoch
            installDate = new Date(0);
            installDateString = installDate.toString();
        }
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND, 0);
        Date todayDate = cal.getTime();
        List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, todayDate.getTime(), time);
        int i=0,j=0;
        if(stats != null) {
            SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();
            for (UsageStats usageStats : stats) {

                TimeInforground = usageStats.getTotalTimeInForeground();

                PackageName = usageStats.getPackageName();

                minutes = (int) ((TimeInforground / (1000 * 60)) % 60);

                seconds = (int) (TimeInforground / 1000) % 60;

                hours = (int) ((TimeInforground / (1000 * 60 * 60)) % 24);

                if(lockInfo.getPackageName().equals(PackageName)) {
                    j = 1;

                    tvAppName.setText(packageManager.getApplicationLabel(lockInfo.getAppInfo()));
                    //tvAppName.setText(installDateString);
                    if(hours<10 && minutes<10 &&seconds<10) {
                        tvtimespent.setText("Time Spent : 0" + hours + ":0" + minutes + ":0" + seconds);
                    }
                    else if(hours<10 && minutes<10) {
                        tvtimespent.setText("Time Spent : 0" + hours + ":0" + minutes + ":" + seconds);
                    }
                    else if(hours<10 && seconds<10) {
                        tvtimespent.setText("Time Spent : 0" + hours + ":" + minutes + ":0" + seconds);
                    }
                    else if(minutes<10 && seconds<10) {
                        tvtimespent.setText("Time Spent : " + hours + ":0" + minutes + ":0" + seconds);
                    }
                    else
                    {
                        tvtimespent.setText("Time Spent : " + hours + ":" + minutes + ":" + seconds);
                    }
                }
                i=i+1;
                if(i==stats.size() && j==0)
                {
                    tvAppName.setText(packageManager.getApplicationLabel(lockInfo.getAppInfo()));
                    //tvAppName.setText(installDateString);
                    tvtimespent.setText("Time Spent : "+hours+":"+minutes+":"+seconds);
                }
                //Log.i("BAC", "PackageName is" + PackageName + "Time is: " + hours + "h" + ":" + minutes + "m" + seconds + "s");

            }
        }
    }

    public void changeItemLockStatus(@NonNull CheckBox checkBox, @NonNull CommLockInfo info, int position) {
        if (checkBox.isChecked()) {
            //info.setLocked(true);
            //mLockInfoManager.lockCommApplication(info.getPackageName());
        } else {
            //info.setLocked(false);
            //mLockInfoManager.unlockCommApplication(info.getPackageName());
        }
        notifyItemChanged(position);
    }

    @Override
    public int getItemCount() {
        return mLockInfos.size();
    }

    public class MainViewHolder extends RecyclerView.ViewHolder {
        private ImageView mAppIcon;
        private TextView mAppName,timespent;
        private CheckBox mSwitchCompat;

        public MainViewHolder(@NonNull View itemView) {
            super(itemView);
            mAppIcon = itemView.findViewById(R.id.app_icon);
            mAppName = itemView.findViewById(R.id.app_name);
            timespent = itemView.findViewById(R.id.time_spent);
            mSwitchCompat = itemView.findViewById(R.id.switch_compat);
        }
    }
}
