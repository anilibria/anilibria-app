package ru.radiationx.anilibria;

import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.List;

import ru.radiationx.anilibria.ui.fragments.release.ReleaseFragment;
import ru.radiationx.anilibria.ui.fragments.releases.ReleasesFragment;
import ru.terrakok.cicerone.Navigator;
import ru.terrakok.cicerone.android.SupportFragmentNavigator;
import ru.terrakok.cicerone.commands.BackTo;
import ru.terrakok.cicerone.commands.Command;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationViewEx bottomTabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomTabs = findViewById(R.id.bottom_tabs);

        bottomTabs.setOnNavigationItemSelectedListener(item -> {
            setTitle(item.getTitle());
            return true;
        });


        addMenuToBottom(R.string.fragment_title_releases, R.drawable.ic_releases)
                .setOnMenuItemClickListener(item -> {
                    navigator.applyCommand(new BackTo(Screens.RELEASES_LIST));
                    return false;
                });
        addMenuToBottom(R.string.fragment_title_news, R.drawable.ic_news)
                .setOnMenuItemClickListener(item -> {
                    navigator.applyCommand(new BackTo(Screens.RELEASES_LIST));
                    return false;
                });
        addMenuToBottom(R.string.fragment_title_videos, R.drawable.ic_videos)
                .setOnMenuItemClickListener(item -> {
                    navigator.applyCommand(new BackTo(Screens.RELEASES_LIST));
                    return false;
                });
        addMenuToBottom(R.string.fragment_title_blogs, R.drawable.ic_blogs)
                .setOnMenuItemClickListener(item -> {
                    navigator.applyCommand(new BackTo(Screens.RELEASES_LIST));
                    return false;
                });
        addMenuToBottom(R.string.fragment_title_other, R.drawable.ic_other)
                .setOnMenuItemClickListener(item -> {
                    navigator.applyCommand(new BackTo(Screens.RELEASES_LIST));
                    return false;
                });

        bottomTabs.enableItemShiftingMode(false);
        bottomTabs.enableShiftingMode(false);
        bottomTabs.setTextVisibility(false);
        bottomTabs.enableAnimation(false);

        Log.e("SUKA", "" + getSupportFragmentManager().getFragments().size());
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            Log.e("SUKA", "Old fragments: " + fragment);
        }
        if (savedInstanceState == null) {
            App.get().getRouter().newRootScreen(Screens.RELEASES_LIST);
        }
    }

    private MenuItem addMenuToBottom(String title, @DrawableRes int iconId) {
        return bottomTabs.getMenu().add(title).setIcon(iconId);
    }

    private MenuItem addMenuToBottom(@StringRes int titleId, @DrawableRes int iconId) {
        return bottomTabs.getMenu().add(titleId).setIcon(iconId);
    }

    private void setTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    private MenuItem getCurrentTab() {
        return bottomTabs.getMenu().getItem(bottomTabs.getCurrentItem());
    }

    private Navigator navigator =
            new SupportFragmentNavigator(getSupportFragmentManager(), R.id.fragments_container) {

                @Override
                public void applyCommand(Command command) {
                    super.applyCommand(command);
                    MenuItem item = getCurrentTab();
                    setTitle(item.getTitle());
                }

                @Override
                protected Fragment createFragment(String screenKey, Object data) {
                    switch (screenKey) {
                        case Screens.RELEASE_DETAILS: {
                            ReleaseFragment f = new ReleaseFragment();
                            if (data instanceof Bundle) {
                                f.setArguments((Bundle) data);
                            }
                            return f;
                        }
                        case Screens.RELEASES_LIST: {
                            ReleasesFragment fragment = new ReleasesFragment();
                            return fragment;
                        }
                        default:
                            throw new RuntimeException("Unknown screen key: " + screenKey);
                    }
                }

                @Override
                protected void showSystemMessage(String message) {
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                }

                @Override
                protected void exit() {
                    finish();
                }
            };


    @Override
    protected void onResume() {
        super.onResume();
        App.get().getNavigatorHolder().setNavigator(navigator);
    }

    @Override
    protected void onPause() {
        super.onPause();
        App.get().getNavigatorHolder().removeNavigator();
    }
}
