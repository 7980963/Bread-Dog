package com.emoji.bread_dog;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class MainActivity extends AppCompatActivity implements ButtonGenerator.ImageClickListener {

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
        Button Jump_Github = findViewById(R.id.footer_item_github);
        Button button_setting = findViewById(R.id.footer_item_setting);

        LinearLayout buttonContainer;
        ButtonGenerator buttonGenerator;

        Jump_Github.setOnClickListener(v -> {
            // 定义要跳转的URL
            String url = "https://github.com/7980963/Bread-Dog";

            // 创建Intent对象，并设置Action为ACTION_VIEW
            Intent intent = new Intent(Intent.ACTION_VIEW);

            // 将URL字符串解析为Uri对象，并设置给Intent
            Uri uri = Uri.parse(url);
            intent.setData(uri);

            // 启动Intent
            startActivity(intent);
        });

        button_setting.setOnClickListener(V -> Toast.makeText(getApplicationContext(), "前面的区域，以后再来探索吧！", Toast.LENGTH_SHORT).show());

        Toolbar toolbar = findViewById(R.id.toolbar);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        //navigationView.setCheckedItem(R.id.single_1);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, 0, 0);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //添加头布局和尾布局
        View headerView = navigationView.getHeaderView(0);
        ImageView imageView = headerView.findViewById(R.id.iv_head);
        imageView.setImageResource(R.mipmap.image01);
        imageView.setOnClickListener(v -> Toast.makeText(getApplicationContext(), "不吃你别扒拉(╯‵□′)╯︵┻━┻", Toast.LENGTH_LONG).show());
        navigationView.setNavigationItemSelectedListener(menuItem -> false);
        ColorStateList csl = ContextCompat.getColorStateList(this, R.color.nav_menu_text_color);
        navigationView.setItemTextColor(csl);
        navigationView.setItemIconTintList(null);
        showImage("01_小二柴.zip");

        buttonContainer = findViewById(R.id.buttonContainer);
        toolbar = findViewById(R.id.toolbar);

        buttonGenerator = new ButtonGenerator(this, buttonContainer, toolbar, this);
        buttonGenerator.setImageClickListener(this);
        buttonGenerator.generateButtons();
    }

    @Override
    public void onImageClick(String fileName) {
        // 处理按钮点击事件
        showImage(fileName);
    }

    private void showImage(String zipFileName) {
        // 在这里处理解压缩的逻辑
        if (file.exists()) {
            deleteDirectory(file);
        }

        new Thread(() -> {
            try {
                Utils.unZip(MainActivity.this, zipFileName, file.getPath(), true);
                // 这里添加你希望执行的代码
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
