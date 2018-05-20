package net.clubedocomputador.appname.features.faces;



import javax.inject.Inject;

import net.clubedocomputador.appname.data.LoginService;
import net.clubedocomputador.appname.data.model.Login;
import net.clubedocomputador.appname.features.base.BasePresenter;
import net.clubedocomputador.appname.injection.ConfigPersistent;
import net.clubedocomputador.appname.util.rx.scheduler.SchedulerUtils;

@ConfigPersistent
public class FacePresenter extends BasePresenter<FaceMvpView> {

    private final LoginService service;

    @Inject
    public FacePresenter(LoginService service) {
        this.service = service;
    }

    @Override
    public void attachView(FaceMvpView mvpView) {
        super.attachView(mvpView);
    }


    public void recognise(Login login) {
        checkViewAttached();
        getView().showLoading();
        //TODO
        service
                .login(login)
                .compose(SchedulerUtils.ioToMain())
                .subscribe(
                        loggedIn -> {
                            getView().recognized(loggedIn);
                        },
                        throwable -> {
                            getView().showError(throwable);
                        });
    }
}
