package com.franklinwireless.android.jexkids.locker.gesture.contract;

import android.content.Context;

import com.franklinwireless.android.jexkids.locker.base.BasePresenter;
import com.franklinwireless.android.jexkids.locker.base.BaseView;
import com.franklinwireless.android.jexkids.locker.model.CommLockInfo;

import java.util.List;


public interface MainContract {
    interface View extends BaseView<Presenter> {
        void loadAppInfoSuccess(List<CommLockInfo> list);
    }

    interface Presenter extends BasePresenter {
        void loadAppInfo(Context context, boolean isSort);

        void loadLockAppInfo(Context context);

        void onDestroy();
    }
}
