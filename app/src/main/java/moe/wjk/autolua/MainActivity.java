package moe.wjk.autolua;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_doc, R.id.navigation_settings)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

//        Button b0 = findViewById(R.id.button0);
//        b0.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                try {
//                    Intent intent = new Intent(Intent.ACTION_MAIN); // 这里决定了和点击微信的图标相同的效果
//                    ComponentName cmp = new ComponentName("com.tencent.mm","com.tencent.mm.ui.LauncherUI");
//                    intent.addCategory(Intent.CATEGORY_LAUNCHER); // appear in the Launcher as a top-level application
////                    intent.addCategory(Intent.CATEGORY_HOME);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    intent.setComponent(cmp);
//                    startActivity(intent);
//                } catch (ActivityNotFoundException e) {
//                    Toast.makeText(getBaseContext(), "No wechat installed",
//                            Toast.LENGTH_LONG).show();
//                }
//            }
//        });
//        Button b1 = findViewById(R.id.button1);
//        b1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(getBaseContext(), "开始",
//                        Toast.LENGTH_SHORT).show();
//                Intent reportIntent = new Intent(MainActivity.this, MyAccessibilityService.class);
//                reportIntent.setAction(MyAccessibilityService.ACTION_START_REPORT);
//                startService(reportIntent);
//
//            }
//        });
    }

//    //Both navigation bar back press and title bar back press will trigger this method
//    @Override
//    public void onBackPressed() {
//        Log.d(TAG, "onBackPressed: !!");
//        if (getFragmentManager().getBackStackEntryCount() > 0 ) {
//            getFragmentManager().popBackStack();
//        }
//        else {
//            super.onBackPressed();
//        }
//    }

    /**
     * 处理{@link moe.wjk.autolua.ui.doc.DocWebViewFragment}按返回键
      */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        Log.d(TAG, "onOptionsItemSelected: item id: " + String.valueOf(item.getItemId()));
        // home as up
        if (item.getItemId() == android.R.id.home) {
            Navigation.findNavController(this, R.id.nav_host_fragment).navigateUp();
        }
        return super.onOptionsItemSelected(item);
    }

}