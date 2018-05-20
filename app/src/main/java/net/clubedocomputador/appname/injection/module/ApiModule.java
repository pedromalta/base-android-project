package net.clubedocomputador.appname.injection.module;

import javax.inject.Singleton;

import net.clubedocomputador.appname.data.remote.LoginApi;
import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;

@Module(includes = {NetworkModule.class})
public class ApiModule {

    @Provides
    @Singleton
    LoginApi provideLoginApi(Retrofit retrofit) {
        return retrofit.create(LoginApi.class);
    }

}
