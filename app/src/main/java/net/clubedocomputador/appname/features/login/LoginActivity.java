package net.clubedocomputador.appname.features.login;

import android.content.Context;
import android.content.Intent;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import net.clubedocomputador.appname.R;
import net.clubedocomputador.appname.data.local.PreferencesHelper;
import net.clubedocomputador.appname.data.model.LoggedIn;
import net.clubedocomputador.appname.data.model.Login;
import net.clubedocomputador.appname.databinding.ActivityLoginBinding;
import net.clubedocomputador.appname.features.base.BaseActivity;
import net.clubedocomputador.appname.injection.component.ActivityComponent;
import net.clubedocomputador.appname.util.AppLogger;
import net.clubedocomputador.appname.util.Util;
import butterknife.BindView;

import static net.clubedocomputador.appname.Constants.Preferences.LOGIN_ID;
import static net.clubedocomputador.appname.Constants.Preferences.AUTOCOMPLETE_LOGIN_ID;


public class LoginActivity extends BaseActivity implements LoginMvpView {


    @BindView(R.id.input_username)
    protected AutoCompleteTextView username;

    @BindView(R.id.input_password)
    protected EditText password;

    @BindView(R.id.btn_login)
    protected Button buttonLogin;

    private ActivityLoginBinding binding;

    @Inject
    LoginPresenter presenter;

    @Inject
    PreferencesHelper preferences;

    private Login login = new Login();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        buttonLogin.setOnClickListener(view -> attemptLogin());

        binding.setLogin(login);
        setAutoCompleteUsername();

    }

    @Override
    protected void onResume() {
        super.onResume();
        Util.hideKeyboard(this);
    }

    private void setAutoCompleteUsername() {
        Set<String> autoCompleteUsernameSet = Collections.emptySet();
        autoCompleteUsernameSet = preferences.get(AUTOCOMPLETE_LOGIN_ID, autoCompleteUsernameSet);
        String[] autoCompleteUsernameArray = autoCompleteUsernameSet.toArray(new String[0]);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this, android.R.layout.select_dialog_item, autoCompleteUsernameArray);
        //Getting the instance of AutoCompleteTextView
        username.setThreshold(1);//will start working from first character
        username.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView
    }

    //TODO implement maximum Usernames FIFO structure (Check for Hawk lib)
    private void addUsernameToAutoComplete() {
        Set<String> autoCompleteUsernameSet = new HashSet<>();
        autoCompleteUsernameSet = preferences.get(AUTOCOMPLETE_LOGIN_ID, autoCompleteUsernameSet);
        autoCompleteUsernameSet.add(login.getUsername());
        preferences.put(AUTOCOMPLETE_LOGIN_ID, autoCompleteUsernameSet);
    }

    @Override
    protected void setDataBinding(ViewDataBinding viewDataBinding) {
        this.binding = (ActivityLoginBinding) viewDataBinding;
    }

    @Override
    protected boolean isDataBinding() {
        return true;
    }

    @Override
    public int getLayout() {
        return R.layout.activity_login;
    }


    private void attemptLogin() {
        // Reset errors.
        username.setError(null);
        password.setError(null);

        addUsernameToAutoComplete();

        presenter.login(login);

    }

    @Override
    public void passwordEmpty() {
        password.setError(getString(R.string.error_field_required));
        password.requestFocus();
    }

    @Override
    public void passwordNotValid() {
        username.setError(getString(R.string.error_invalid_password));
        username.requestFocus();
    }

    @Override
    public void usernameEmpty() {
        username.setError(getString(R.string.error_field_required));
        username.requestFocus();
    }

    @Override
    public void usernameNotValid() {
        username.setError(getString(R.string.error_invalid_email));
        username.requestFocus();
    }

    @Override
    public void showError(Throwable error) {
        AppLogger.i("Login Error %s", error.toString());
        Util.DialogFactory.createGenericErrorDialog(this, error).show();
    }

    @Override
    public void loginSuccess(LoggedIn loggedIn) {
        preferences.put(LOGIN_ID, loggedIn.getUid());
        AppLogger.i("Login Success");
        goClassActivity();
    }

    private void goClassActivity() {
        //startActivity(NextActivity.getStartIntent(this));
        //this.finish();
    }

    @Override
    protected void inject(ActivityComponent activityComponent) {
        activityComponent.inject(this);
    }

    @Override
    protected void attachView() {
        presenter.attachView(this);
    }

    @Override
    protected void detachPresenter() {
        presenter.detachView();
    }

    public static Intent getStartIntent(Context context) {
        return new Intent(context, LoginActivity.class);
    }


}

