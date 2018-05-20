package net.clubedocomputador.appname.features.base;

import android.app.ProgressDialog;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.util.LongSparseArray;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import java.util.concurrent.atomic.AtomicLong;

import net.clubedocomputador.appname.AppNameApplication;
import net.clubedocomputador.appname.R;
import net.clubedocomputador.appname.util.AppLogger;
import net.clubedocomputador.appname.util.Util;
import butterknife.ButterKnife;

import net.clubedocomputador.appname.injection.component.ActivityComponent;
import net.clubedocomputador.appname.injection.component.ConfigPersistentComponent;
import net.clubedocomputador.appname.injection.component.DaggerConfigPersistentComponent;
import net.clubedocomputador.appname.injection.module.ActivityModule;

/**
 * Abstract activity that every other Activity in this application must implement. It provides the
 * following functionality: - Handles creation of Dagger components and makes sure that instances of
 * ConfigPersistentComponent are kept across configuration changes. - Set up and handles a
 * GoogleApiClient instance that can be used to access the Google sign in api. - Handles signing out
 * when an authentication error event is received.
 */
public abstract class BaseActivity extends AppCompatActivity {

    private static final String KEY_ACTIVITY_ID = "KEY_ACTIVITY_ID";
    private static final AtomicLong NEXT_ID = new AtomicLong(0);
    private static final LongSparseArray<ConfigPersistentComponent> componentsArray =
            new LongSparseArray<>();

    private ProgressDialog mProgressDialog;

    private long activityId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getLayout() != 0) { //do we have a view?
            if (isDataBinding()) {
                setDataBinding(DataBindingUtil.setContentView(this, getLayout()));
            } else {
                setContentView(getLayout());
            }
            ButterKnife.bind(this);
        }

        // Create the ActivityComponent and reuses cached ConfigPersistentComponent if this is
        // being called after a configuration change.
        activityId =
                savedInstanceState != null
                        ? savedInstanceState.getLong(KEY_ACTIVITY_ID)
                        : NEXT_ID.getAndIncrement();
        ConfigPersistentComponent configPersistentComponent;
        if (componentsArray.get(activityId) == null) {
            AppLogger.i("Creating new ConfigPersistentComponent id=%d", activityId);
            configPersistentComponent =
                    DaggerConfigPersistentComponent.builder()
                            .appComponent(AppNameApplication.get(this).getComponent())
                            .build();
            componentsArray.put(activityId, configPersistentComponent);
        } else {
            AppLogger.i("Reusing ConfigPersistentComponent id=%d", activityId);
            configPersistentComponent = componentsArray.get(activityId);
        }
        ActivityComponent activityComponent =
                configPersistentComponent.activityComponent(new ActivityModule(this));
        inject(activityComponent);
        attachView();
    }

    protected boolean isDataBinding() {
        return false;
    }

    protected void setDataBinding(ViewDataBinding viewDataBinding) {
    }

    protected abstract int getLayout();

    protected abstract void inject(ActivityComponent activityComponent);

    protected abstract void attachView();

    protected abstract void detachPresenter();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(KEY_ACTIVITY_ID, activityId);
    }

    @Override
    protected void onDestroy() {
        if (!isChangingConfigurations()) {
            AppLogger.i("Clearing ConfigPersistentComponent id=%d", activityId);
            componentsArray.remove(activityId);
        }
        detachPresenter();
        super.onDestroy();
    }

    public void showLoading() {
        hideLoading();
        mProgressDialog = Util.DialogFactory.createProgressDialog(this, R.string.dialog_loading);
        mProgressDialog.show();
    }

    public void showLoading(String message) {
        hideLoading();
        mProgressDialog = Util.DialogFactory.createProgressDialog(this, message);
        mProgressDialog.show();
    }

    public void showLoading(@StringRes int stringRes) {
        hideLoading();
        mProgressDialog = Util.DialogFactory.createProgressDialog(this, stringRes);
        mProgressDialog.show();
    }

    public void hideLoading() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.cancel();
        }
    }

    public void showError(Throwable error) {
        Util.DialogFactory.createGenericErrorDialog(this, error).show();
        AppLogger.e(error.toString());
    }

}