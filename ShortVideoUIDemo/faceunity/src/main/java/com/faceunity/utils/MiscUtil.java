package com.faceunity.utils;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.faceunity.utils.Constant.filePath;

/**
 * Created by lirui on 2017/3/6.
 */

public class MiscUtil {

    private static boolean isDebug = true;
    private static String TAG = "FU-MiscUtil";

    public static boolean VERBOSE_LOG = false;

    public static void Logger(String tag, String msg, boolean isImportant) {
        if (isImportant || isDebug) {
            Log.e(tag, msg);
        }
    }

    public static void checkPermission(Context context, String permission) {
        Logger(TAG, "checkPermission " + permission, false);
        if (ContextCompat.checkSelfPermission(context, permission)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{permission}, 0);
        }
    }

    public static void checkPermission(Context context) {
        Logger(TAG, "checkPermission", false);
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "no permission");
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.RECORD_AUDIO}, 0);
        }
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param context Context to get resources and device specific display metrics
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static int convertDpToPixel(Context context, float dp) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        int px = (int) (dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param context Context to get resources and device specific display metrics
     * @param px      A value in px (pixels) unit. Which we need to convert into db
     * @return A float value to represent dp equivalent to px value
     */
    public static int convertPixelsToDp(Context context, float px) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        int dp = (int) (px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
        return dp;
    }

    private Bitmap processImage(int width, int height, byte[] data) throws IOException {
        // Determine the width/height of the image
        //int width = camera.getParameters().getPictureSize().width;
        //int height = camera.getParameters().getPictureSize().height;

        // Load the bitmap from the byte array
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);

        // Rotate and crop the image into a square
        int croppedWidth = (width > height) ? height : width;
        int croppedHeight = (width > height) ? height : width;

        //Matrix matrix = new Matrix();
        //matrix.postRotate(IMAGE_ORIENTATION);
        //Bitmap cropped = Bitmap.createBitmap(bitmap, 0, 0, croppedWidth, croppedHeight, matrix, true);
        //bitmap.recycle();

        // Scale down to the output size
        //Bitmap scaledBitmap = Bitmap.createScaledBitmap(cropped, IMAGE_SIZE, IMAGE_SIZE, true);
        //cropped.recycle();

        //return scaledBitmap;
        return null;
    }

    public static String createFileName() {
        File dir = new File(filePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return filePath + getCurrentDate() +
                "_" + System.currentTimeMillis();
    }

    public static void createFile(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static String saveDataToFile(String fileName, String fileExtName, final byte[] data) {
        File dir = new File(filePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        final String fileFullName = filePath + fileName + "." + fileExtName;
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                File file = new File(fileFullName);
                Logger(TAG, "saveDataToFile " + fileFullName, false);
                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(data);
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    Log.e(TAG, "saveDataToFile", e);
                }
            }
        });
        return fileFullName;
    }

    public static String saveBitmapToFile(final Bitmap bitmap) {
        File dir = new File(filePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        final String fileName = filePath + getCurrentDate() +
                "_" + System.currentTimeMillis() + ".jpg";
        Logger(TAG, "file : " + fileName, false);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    File file = new File(fileName);
                    FileOutputStream fos = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                    fos.flush();
                    fos.close();
                    bitmap.recycle();
                } catch (IOException e) {
                    Log.e(TAG, "saveBitmapToFile: ", e);
                }
            }
        });
        return fileName;
    }

    public static String saveBitmap(final Bitmap bitmap, String dir, String name) {
        File dirFile = new File(dir);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        final String path = new File(dir, name).getAbsolutePath();
        try {
            File file = new File(path);
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            return path;
        } catch (IOException e) {
            Log.e(TAG, "saveBitmap: ", e);
        }
        return null;
    }

    public static Bitmap getBitmapFromPath(String filePath) {
        return BitmapFactory.decodeFile(filePath, new BitmapFactory.Options());
    }

    public static Bitmap getBitmapFromUri(Context context, Uri uri) throws IOException {
        Logger(TAG, "getBitmapFromUri " + uri.toString(), false);
        ParcelFileDescriptor parcelFileDescriptor =
                context.getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Logger(TAG, "uri FileDescriptor : " + fileDescriptor.toString(), false);
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    public static String getCurrentDate() {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
        return df.format(new Date());
    }

    public static void saveInputStreamToFile(InputStream is, FileOutputStream fos, long total) {
        byte[] buf = new byte[2048];
        int len = 0;
        MiscUtil.Logger(TAG, "total------>" + total, false);
        long current = 0;
        try {
            while ((len = is.read(buf)) != -1) {
                current += len;
                fos.write(buf, 0, len);
                MiscUtil.Logger(TAG, "current------>" + current, false);
            }
            fos.flush();
        } catch (IOException e) {
            Log.e(TAG, "saveInputStreamToFile: ", e);
        }
    }

    public static String getFileAbsolutePath(Context context, Uri fileUri) {
        if (context == null || fileUri == null)
            return null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, fileUri)) {
            if (isExternalStorageDocument(fileUri)) {
                String docId = DocumentsContract.getDocumentId(fileUri);
                String[] split = docId.split(":");
                String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(fileUri)) {
                String id = DocumentsContract.getDocumentId(fileUri);
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(fileUri)) {
                String docId = DocumentsContract.getDocumentId(fileUri);
                String[] split = docId.split(":");
                String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                String selection = MediaStore.Images.Media._ID + "=?";
                String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } // MediaStore (and general)
        else if ("content".equalsIgnoreCase(fileUri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(fileUri))
                return fileUri.getLastPathSegment();
            return getDataColumn(context, fileUri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(fileUri.getScheme())) {
            return fileUri.getPath();
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String[] projection = {MediaStore.Images.Media.DATA};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static boolean isEmptyString(String str) {
        return str == null || str.equals("");
    }

    public static int getScreenWidth(Context context) {
        //WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        //return wm.getDefaultDisplay().getWidth();
        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        return dm.widthPixels;
    }

    public static String getMediaLatestPictureThumbnailPath(Context context) {
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(
                MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                new String[]{
                        MediaStore.Images.Thumbnails.IMAGE_ID,
                        MediaStore.Images.Thumbnails.DATA
                },
                null,
                null,
                null
        );
        cursor.moveToLast();
        int imageId = cursor.getInt(0);
        String imagePath = cursor.getString(1);
        /*cursor = cr.query(
                MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                new String[] {
                        MediaStore.Images.Media.DATA
                },
                MediaStore.Audio.Media._ID + "=" + imageId
                )*/
        if (VERBOSE_LOG) {
            MiscUtil.Logger(TAG, "latest media thumbnail path " + imagePath, false);
        }
        return imagePath;
    }

    public static boolean copyFilesTo(File srcDir, File destDir) throws IOException {

        if (!srcDir.isDirectory() || !destDir.isDirectory())
            return false;// 判断是否是目录
        if (!destDir.exists())
            return false;// 判断目标目录是否存在
        File[] srcFiles = srcDir.listFiles();
        for (int i = 0; i < srcFiles.length; i++) {
            if (srcFiles[i].isFile()) {
                // 获得目标文件
                File destFile = new File(destDir.getPath() + "//"
                        + srcFiles[i].getName());
                copyFileTo(srcFiles[i], destFile);
            } else if (srcFiles[i].isDirectory()) {
                File theDestDir = new File(destDir.getPath() + "//"
                        + srcFiles[i].getName());
                copyFilesTo(srcFiles[i], theDestDir);
            }
        }
        return true;
    }

    public static boolean copyFileTo(final File srcFile, final File destFile) {

        if (srcFile.isDirectory() || destFile.isDirectory())
            return false;// 判断是否是文件
        try {
            FileInputStream fis = new FileInputStream(srcFile);
            FileOutputStream fos = new FileOutputStream(destFile);
            int readLen = 0;
            byte[] buf = new byte[1024];
            while ((readLen = fis.read(buf)) != -1) {
                fos.write(buf, 0, readLen);
            }
            fos.flush();
            fos.close();
            fis.close();
        } catch (IOException e) {
            Log.e(TAG, "copyFileTo: ", e);
        }
        return true;
    }

    public static void deleteDirWihtFile(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return;
        for (File file : dir.listFiles()) {
            if (file.isFile())
                file.delete(); // 删除所有文件
            else if (file.isDirectory())
                deleteDirWihtFile(file); // 递规的方式删除文件夹
        }
        dir.delete();// 删除目录本身
    }

    public static byte[] int2byte(int res) {
        byte[] targets = new byte[4];

        targets[0] = (byte) (res & 0xff);// 最低位
        targets[1] = (byte) ((res >> 8) & 0xff);// 次低位
        targets[2] = (byte) ((res >> 16) & 0xff);// 次高位
        targets[3] = (byte) (res >>> 24);// 最高位,无符号右移。
        return targets;
    }
}
