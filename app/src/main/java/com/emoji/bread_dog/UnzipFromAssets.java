package com.emoji.bread_dog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import android.content.Context;

/**
 * 从assets目录解压zip到本地
 */
public class UnzipFromAssets {
    /**
     * 解压assets的zip压缩文件到指定目录
     * @param context 上下文对象
     * @param assetName 压缩文件名
     * @param outputDirectory 输出目录
     * @param isReWrite 是否覆盖
     * @throws IOException
     */
    public static void unZip(Context context, String assetName, String outputDirectory, boolean isReWrite) throws IOException {
        //创建解压目标目录
        File file = new File(outputDirectory);
        //如果目标目录不存在，则创建
        if (!file.exists()) {
            file.mkdirs();
        }
        //打开压缩文件
        InputStream inputStream = context.getAssets().open(assetName);
        //使用Apache Commons Compress库来处理中文名的问题
        ZipArchiveInputStream zipInputStream = new ZipArchiveInputStream(inputStream, "UTF-8", true);
        //使用1M buffer
        byte[] buffer = new byte[1024 * 1024];
        //解压时字节计数
        int count = 0;
        //遍历压缩包中文件和目录
        ZipArchiveEntry zipEntry = zipInputStream.getNextZipEntry();
        while (zipEntry != null) {
            //获取文件名
            String name = zipEntry.getName();
            //判断是否为文件夹
            if (zipEntry.isDirectory()) {
                file = new File(outputDirectory + File.separator + name);
                if (isReWrite || !file.exists()) {
                    file.mkdirs();
                }
            } else {
                //如果是文件
                file = new File(outputDirectory + File.separator + name);
                if (isReWrite || !file.exists()) {
                    //创建文件
                    file.createNewFile();
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