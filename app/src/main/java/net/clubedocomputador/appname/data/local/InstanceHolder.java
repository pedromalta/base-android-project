package net.clubedocomputador.appname.data.local;

import javax.inject.Inject;

import net.clubedocomputador.appname.injection.ApplicationContext;

//import static net.clubedocomputador.appname.Constants.Preferences.REPORT_INSTANCE;

@ApplicationContext
public class InstanceHolder {

    private PreferencesHelper preferences;

    @Inject
    public InstanceHolder(PreferencesHelper preferences){
        this.preferences = preferences;
    }

//    public synchronized Report getReport(){
//        return preferences.get(REPORT_INSTANCE);
//    }
//
//    public synchronized void setReport(Report report){
//        preferences.put(REPORT_INSTANCE, report);
//    }
//
//    public synchronized void releaseReport(){
//        preferences.delete(REPORT_INSTANCE);
//    }

}
