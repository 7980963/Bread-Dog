package com.emoji.bread_dog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
                    UnzipFromAssets.unZip(MainActivity.this, "image.zip", file.getPath(), true);
                    runOnUiThread(this::initList);
                } catch (Exception e) {
                    //runOnUiThread(() -> Toast.makeText(MainActivity.this, "资源释放失败，请重试", Toast.LENGTH_LONG).show());
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show());
                }
            }).start();
        }
    }

    private void initList(){
        File file = new File(getCacheDir(),"img"); String[] files = arraySort(Objects.requireNonNull(file.list()));
        for (String s : files) {
            list.add(new File(file, s));
        }

        // 创建针对中文的Collator实例
        Collator collator = Collator.getInstance(Locale.CHINA);

        // 使用Collator比较文件名进行排序
        list.clear();
        Arrays.sort(files, collator::compare);
        for (String s : files) {
            if(list.isEmpty() || !list.get(list.size()-1).getName().equals(s)) {
                list.add(new File(file, s));
            }
        }

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,5);
        recyclerView.setLayoutManager(gridLayoutManager);
        Adapter adapter = new Adapter(list);
        recyclerView.setAdapter(adapter);
        linearLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

    }

    public static String[] arraySort(String[] input){
        for (int i = 0; i < input.length - 1; i++) {
            for (int j = 0; j < input.length - i - 1; j++) {
                if (input[j].compareTo(input[j + 1]) > 0) {
                    String temp = input[j]; input[j] = input[j + 1]; input[j + 1] = temp;
                } } }
        return input;
    }

    //bmp缩放
    //bmp缩放
    public static Bitmap bitMapScale(Bitmap bitmap,float scale) {
        Matrix matrix = new Matrix();
        matrix.postScale(1,1);
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
        return resizeBmp;
    }


    public static void share(Context context, File file, int type){
        Uri uri;
        uri = FileProvider.getUriForFile(context, context.getPackageName()+".FileProvider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION| Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setType(context.getContentResolver().getType(uri));
        intent.putExtra(Intent.EXTRA_STREAM,uri);
        switch (type){
            case 1:
                intent.setClassName("com.tencent.mobileqq", "com.tencent.mobileqq.activity.JumpActivity");
                break;
            case 2:
                intent.setClassName("com.tencent.mm","com.tencent.mm.ui.tools.ShareImgUI");
                break;
            case 3:
                intent.setClassName("com.tencent.tim","com.tencent.mobileqq.activity.JumpActivity");
                break;
            default:
        }
        try {
            context.startActivity(Intent.createChooser(intent, "分享"));
        } catch (Exception e){
            e.printStackTrace();
            Toast.makeText(context,"失败",Toast.LENGTH_LONG).show();
        }
    }

    public static void save(Activity context, File file, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        Uri uri;
        uri = FileProvider.getUriForFile(context, context.getPackageName()+".FileProvider", file);
        intent.setType(context.getContentResolver().getType(uri));
        intent.putExtra(Intent.EXTRA_TITLE, file.getName());
        context.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if(resultCode == RESULT_OK){
            Uri uri = resultData.getData();
            File file = list.get(requestCode);
            if(uri != null) {
                try {
                    FileInputStream inputStream = new FileInputStream(file);
                    OutputStream outputStream = getContentResolver().openOutputStream(uri);
                    int i;
                    byte[] bytes = new byte[1024];
                    while ((i = inputStream.read(bytes)) != -1) {
                        outputStream.write(bytes, 0, i);
                    }
                    inputStream.close();
                    outputStream.close();
                    Toast.makeText(this,"已保存",Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this,"保存失败",Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this,"创建失败",Toast.LENGTH_LONG).show();
            }
        }
    }

}