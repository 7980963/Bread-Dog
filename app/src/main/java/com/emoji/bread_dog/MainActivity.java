package com.emoji.bread_dog;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    LinearLayout linearLayout;
    RecyclerView recyclerView;
    List<File> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        linearLayout = findViewById(R.id.load_view);
        recyclerView = findViewById(R.id.recycler_view);
        File file = new File(getCacheDir(),"img");
        if(file.exists()){
            initList();
        } else {
            new Thread(() -> {
                try {
                    UnzipFromAssets.unZip(MainActivity.this, "1_小二柴.zip", file.getPath(), true);
                    runOnUiThread(this::initList);
                } catch (Exception e) {
                    //runOnUiThread(() -> Toast.makeText(MainActivity.this, "资源释放失败，请重试", Toast.LENGTH_LONG).show());
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show());
                }
            }).start();
        }
    }

    private void initList() {
        List<File> list = Utils.getList(this);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,5);
        recyclerView.setLayoutManager(gridLayoutManager);
        Adapter adapter = new Adapter(list);
        recyclerView.setAdapter(adapter);
        linearLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        Utils.handleActivityResult(this, requestCode, resultCode, resultData, list);
    }

}