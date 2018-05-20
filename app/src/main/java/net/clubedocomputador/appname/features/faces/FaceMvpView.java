package net.clubedocomputador.appname.features.faces;

import net.clubedocomputador.appname.data.model.LoggedIn;
import net.clubedocomputador.appname.data.model.Login;
import net.clubedocomputador.appname.features.base.MvpView;

/**
 * Created by pedromalta on 14/03/18.
 */

public interface FaceMvpView extends MvpView {

    void showLoading();

    void hideLoading();

    void recognized(LoggedIn loggedIn);

    void showError(Throwable error);
}
