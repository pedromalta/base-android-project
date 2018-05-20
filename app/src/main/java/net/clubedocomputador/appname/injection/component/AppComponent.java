package net.clubedocomputador.appname.injection.component;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import net.clubedocomputador.appname.data.LoginService;
import net.clubedocomputador.appname.data.local.InstanceHolder;
import net.clubedocomputador.appname.data.local.PreferencesHelper;
import net.clubedocomputador.appname.util.location.LocationTracker;
import dagger.Component;
import net.clubedocomputador.appname.injection.ApplicationContext;
import net.clubedocomputador.appname.injection.module.AppModule;

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {

    @ApplicationContext
    Context context();

    Application application();

    LocationTracker locationTracker();

    LoginService loginService();

    PreferencesHelper preferencesHelper();

    InstanceHolder instanceHolder();
}
