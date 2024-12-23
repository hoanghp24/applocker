package com.vacapplock.mvp.contract;

import android.content.Context;

import com.vacapplock.base.BasePresenter;
import com.vacapplock.base.BaseView;
import com.vacapplock.model.CommLockInfo;

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
