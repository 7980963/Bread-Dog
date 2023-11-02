package cc.doger.bread_dog;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.Uri;
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
import com.hjq.window.EasyWindow;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ButtonGenerator.ImageClickListener {
    //声明侧边栏控件
    LinearLayout sidebarLinearLayout;
    RecyclerView sidebarRecyclerView;
    //声明表情包显示列表
    List<File> emoticonList = new ArrayList<>();
    //声明表情包压缩包
    File emoticonZipFile;
    private DrawerLayout drawer;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //创建File对象，用于存放解压出的表情包
        emoticonZipFile = new File(getCacheDir(), "img");
        //绑定侧栏的相关布局文件
        sidebarLinearLayout = findViewById(R.id.load_view);
        sidebarRecyclerView = findViewById(R.id.recycler_view);
        LinearLayout sidebarGithubButton = findViewById(R.id.footer_item_github);
        LinearLayout sidebarSettingButton = findViewById(R.id.footer_item_setting);
        drawer = findViewById(R.id.drawer_layout);
        //声明侧栏的动态表情包按钮列表
        LinearLayout buttonContainer;
        ButtonGenerator buttonGenerator;
        //设置状态栏沉浸
        ImmersionBar.with(this)
                .transparentStatusBar()
                .statusBarColor(R.color.colorPrimary)
                .navigationBarColor(R.color.white)
                .init();
        //设置跳转到Github按钮
        sidebarGithubButton.setOnClickListener(v -> {
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
        //设置设置按钮
        sidebarSettingButton.setOnClickListener(V -> Toast.makeText(getApplicationContext(), "前面的区域，以后再来探索吧！", Toast.LENGTH_SHORT).show());

        PackageManager packageManager = getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            String versionName = packageInfo.versionName;
            String versionType = (BuildConfig.DEBUG) ? getString(R.string.app_version_debug) : getString(R.string.app_version_release);

            TextView versionTextView = findViewById(R.id.iv_version);
            versionTextView.setText(getString(R.string.app_version, versionName, versionType));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        //声明并绑定标题栏样式
        Toolbar headerToolbar = findViewById(R.id.header_toolbar);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        //将 headerToolbar 设置为活动的操作栏
        setSupportActionBar(headerToolbar);
        //创建一个ActionBarDrawerToggle对象，用于管理侧滑菜单的开关操作
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, headerToolbar, 0, 0);
        //将 ActionBarDrawerToggle 对象 toggle 添加为 DrawerLayout 控件 drawer 的侦听器
        //当用户执行打开或关闭侧滑菜单的操作时，ActionBarDrawerToggle 可以捕获这些事件并执行相应的操作
        drawer.addDrawerListener(toggle);
        //同步 ActionBarDrawerToggle 的状态
        toggle.syncState();

        //添加头布局和尾布局
        View headerView = navigationView.getHeaderView(0);
        //绑定侧边栏上部布局文件
        ImageView imageView = headerView.findViewById(R.id.iv_head);
        //设置头像显示
        imageView.setImageResource(R.mipmap.image01);
        //设置头像点击事件
        imageView.setOnClickListener(v -> Toast.makeText(getApplicationContext(), "不吃你别扒拉(╯‵□′)╯︵┻━┻", Toast.LENGTH_LONG).show());
        //设置导航视图NavigationView的点击菜单项监听器，用于处理导航菜单项被选中时的操作。
        navigationView.setNavigationItemSelectedListener(menuItem -> false);
        //设置按钮颜色
        ColorStateList csl = ContextCompat.getColorStateList(this, R.color.nav_menu_text_color);
        navigationView.setItemTextColor(csl);
        navigationView.setItemIconTintList(null);
        new EasyWindow<>(this)
                //绑定布局文件
                .setContentView(R.layout.loading_window_hint)
                //设置图片文件
                .setImageDrawable(android.R.id.icon, R.drawable.ic_dialog_loading)
                //设置文字提示
                .setText(android.R.id.message, "正在加载")
                //设置Tag
                .setTag("windowLoading")
                // 设置外层是否能被触摸
                .setOutsideTouchable(false)
                // 设置窗口背景阴影强度
                .setBackgroundDimAmount(0.5f)
                //显示弹窗
                .show();
        //解压默认的表情包压缩包
        showImage("01_小二柴.zip", () -> {
            //完成解压任务后，根据设置的Tag关闭弹窗
            EasyWindow.cancelByTag("windowLoading");
        });

        buttonContainer = findViewById(R.id.buttonContainer);
        headerToolbar = findViewById(R.id.header_toolbar);
        //根据assets目录中的压缩包以及mipmap目录中的图片动态生成按钮，用于切换不同的表情包压缩包
        buttonGenerator = new ButtonGenerator(this, buttonContainer, headerToolbar, this);
        buttonGenerator.setImageClickListener(this);
        buttonGenerator.generateButtons();
    }
    //按钮点击函数，用于处理按钮点击事件
    @Override
    public void onImageClick(String fileName) {
        //关闭侧边栏
        drawer.closeDrawers();
        //弹出正在加载提示框，同时锁定界面，防止连续点击
        new EasyWindow<>(this)
                //绑定布局文件
                .setContentView(R.layout.loading_window_hint)
                //设置图片文件
                //.setImageDrawable(android.R.id.icon, R.drawable.ic_dialog_loading)
                //设置文字提示
                .setText(android.R.id.message, "正在加载")
                //设置Tag
                .setTag("windowLoading")
                // 设置外层是否能被触摸
                .setOutsideTouchable(false)
                // 设置窗口背景阴影强度
                .setBackgroundDimAmount(0.5f)
                //显示弹窗
                .show();
            showImage(fileName, () -> {
            // 这里执行处理showImage完成后的操作
                EasyWindow.cancelByTag("windowLoading");
        });
    }

    private void showImage(String zipFileName, OnImageProcessingListener listener) {
        // 在这里处理解压缩的逻辑
        if (emoticonZipFile.exists()) {
            deleteDirectory(emoticonZipFile);
        }

        new Thread(() -> {
            try {
                Utils.unZip(MainActivity.this, zipFileName, emoticonZipFile.getPath(), true);
                // 这里添加你希望执行的代码
                runOnUiThread(() -> {
                    initList();
                    if (listener != null) {
                        listener.onImageProcessingComplete();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    //回调接口，判断什么时候解压完成
    public interface OnImageProcessingListener {
        void onImageProcessingComplete();
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
        sidebarLinearLayout.setVisibility(View.VISIBLE);
        sidebarRecyclerView.setVisibility(View.GONE);

        emoticonList.clear();

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 5);
        sidebarRecyclerView.setLayoutManager(gridLayoutManager);
        Adapter adapter = new Adapter(emoticonList);
        sidebarRecyclerView.setAdapter(adapter);

        new Thread(() -> {
            try {
                emoticonList = Utils.getList(MainActivity.this);
                runOnUiThread(() -> {
                    adapter.updateList(emoticonList);
                    sidebarLinearLayout.setVisibility(View.GONE);
                    sidebarRecyclerView.setVisibility(View.VISIBLE);
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        Utils.handleActivityResult(this, requestCode, resultCode, resultData, emoticonList);
    }
}
