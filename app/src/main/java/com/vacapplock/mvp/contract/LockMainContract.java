package com.vacapplock.mvp.contract;

import android.content.Context;

import com.vacapplock.base.BasePresenter;
import com.vacapplock.base.BaseView;
import com.vacapplock.model.CommLockInfo;
import com.vacapplock.mvp.p.LockMainPresenter;

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
