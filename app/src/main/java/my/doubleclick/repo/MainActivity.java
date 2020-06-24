package my.doubleclick.repo;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import my.doubleclick.repo.ui.home.HomeFragment;

public class MainActivity extends AppCompatActivity {
    final String[] permissions = new String[]{
        Manifest.permission.INTERNET,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private AppBarConfiguration mAppBarConfiguration;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.report:
                checkPermissions();
                try {
                    SharedPreferences prefs = PreferenceManager
                        .getDefaultSharedPreferences(this);
                    JSONArray
                        array =
                        new JSONArray(prefs.getString(HomeFragment.TOPIC,
                                                      "[]"));
                    generateTextFile(HomeFragment.TOPIC + ".txt",
                                     array.toString());
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController nav = Navigation.findNavController(
            this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(nav, mAppBarConfiguration)
               || super.onSupportNavigateUp();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_home)
            .setDrawerLayout(drawer)
            .build();
        NavController nav =
            Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this,
                                                     nav,
                                                     mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, nav);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 100) {
            if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "permission granted", Toast.LENGTH_SHORT)
                     .show();
            }
        }
    }

    /**
     * Checks runtime permission.
     */
    private void checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                listPermissionsNeeded.toArray(new String[0]),
                100);
        }
    }

    /**
     * Generates text file. PDF would be the best.
     *
     * @param filename the file name.
     * @param content  the content inside file.
     */
    public void generateTextFile(String filename, String content) {
        try {
            File
                root =
                new File(getApplicationContext().getFilesDir().getPath(),
                         "REPO");
            if (!root.exists()) {
                root.mkdirs();
            }
            File file = new File(root, filename);
            FileWriter writer = new FileWriter(file);
            writer.append(content);
            writer.flush();
            writer.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
