package com.pangjie.eyeprotect.impl;

import com.pangjie.eyeprotect.base.IView;
import com.pangjie.eyeprotect.presenter.IMainPresenter;

public class MainPresenterImpl implements IMainPresenter {
    @Override
    public boolean checkUpdate() {
        return false;
    }

    @Override
    public void attachView(IView iView) {

    }

    @Override
    public void detachView() {

    }
}
