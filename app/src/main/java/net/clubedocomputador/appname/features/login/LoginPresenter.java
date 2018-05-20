package net.clubedocomputador.appname.features.login;


import javax.inject.Inject;

import net.clubedocomputador.appname.data.LoginService;
import net.clubedocomputador.appname.data.model.Login;
import net.clubedocomputador.appname.features.base.BasePresenter;
import net.clubedocomputador.appname.injection.ConfigPersistent;
import net.clubedocomputador.appname.util.rx.scheduler.SchedulerUtils;

@ConfigPersistent
public class LoginPresenter extends BasePresenter<LoginMvpView> {

    private final LoginService service;

    @Inject
    public LoginPresenter(LoginService service) {
        this.service = service;
    }

    @Override
    public void attachView(LoginMvpView mvpView) {
        super.attachView(mvpView);
    }

    public void login(Login login) {
        checkViewAttached();
        getView().showLoading();
        service
                .login(login)
                .compose(SchedulerUtils.ioToMain())
                .subscribe(
                        loggedIn -> {
                            getView().hideLoading();
                            getView().loginSuccess(loggedIn);
                        },
                        throwable -> {
                            getView().hideLoading();
                            getView().showError(throwable);
                        });
    }
}
