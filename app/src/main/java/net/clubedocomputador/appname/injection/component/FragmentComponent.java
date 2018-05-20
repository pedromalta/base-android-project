package net.clubedocomputador.appname.injection.component;

import dagger.Subcomponent;
import net.clubedocomputador.appname.injection.PerFragment;
import net.clubedocomputador.appname.injection.module.FragmentModule;

/**
 * This component inject dependencies to all Fragments across the application
 */
@PerFragment
@Subcomponent(modules = FragmentModule.class)
public interface FragmentComponent {

    //void inject(SomeFragment someFragment);

}
