package com.qiniu.shortvideo.app.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.qiniu.shortvideo.app.model.AudioFile;
import com.qiniu.shortvideo.app.model.MediaFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 本地媒体文件获取的工具类
 */
public class MediaUtils {
    public static final int MEDIA_TYPE_PHOTO = 0;
    public static final int MEDIA_TYPE_VIDEO = 1;
    public static final int MEDIA_TYPE_ALL = 2;

    public static final int FIRST_NOTIFY_SIZE = 20;
    public static final int NOTIFY_SIZE_OFFSET = 20;

    public interface LocalMediaCallback {
        void onLocalMediaFileUpdate(List<MediaFile> mediaFiles);
    }

    public static void getLocalMedia(final Context context, final int mediaType, final LocalMediaCallback localMediaCallback) {
        ThreadPoolUtils.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                // 获取视频
                Cursor videoCursor = null;
                if (mediaType == MEDIA_TYPE_VIDEO || mediaType == MEDIA_TYPE_ALL) {
                    Uri videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    String[] projection = {MediaStore.Video.Media._ID,
                            MediaStore.Video.Media.DATA,
                            MediaStore.Video.Media.DURATION,
                            MediaStore.Video.Media.SIZE,
                            MediaStore.Video.Media.DATE_ADDED,
                            MediaStore.Video.Media.DISPLAY_NAME,
                            MediaStore.Video.Media.DATE_MODIFIED};

                    String where = MediaStore.Video.Media.MIME_TYPE + "=? or "
                            + MediaStore.Video.Media.MIME_TYPE + "=? or "
                            + MediaStore.Video.Media.MIME_TYPE + "=? or "
                            + MediaStore.Video.Media.MIME_TYPE + "=? or "
                            + MediaStore.Video.Media.MIME_TYPE + "=? or "
                            + MediaStore.Video.Media.MIME_TYPE + "=?";
                    String[] whereArgs = {"video/mp4", "video/3gp", "video/flv", "video/mkv", "video/mov", "video/mpg"};
                    videoCursor = context.getContentResolver().query(videoUri,
                            projection, where, whereArgs, MediaStore.Video.Media.DATE_ADDED + " DESC ");
                }

                // 获取图片
                Cursor imageCursor = null;
                if (mediaType == MEDIA_TYPE_PHOTO || mediaType == MEDIA_TYPE_ALL) {
                    Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    String[] projection = {MediaStore.Images.Media._ID,
                            MediaStore.Images.Media.DATA,
                            MediaStore.Images.Media.TITLE,
                            MediaStore.Images.Media.MIME_TYPE,
                            MediaStore.Images.Media.DATE_ADDED};

                    imageCursor = context.getContentResolver().query(imageUri,
                            projection, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC ");
                }

                int totalMediaCount = (videoCursor == null ? 0 : videoCursor.getCount()) + (imageCursor == null ? 0 : imageCursor.getCount());

                int colVideoId = 0;
                int colVideoData = 0;
                int colVideoDuration = 0;
                int colVideoDisplayName = 0;
                int colVideoAddTime = 0;

                int colImageId = 0;
                int colImageData = 0;
                int colImageDisplayName = 0;
                int colImageAddTime = 0;

                if (videoCursor != null) {
                    colVideoId = videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
                    colVideoData = videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                    colVideoDuration = videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
                    colVideoDisplayName = videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
                    colVideoAddTime = videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED);
                }
                if (imageCursor != null) {
                    colImageId = imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                    colImageData = imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    colImageDisplayName = imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE);
                    colImageAddTime = imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED);
                }

                boolean videoMoveToNext = true;
                boolean imageMoveToNext = true;
                MediaFile videoFile = null;
                MediaFile imageFile = null;
                List<MediaFile> mediaList = new ArrayList<>();
                int notifySize = FIRST_NOTIFY_SIZE;
                for (int i = 0; i < totalMediaCount; i++) {
                    if (videoCursor != null) {
                        while (videoFile == null && videoMoveToNext && videoCursor.moveToNext()) {
                            videoFile = generateVideoFile(context, videoCursor, colVideoId, colVideoData, colVideoDuration, colVideoDisplayName, colVideoAddTime);
                        }
                    }
                    if (imageCursor != null) {
                        while (imageFile == null && imageMoveToNext && imageCursor.moveToNext()) {
                            imageFile = generateImageFile(context, imageCursor, colImageId, colImageData, colImageDisplayName, colImageAddTime);
                        }
                    }
                    if (videoFile != null && imageFile == null) {
                        mediaList.add(videoFile);
                        videoMoveToNext = true;
                        videoFile = null;
                    } else if (videoFile == null && imageFile != null) {
                        mediaList.add(imageFile);
                        imageMoveToNext = true;
                        imageFile = null;
                    } else if (videoFile != null) {
                        if (videoFile.getAddTime() > imageFile.getAddTime()) {
                            mediaList.add(videoFile);
                            videoMoveToNext = true;
                            imageMoveToNext = false;
                            videoFile = null;
                        } else {
                            mediaList.add(imageFile);
                            videoMoveToNext = false;
                            imageMoveToNext = true;
                            imageFile = null;
                        }
                    }

                    if (mediaList.size() == notifySize) {
                        if (localMediaCallback != null) {
                            localMediaCallback.onLocalMediaFileUpdate(mediaList);
                        }
                        mediaList = new ArrayList<>();
                        notifySize += NOTIFY_SIZE_OFFSET;
                    }
                }
                if (videoCursor != null) {
                    videoCursor.close();
                }
                if (imageCursor != null) {
                    imageCursor.close();
                }

                if (localMediaCallback != null) {
                    localMediaCallback.onLocalMediaFileUpdate(mediaList);
                }
            }
        });
    }

    public static ArrayList<AudioFile> getLocalAudios(Context context) {

        ArrayList<AudioFile> audioFiles = null;

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.YEAR,
                        MediaStore.Audio.Media.MIME_TYPE,
                        MediaStore.Audio.Media.SIZE,
                        MediaStore.Audio.Media.DATA },
                MediaStore.Audio.Media.MIME_TYPE + "=? or "
                        + MediaStore.Audio.Media.MIME_TYPE + "=?",
                new String[] { "audio/mpeg", "audio/x-ms-wma" }, null);

        audioFiles = new ArrayList<AudioFile>();

        if (cursor != null && cursor.moveToFirst()) {
            AudioFile audioFile = null;
            do {
                audioFile = new AudioFile();
                // 文件名
                audioFile.setFileName(cursor.getString(1));
                // 歌曲名
                audioFile.setTitle(cursor.getString(2));
                // 时长
                audioFile.setDuration(cursor.getInt(3));
                // 歌手名
                audioFile.setSinger(cursor.getString(4));
                // 专辑名
                audioFile.setAlbum(cursor.getString(5));
                // 年代
                if (cursor.getString(6) != null) {
                    audioFile.setYear(cursor.getString(6));
                } else {
                    audioFile.setYear("未知");
                }
                // 歌曲格式
                if ("audio/mpeg".equals(cursor.getString(7).trim())) {
                    audioFile.setType("mp3");
                } else if ("audio/x-ms-wma".equals(cursor.getString(7).trim())) {
                    audioFile.setType("wma");
                }
                // 文件大小
                if (cursor.getString(8) != null) {
                    float size = cursor.getInt(8) / 1024f / 1024f;
                    audioFile.setSize((size + "").substring(0, 4) + "M");
                } else {
                    audioFile.setSize("未知");
                }
                // 文件路径
                if (cursor.getString(9) != null) {
                    audioFile.setFilePath(cursor.getString(9));
                }
                audioFiles.add(audioFile);
            } while (cursor.moveToNext());

            cursor.close();

        }
        return audioFiles;
    }

    private static boolean isFileExists(String strFile) {
        try {
            File f = new File(strFile);
            if (!f.exists()) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private static MediaFile generateVideoFile(Context context, Cursor cursor, int colId, int colData, int colDuration, int colDisplayName, int colAddTime) {
        String filePath = cursor.getString(colData);
        if (!new File(filePath).exists()) {
            return null;
        }

        int videoId = cursor.getInt(colId);
        long duration = cursor.getLong(colDuration);
        String displayName = cursor.getString(colDisplayName);
        long addTime = cursor.getLong(colAddTime);
        String thumbnailPath = null;
        Cursor thumbCursor = context.getContentResolver().query(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,
                new String[]{
                        MediaStore.Video.Thumbnails.DATA,
                        MediaStore.Video.Thumbnails.VIDEO_ID
                },
                MediaStore.Video.Thumbnails.VIDEO_ID + "=?",
                new String[]{String.valueOf(videoId)}, null);
        if (thumbCursor != null && thumbCursor.moveToFirst()) {
            thumbnailPath = thumbCursor.getString(
                    thumbCursor.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.DATA));
            thumbCursor.close();
        }
        return new MediaFile(videoId, MediaFile.VIDEO, filePath, thumbnailPath == null ? filePath : thumbnailPath, duration, displayName, addTime);
    }

    private static MediaFile generateImageFile(Context context, Cursor cursor, int colId, int colData, int colDisplayName, int colAddTime) {
        String filePath = cursor.getString(colData);
        if (!new File(filePath).exists()) {
            return null;
        }
        int imageId = cursor.getInt(colId);
        String displayName = cursor.getString(colDisplayName);
        long addTime = cursor.getLong(colAddTime);
        String thumbnailPath = null;
        Cursor thumbCursor = context.getContentResolver().query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                new String[]{
                        MediaStore.Images.Thumbnails.DATA,
                        MediaStore.Images.Thumbnails.IMAGE_ID
                },
                MediaStore.Images.Thumbnails.IMAGE_ID + "=?",
                new String[]{String.valueOf(imageId)}, null);
        if (thumbCursor != null && thumbCursor.moveToFirst()) {
            thumbnailPath = thumbCursor.getString(
                    thumbCursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA));
            thumbCursor.close();
        }
        return new MediaFile(imageId, MediaFile.IMAGE, filePath, thumbnailPath == null ? filePath : thumbnailPath, displayName, addTime);
    }
}
