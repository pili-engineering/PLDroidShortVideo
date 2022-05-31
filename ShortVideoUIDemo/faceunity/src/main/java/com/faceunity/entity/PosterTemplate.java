package com.faceunity.entity;

import android.content.Context;
import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.faceunity.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author LiuQiang on 2018.10.09
 */
public class PosterTemplate implements Comparable<PosterTemplate> {
    public static final String GRID_ITEM = "grid";
    public static final String LIST_ITEM = "list";
    private String path;
    private String description;
    private String gridIconPath;
    private String listIconPath;

    public PosterTemplate(String gridIconPath, String listIconPath, String path) {
        this.gridIconPath = gridIconPath;
        this.listIconPath = listIconPath;
        this.path = path;
    }

    public PosterTemplate() {
    }

    public static int findSelectedIndex(List<PosterTemplate> templates, String path) {
        for (int i = 0, j = templates.size(); i < j; i++) {
            if (TextUtils.equals(templates.get(i).path, path)) {
                return i;
            }
        }
        return -1;
    }

    public static List<PosterTemplate> getPosterTemplates(Context context) {
        List<PosterTemplate> templates = new ArrayList<>();
        File templatesDir = FileUtils.getTemplatesDir(context);
        File[] dirFiles = templatesDir.listFiles();
        PosterTemplate posterTemplate;
        for (File f : dirFiles) {
            if (!f.getName().startsWith(FileUtils.TEMPLATE_PREFIX)) {
                continue;
            }
            File[] tempFiles = f.listFiles();
            posterTemplate = new PosterTemplate();
            for (File tempFile : tempFiles) {
                String path = tempFile.getAbsolutePath();
                if (path.contains(GRID_ITEM)) {
                    posterTemplate.gridIconPath = path;
                } else if (path.contains(LIST_ITEM)) {
                    posterTemplate.listIconPath = path;
                } else {
                    posterTemplate.path = path;
                }
            }
            templates.add(posterTemplate);
        }
        Collections.sort(templates);
        return templates;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGridIconPath() {
        return gridIconPath;
    }

    public void setGridIconPath(String gridIconPath) {
        this.gridIconPath = gridIconPath;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getListIconPath() {
        return listIconPath;
    }

    public void setListIconPath(String listIconPath) {
        this.listIconPath = listIconPath;
    }

    @Override
    public String toString() {
        return "PosterTemplate{" +
                "gridIconPath=" + gridIconPath +
                ", listIconPath=" + listIconPath +
                ", path='" + path + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    @Override
    public int compareTo(@NonNull PosterTemplate o) {
        return this.path.compareTo(o.path);
    }
}
