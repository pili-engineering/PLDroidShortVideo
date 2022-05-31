package com.faceunity.gles.core;

import android.content.Context;
import android.opengl.GLES20;

/**
 * Created by tujh on 2018/1/24.
 */

public abstract class Program {
    private static final String TAG = GlUtil.TAG;

    // Handles to the GL program and various components of it.
    protected int mProgramHandle;


    protected Drawable2d mDrawable2d;

    /**
     * Prepares the program in the current EGL context.
     */
    public Program(String VERTEX_SHADER, String FRAGMENT_SHADER_2D) {
        mProgramHandle = GlUtil.createProgram(VERTEX_SHADER, FRAGMENT_SHADER_2D);
        mDrawable2d = getDrawable2d();
        getLocations();
    }

    public Program(Context context, int vertexShaderResourceId, int fragmentShaderResourceId) {
        this(Extensions.readTextFileFromResource(context, vertexShaderResourceId), Extensions.readTextFileFromResource(context, fragmentShaderResourceId));
    }

    public void updateVertexArray(float[] FULL_RECTANGLE_COORDS) {
        mDrawable2d.updateVertexArray(FULL_RECTANGLE_COORDS);
    }

    public void updateTexCoordArray(float[] FULL_RECTANGLE_TEX_COORDS) {
        mDrawable2d.updateTexCoordArray(FULL_RECTANGLE_TEX_COORDS);
    }

    protected abstract Drawable2d getDrawable2d();

    /**
     * get locations of attributes and uniforms
     */
    protected abstract void getLocations();

    /**
     * Issues the draw call.  Does the full setup on every call.
     */
    public abstract void drawFrame(int textureId, float[] texMatrix, float[] mvpMatrix);

    public void drawFrame(int textureId, float[] texMatrix) {
        drawFrame(textureId, texMatrix, GlUtil.IDENTITY_MATRIX);
    }

    public void drawFrame(int textureId, float[] texMatrix, float[] mvpMatrix, int x, int y, int width, int height) {
        int[] originalViewport = new int[4];
        GLES20.glGetIntegerv(GLES20.GL_VIEWPORT, originalViewport, 0);
        GLES20.glViewport(x, y, width, height);
        drawFrame(textureId, texMatrix, mvpMatrix);
        GLES20.glViewport(originalViewport[0], originalViewport[1], originalViewport[2], originalViewport[3]);
    }

    /**
     * Releases the program.
     * <p>
     * The appropriate EGL context must be current (i.e. the one that was used to create
     * the program).
     */
    public void release() {
        GLES20.glDeleteProgram(mProgramHandle);
        mProgramHandle = -1;
    }
}
