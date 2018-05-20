package net.clubedocomputador.appname.injection.component;

import net.clubedocomputador.appname.features.map.MapActivity;
import net.clubedocomputador.appname.features.faces.FaceActivity;
import net.clubedocomputador.appname.features.login.LoginActivity;
import net.clubedocomputador.appname.features.splash.SplashActivity;
import dagger.Subcomponent;
import net.clubedocomputador.appname.injection.PerActivity;
import net.clubedocomputador.appname.injection.module.ActivityModule;

@PerActivity
@Subcomponent(modules = ActivityModule.class)
public interface ActivityComponent {

    void inject(SplashActivity splashActivity);

    void inject(LoginActivity loginActivity);

    void inject(MapActivity mapActivity);

    void inject(FaceActivity faceActivity);

}