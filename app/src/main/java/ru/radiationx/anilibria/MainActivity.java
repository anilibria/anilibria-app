package ru.radiationx.anilibria;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import ru.radiationx.anilibria.ui.releases.ReleasesFragment;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragments_container, new ReleasesFragment())
                .commit();
    }
}
