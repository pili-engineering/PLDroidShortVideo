package com.qiniu.shortvideo.app.utils;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

    public static boolean copyFile(File input, File output) {
        createParentDirIfNotExists(input);
        createParentDirIfNotExists(output);
        FileInputStream is = null;
        FileOutputStream os = null;
        try {
            is = new FileInputStream(input);
            os = new FileOutputStream(output);
            copyFile(is, os);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        close(is);
        close(os);
        return false;
    }

    public static boolean copyFile(File input, OutputStream os) {
        createParentDirIfNotExists(input);
        FileInputStream is = null;
        try {
            is = new FileInputStream(input);
            copyFile(is, os);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        close(is);
        close(os);
        return true;
    }

    public static void copyFile(InputStream is, OutputStream os) throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) != -1) {
            os.write(buffer, 0, len);
        }
        os.flush();
    }

    private static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void createParentDirIfNotExists(File file) {
        File parentFile = file.getParentFile();
        if (parentFile != null && !parentFile.exists()) {
            parentFile.mkdirs();
        }
    }
}
