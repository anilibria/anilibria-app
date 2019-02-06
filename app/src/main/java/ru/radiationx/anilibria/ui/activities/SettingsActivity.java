package ru.radiationx.anilibria.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import org.jetbrains.annotations.NotNull;

import io.reactivex.disposables.CompositeDisposable;
import ru.radiationx.anilibria.App;
import ru.radiationx.anilibria.R;
import ru.radiationx.anilibria.extension.ContextKt;
import ru.radiationx.anilibria.model.data.holders.AppThemeHolder;
import ru.radiationx.anilibria.ui.fragments.settings.SettingsFragment;


/**
 * Created by radiationx on 25.12.16.
 */

public class SettingsActivity extends BaseActivity {

    private AppThemeHolder appThemeHolder = App.injections.getAppThemeHolder();
    private CompositeDisposable disposables = new CompositeDisposable();
    private AppThemeHolder.AppTheme currentAppTheme = appThemeHolder.getTheme();

    @NotNull
    public static Intent getIntent(@NotNull Context context) {
        return new Intent(context, SettingsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentAppTheme = appThemeHolder.getTheme();
        setTheme(ContextKt.getPrefStyleRes(currentAppTheme));
        setContentView(R.layout.activity_settings);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle("Настройки");
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_content, new SettingsFragment()).commit();

        disposables.add(
                appThemeHolder
                        .observeTheme()
                        .subscribe(appTheme -> {
                            if (currentAppTheme != appTheme) {
                                currentAppTheme = appTheme;
                                recreate();
                            }
                        })
        );
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }
}
