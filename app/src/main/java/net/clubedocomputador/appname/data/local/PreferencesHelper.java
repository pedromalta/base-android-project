package net.clubedocomputador.appname.data.local;

import android.content.Context;

import com.orhanobut.hawk.Hawk;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import net.clubedocomputador.appname.injection.ApplicationContext;

@ApplicationContext
public class PreferencesHelper {

    @Inject
    public PreferencesHelper(@ApplicationContext Context context) {
        Hawk.init(context).build();
    }

    /**
     * Saves any type including any collection, primitive values or custom objects
     *
     * @param key   is required to differentiate the given data
     * @param value is the data that is going to be encrypted and persisted
     *
     * @return true if the operation is successful. Any failure in any step will return false
     */
    public <T> boolean put(@Nonnull String key, @Nonnull T value) {
        return Hawk.put(key, value);
    }

    /**
     * Gets the saved data, if it is null, default value will be returned
     *
     * @param key          is used to get the saved data
     * @param defaultValue will be return if the response is null
     *
     * @return the saved object
     */
    public <T> T get(@Nonnull String key,  @Nonnull T defaultValue) {
        return Hawk.get(key, defaultValue);
    }

    /**
     * Gets the original data along with original type by the given key.
     * This is not guaranteed operation since Hawk uses serialization. Any change in in the requested
     * data type might affect the result. It's guaranteed to return primitive types and String type
     *
     * @param key is used to get the persisted data
     *
     * @return the original object
     */
    public <T> T get(@Nonnull String key) {
        return Hawk.get(key);
    }

    /**
     * Removes the given key/value from the storage
     *
     * @param key is used for removing related data from storage
     *
     * @return true if delete is successful
     */
    public boolean delete(@Nonnull String key) {
        return Hawk.delete(key);
    }

    /**
     * Checks the given key whether it exists or not
     *
     * @param key is the key to check
     *
     * @return true if it exists in the storage
     */
    public boolean contains(@Nonnull String key) {
        return Hawk.contains(key);
    }

    /**
     * Clears the storage
     *
     * @return true if clear is successful
     */
    public boolean clear() {
        return Hawk.deleteAll();
    }
}
