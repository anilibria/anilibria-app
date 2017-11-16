package ru.radiationx.anilibria;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.List;

import ru.radiationx.anilibria.ui.release.ReleaseFragment;
import ru.radiationx.anilibria.ui.releases.ReleasesFragment;
import ru.terrakok.cicerone.Navigator;
import ru.terrakok.cicerone.android.SupportFragmentNavigator;
import ru.terrakok.cicerone.commands.Back;
import ru.terrakok.cicerone.commands.BackTo;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationViewEx bottomTabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomTabs = findViewById(R.id.bottom_tabs);

        bottomTabs.getMenu()
                .add("Releases")
                .setIcon(R.drawable.ic_releases)
                .setOnMenuItemClickListener(item -> {
                    navigator.applyCommand(new BackTo("ReleasesFragment"));
                    return false;
                });
        bottomTabs.getMenu()
                .add("News")
                .setIcon(R.drawable.ic_news)
                .setOnMenuItemClickListener(item -> {
                    navigator.applyCommand(new BackTo("ReleasesFragment"));
                    return false;
                });

        bottomTabs.getMenu()
                .add("Videos")
                .setIcon(R.drawable.ic_videos)
                .setOnMenuItemClickListener(item -> {
                    navigator.applyCommand(new BackTo("ReleasesFragment"));
                    return false;
                });

        bottomTabs.getMenu()
                .add("Blogs")
                .setIcon(R.drawable.ic_blogs)
                .setOnMenuItemClickListener(item -> {
                    navigator.applyCommand(new BackTo("ReleasesFragment"));
                    return false;
                });

        bottomTabs.getMenu()
                .add("Other")
                .setIcon(R.drawable.ic_other)
                .setOnMenuItemClickListener(item -> {
                    navigator.applyCommand(new BackTo("ReleasesFragment"));
                    return false;
                });
        bottomTabs.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(item.getTitle());
                }
                return true;
            }
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
            App.get().getRouter().replaceScreen("ReleasesFragment");

        }
        /*if (fragments.size() == 0) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragments_container, new ReleasesFragment())
                    .commit();
        }*/


    }

    private Navigator navigator =
            new SupportFragmentNavigator(getSupportFragmentManager(), R.id.fragments_container) {
                @Override
                protected Fragment createFragment(String screenKey, Object data) {
                    switch (screenKey) {
                        case "ReleaseFragment": {
                            ReleaseFragment f = new ReleaseFragment();
                            if (data instanceof Bundle) {
                                f.setArguments((Bundle) data);
                            }
                            return f;
                        }
                        case "ReleasesFragment": {
                            ReleasesFragment fragment = new ReleasesFragment();
                            return fragment;
                        }
                        default:
                            throw new RuntimeException("Unknown screen key!");
                    }
                }

                @Override
                protected void showSystemMessage(String message) {
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                }

                @Override
                protected void backToUnexisting() {
                    super.backToUnexisting();
                    Log.e("SUKA", "backToUnexisting");
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
