package com.franklinwireless.android.jexkids.locker.gesture.contract;

import android.content.Context;

import com.franklinwireless.android.jexkids.locker.base.BasePresenter;
import com.franklinwireless.android.jexkids.locker.base.BaseView;
import com.franklinwireless.android.jexkids.locker.model.CommLockInfo;
import com.franklinwireless.android.jexkids.locker.gesture.presenter.LockMainPresenter;

import java.util.List;


public interface LockMainContract {
    interface View extends BaseView<Presenter> {

        void loadAppInfoSuccess(List<CommLockInfo> list);
    }

    interface Presenter extends BasePresenter {
        void loadAppInfo(Context context);

        void searchAppInfo(String search, LockMainPresenter.ISearchResultListener listener);

        void onDestroy();
    }
}
