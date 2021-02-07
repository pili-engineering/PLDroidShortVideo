package com.qiniu.pili.droid.shortvideo.demo.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;

public class MediaStoreUtils {

    public static Uri storeVideo(Context context, File srcFile, String mime) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            File dstFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), srcFile.getName());
            boolean succeed = FileUtils.copyFile(srcFile, dstFile);
            if (succeed) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Video.Media.DATA, dstFile.getAbsolutePath());
                values.put(MediaStore.Video.Media.MIME_TYPE, mime);
                Uri uri = context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(uri);
                context.sendBroadcast(intent);
                return Uri.fromFile(dstFile);
            }
        } else {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Video.Media.DISPLAY_NAME, getDisplayName(srcFile));
            values.put(MediaStore.Video.Media.MIME_TYPE, mime);
            values.put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis());

            ContentResolver resolver = context.getContentResolver();
            Uri insertUri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
            if (insertUri != null) {
                try {
                    OutputStream outputStream = resolver.openOutputStream(insertUri);
                    boolean succeed = FileUtils.copyFile(srcFile, outputStream);
                    if (succeed) {
                        return insertUri;
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static Uri storeAudio(Context context, File srcFile, String mime) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            File dstFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), srcFile.getName());
            boolean succeed = FileUtils.copyFile(srcFile, dstFile);
            if (succeed) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Audio.Media.DATA, dstFile.getAbsolutePath());
                values.put(MediaStore.Audio.Media.MIME_TYPE, mime);
                Uri uri = context.getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(uri);
                context.sendBroadcast(intent);
                return Uri.fromFile(dstFile);
            }
        } else {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Audio.Media.DISPLAY_NAME, getDisplayName(srcFile));
            values.put(MediaStore.Audio.Media.MIME_TYPE, mime);
            values.put(MediaStore.Audio.Media.DATE_TAKEN, System.currentTimeMillis());

            ContentResolver resolver = context.getContentResolver();
            Uri insertUri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
            if (insertUri != null) {
                try {
                    OutputStream outputStream = resolver.openOutputStream(insertUri);
                    boolean succeed = FileUtils.copyFile(srcFile, outputStream);
                    if (succeed) {
                        return insertUri;
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static Uri storeImage(Context context, File srcFile, String mime) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            File dstFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), srcFile.getName());
            boolean succeed = FileUtils.copyFile(srcFile, dstFile);
            if (succeed) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, dstFile.getAbsolutePath());
                values.put(MediaStore.Images.Media.MIME_TYPE, mime);
                Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(uri);
                context.sendBroadcast(intent);
                return Uri.fromFile(dstFile);
            }
        } else {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, getDisplayName(srcFile));
            values.put(MediaStore.Images.Media.MIME_TYPE, mime);
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());

            ContentResolver resolver = context.getContentResolver();
            Uri insertUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (insertUri != null) {
                try {
                    OutputStream outputStream = resolver.openOutputStream(insertUri);
                    boolean succeed = FileUtils.copyFile(srcFile, outputStream);
                    if (succeed) {
                        return insertUri;
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private static String getDisplayName(File file) {
        String fileName = file.getName().substring(0, file.getName().lastIndexOf("."));
        String fileType = file.getName().substring(file.getName().lastIndexOf("."));
        String displayName = fileName + "_" + System.currentTimeMillis() + fileType;
        return displayName;
    }

}
