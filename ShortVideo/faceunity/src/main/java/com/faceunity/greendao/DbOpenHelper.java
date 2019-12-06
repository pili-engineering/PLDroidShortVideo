package com.faceunity.greendao;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.faceunity.entity.MagicPhotoEntity;
import com.faceunity.utils.FileUtils;

import org.greenrobot.greendao.database.Database;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Lq on 2018.12.02
 */
public class DbOpenHelper extends DaoMaster.OpenHelper {
    private static final String TAG = "DbOpenHelper";
    private Context mContext;

    public DbOpenHelper(Context context, String name) {
        super(context, name);
        mContext = context;
    }

    public static List<MagicPhotoEntity> getDefaultMagicPhotos(Context context) {
        List<MagicPhotoEntity> magicPhotoEntities = new ArrayList<>();
        File magicPhotoDir = FileUtils.getMagicPhotoDir(context);
        File[] files = magicPhotoDir.listFiles();
        MagicPhotoEntity magicPhotoEntity;
        Arrays.sort(files);
        for (File file : files) {
            File[] mf = file.listFiles();
            Arrays.sort(mf);
            magicPhotoEntity = new MagicPhotoEntity();
            for (File f : mf) {
                String name = f.getName();
                if (name.endsWith(".json")) {
                    try {
                        String s = FileUtils.readStringFromFile(f);
                        JSONObject jsonObject = new JSONObject(s);
                        int width = jsonObject.optInt("width");
                        int height = jsonObject.optInt("height");
                        JSONArray pointsArray = jsonObject.optJSONArray("group_points");
                        int len = pointsArray.length();
                        double[] groupPoints = new double[len];
                        for (int i = 0; i < len; i++) {
                            groupPoints[i] = pointsArray.optDouble(i);
                        }
                        JSONArray typeArray = jsonObject.optJSONArray("group_type");
                        len = typeArray.length();
                        double[] groupType = new double[len];
                        for (int i = 0; i < len; i++) {
                            groupType[i] = typeArray.optDouble(i);
                        }
                        magicPhotoEntity.setWidth(width);
                        magicPhotoEntity.setHeight(height);
                        magicPhotoEntity.setGroupPoints(groupPoints);
                        magicPhotoEntity.setGroupType(groupType);
                    } catch (Exception e) {
                        Log.e(TAG, "getDefaultMagicPhotos: ", e);
                    }
                } else if (name.endsWith(".jpg") || name.endsWith(".png")) {
                    magicPhotoEntity.setImagePath(f.getAbsolutePath());
                }
            }
            magicPhotoEntities.add(magicPhotoEntity);
        }
        return magicPhotoEntities;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        super.onCreate(db);
        // 初始化默认的异图数据
        List<MagicPhotoEntity> defaultMagicPhotos = getDefaultMagicPhotos(mContext);
        String tableName = "MAGIC_PHOTO_ENTITY";
        if (defaultMagicPhotos.size() > 0) {
            try {
                db.beginTransaction();
                for (MagicPhotoEntity defaultMagicPhoto : defaultMagicPhotos) {
                    ContentValues values = new ContentValues();
                    values.put("WIDTH", defaultMagicPhoto.getWidth());
                    values.put("HEIGHT", defaultMagicPhoto.getHeight());
                    values.put("GROUP_POINTS_STR", defaultMagicPhoto.getGroupPointsStr());
                    values.put("GROUP_TYPE_STR", defaultMagicPhoto.getGroupTypeStr());
                    values.put("IMAGE_PATH", defaultMagicPhoto.getImagePath());
                    db.insert(tableName, null, values);
                }
                db.setTransactionSuccessful();
            } catch (Exception e) {
                Log.e(TAG, "onCreate: ", e);
            } finally {
                db.endTransaction();
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);
        onCreate(db);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);
        // TODO: 2018/12/19 0019 数据库升级。不能强行删除
        Log.d(TAG, "onUpgrade: oldVersion:" + oldVersion + ", newVersion:" + newVersion);
        DaoMaster.dropAllTables(db, true);
    }

}
