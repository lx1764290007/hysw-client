/**
 * 缓存数据使用
 */
package com.gdxydgyhlw.heyuan.libs;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class Cache {
    private Context context;
    public  final String DEVICES = "devices";
    public  final String COLLECTS = "collects";
    public final String SPINNER_POSITION = "spinner_position";
    public final String ALARM = "alarm";
    public final String PIE = "pie_chart";
    public final String DEVICE_SEARCH_PAGE_TOTAL = "device_search_page_total";
    public final String DEVICE_SEARCH_PAGE_CURRENT = "device_search_page_current";
    public final String DEVICE_SEARCH_PAGE_DATA = "device_search_page_data";
    public Cache(Context context) {
        this.context = context;
    }

    // 存储数据到缓存目录
    public void saveToCache(String filename, String data) {
        File cacheDir = context.getCacheDir();
        File myCacheFile = new File(cacheDir, filename);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(myCacheFile);
            fos.write(data.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 从缓存目录读取数据
    public String readFromCache(String filename) {
        File cacheDir = context.getCacheDir();
        File myCacheFile = new File(cacheDir, filename);

        FileInputStream fis = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            fis = new FileInputStream(myCacheFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));

            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return stringBuilder.toString();
    }
    public void deleteCache() {
        File cacheDir = context.getCacheDir();

        File[] files = cacheDir.listFiles();
        for (File file : files) {
            file.delete();
        }

    }

    /**
     * 计算缓存大小
     * @return
     */
    public String getCacheSize(){
        long cacheSize = getFolderSize(this.context.getCacheDir());
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){ // 判断外部存储是否可用
            cacheSize += getFolderSize(this.context.getExternalCacheDir());
        }
        return formatSize(cacheSize);
    }


    /**
     * 计算文件夹内部文件大小总和
     * @param file 文件夹
     * @return 文件大小总和
     */
    private long getFolderSize(File file) {
        long size = 0;
        try{
            File[] files = file.listFiles(); // 获取文件夹内的文件列表
            for (File file1: files){
                if (file1.isDirectory()){ // 如果某文件是文件夹，计算内部总和
                    size += getFolderSize(file1);
                }else{
                    size += file1.length();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return size;
    }


    /**
     * 将缓存换算成对应的单位
     * @param cacheSize 缓存大小
     * @return String类型的缓存大小
     */
    private String formatSize(long cacheSize) {
        double kb = cacheSize/1024;
        if (kb < 1){ // cacheSize在 0-1024内， 单位为b
            return cacheSize/1024 + "B";
        }
        double mb = kb/1024;
        if (mb < 1){
            return String.format("%.2f", kb) + "KB";
        }
        double gb = mb/1024;
        if (gb < 1) {
            return String.format("%.2f", mb) + "MB";
        }
        return String.format("%.2f", gb) + "GB";
    }


    /**
     * 删除文件夹
     * @param dir 文件夹路径
     */
    private static void deleteFolder(File dir) {
        try{
            File[] files = dir.listFiles();
            for (File file: files){
                if (file.isDirectory()){
                    deleteFolder(file);
                }else
                    file.delete();
            }
            dir.delete();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
