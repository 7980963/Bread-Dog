package com.emoji.bread_dog;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.gyf.immersionbar.ImmersionBar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    LinearLayout linearLayout;
    RecyclerView recyclerView;
    List<File> list = new ArrayList<>();
    File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        file = new File(getCacheDir(), "img");
        ImmersionBar.with(this)
                .transparentStatusBar()
                .statusBarColor(R.color.colorPrimary)
                .navigationBarColor(R.color.white)
                .init();

        linearLayout = findViewById(R.id.load_view);
        recyclerView = findViewById(R.id.recycler_view);

        Toolbar toolbar = findViewById(R.id.toolbar);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navigationView.setCheckedItem(R.id.single_1);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, 0, 0);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //添加头布局和尾布局
        View headerView = navigationView.getHeaderView(0);
        ImageView imageView = headerView.findViewById(R.id.iv_head);
        imageView.setImageResource(R.drawable.image1);
        TextView textView = headerView.findViewById(R.id.iv_text);
        imageView.setOnClickListener(v -> Toast.makeText(getApplicationContext(), "点击了头像", Toast.LENGTH_LONG).show());
        navigationView.setNavigationItemSelectedListener(menuItem -> false);
        ColorStateList csl = ContextCompat.getColorStateList(this, R.color.nav_menu_text_color);
        navigationView.setItemTextColor(csl);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            drawer.closeDrawers();
            if (menuItem.getItemId() == R.id.single_1) {
                // 运行与按钮1相关的代码
                showImage("1_小二柴.zip");
                toolbar.setTitle("小二柴");
            } else if (menuItem.getItemId() == R.id.single_2) {
                // 运行与按钮2相关的代码
                showImage("2_小肥柴.zip");
                toolbar.setTitle("小肥柴");
            } else if (menuItem.getItemId() == R.id.single_3) {
                // 运行与按钮3相关的代码
                showImage("3_恶魔猫.zip");
                toolbar.setTitle("恶魔猫");
            } else if (menuItem.getItemId() == R.id.single_4) {
                // 运行与按钮1相关的代码
                showImage("4_真的是小恐龙吗.zip");
                toolbar.setTitle("真的是小恐龙吗");
            } else if (menuItem.getItemId() == R.id.single_5) {
                // 运行与按钮2相关的代码
                showImage("5_新鲜动物园.zip");
                toolbar.setTitle("新鲜动物园");
            }
            return true;
        });
        showImage("1_小二柴.zip");
    }

    private void showImage(String zipFileName) {
        if (file.exists()) {
            deleteDirectory(file);
        }

        new Thread(() -> {
            try {
                Utils.unZip(MainActivity.this, zipFileName, file.getPath(), true);
                runOnUiThread(this::initList);
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void deleteDirectory(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    deleteDirectory(child);
                }
            }
        }
        boolean isDeleted = file.delete();
        if (!isDeleted) {
            // 文件删除失败，进行相应的处理逻辑
            Toast.makeText(MainActivity.this, "文件删除失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void initList() {
        linearLayout.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        list.clear();

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 5);
        recyclerView.setLayoutManager(gridLayoutManager);
        Adapter adapter = new Adapter(list);
        recyclerView.setAdapter(adapter);

        new Thread(() -> {
            try {
                list = Utils.getList(MainActivity.this);
                runOnUiThread(() -> {
                    adapter.updateList(list);
                    linearLayout.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        Utils.handleActivityResult(this, requestCode, resultCode, resultData, list);
    }
}
