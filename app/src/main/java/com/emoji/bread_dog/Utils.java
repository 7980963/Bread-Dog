package com.emoji.bread_dog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class Utils {
    //图片列表生成函数
    public static List<File> getList(Context context) {
        File file = new File(context.getCacheDir(), "img");
        String[] files = arraySort(Objects.requireNonNull(file.list()));
        List<File> list = new ArrayList<>();
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
        return list;
    }
    //图片排序函数
    public static String[] arraySort(String[] input){
        for (int i = 0; i < input.length - 1; i++) {
            for (int j = 0; j < input.length - i - 1; j++) {
                if (input[j].compareTo(input[j + 1]) > 0) {
                    String temp = input[j]; input[j] = input[j + 1]; input[j + 1] = temp;
                }
            }
        }
        return input;
    }

    //bmp缩放函数
    public static Bitmap bitMapScale(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postScale(1,1);
        return Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
    }
    //分享模块函数
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
    //保存图片函数
    public static void save(Activity context, File file, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        Uri uri;
        uri = FileProvider.getUriForFile(context, context.getPackageName()+".FileProvider", file);
        intent.setType(context.getContentResolver().getType(uri));
        intent.putExtra(Intent.EXTRA_TITLE, file.getName());
        context.startActivityForResult(intent, requestCode);
    }
    //图片显示函数
    public static void handleActivityResult(Context context, int requestCode, int resultCode, Intent resultData, List<File> list) {
        if(resultCode == Activity.RESULT_OK){
            Uri uri = resultData.getData();
            File file = list.get(requestCode);
            if(uri != null) {
                try {
                    FileInputStream inputStream = new FileInputStream(file);
                    OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
                    int i;
                    byte[] bytes = new byte[1024];
                    while ((i = inputStream.read(bytes)) != -1) {
                        outputStream.write(bytes, 0, i);
                    }
                    inputStream.close();
                    outputStream.close();
                    Toast.makeText(context,"已保存",Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context,"保存失败",Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(context,"创建失败",Toast.LENGTH_LONG).show();
            }
        }
    }
    //解压缩函数
    public static void unZip(Context context, String assetName, String outputDirectory, boolean isReWrite) throws IOException {
        //创建解压目标目录
        File file = new File(outputDirectory);
        //如果目标目录不存在，则创建
        if (!file.exists()) {
            boolean isCreated = file.mkdirs();
            if (!isCreated) {
                // 文件夹创建失败，进行相应的处理逻辑
                Toast.makeText(context,"文件夹创建失败",Toast.LENGTH_LONG).show();
            }
        }
        //打开压缩文件
        InputStream inputStream = context.getAssets().open(assetName);
        //使用Apache Commons Compress库来处理中文名的问题
        ZipArchiveInputStream zipInputStream = new ZipArchiveInputStream(inputStream, "UTF-8", true);
        //使用1M buffer
        byte[] buffer = new byte[1024 * 1024];
        //解压时字节计数
        int count;
        //遍历压缩包中文件和目录
        ZipArchiveEntry zipEntry = zipInputStream.getNextZipEntry();
        while (zipEntry != null) {
            //获取文件名
            String name = zipEntry.getName();
            //判断是否为文件夹
            if (zipEntry.isDirectory()) {
                file = new File(outputDirectory + File.separator + name);
                if (isReWrite || !file.exists()) {
                    boolean isCreated = file.mkdirs();
                    if (!isCreated) {
                        // 文件夹创建失败，进行相应的处理逻辑
                        Toast.makeText(context,"文件夹创建失败",Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                //如果是文件
                file = new File(outputDirectory + File.separator + name);
                if (isReWrite || !file.exists()) {
                    //创建文件
                    boolean isCreated = file.createNewFile();
                    if (!isCreated) {
                        // 文件创建失败，进行相应的处理逻辑
                        Toast.makeText(context,"文件创建失败",Toast.LENGTH_LONG).show();
                    }
                    //写入文件内容
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    while ((count = zipInputStream.read(buffer)) > 0) {
                        fileOutputStream.write(buffer, 0, count);
                    }
                    fileOutputStream.close();
                }
            }
            //定位到下一个文件入口
            zipEntry = zipInputStream.getNextZipEntry();
        }
        //关闭流
        zipInputStream.close();
    }
}

