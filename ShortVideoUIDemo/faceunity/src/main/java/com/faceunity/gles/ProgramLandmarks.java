/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.faceunity.gles;

import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.faceunity.gles.core.Drawable2d;
import com.faceunity.gles.core.GlUtil;
import com.faceunity.gles.core.Program;

import java.util.Arrays;

public class ProgramLandmarks extends Program {

    private static final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "uniform float uPointSize;" +
                    "void main() {" +
                    // the matrix must be included as a modifier of gl_Position
                    // Note that the uMVPMatrix factor *must be first* in order
                    // for the matrix multiplication product to be correct.
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "  gl_PointSize = uPointSize;" +
                    "}";

    private static final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    private static final float color[] = {0.63671875f, 0.76953125f, 0.22265625f, 1.0f};

    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;
    private int mPointSizeHandle;

    private float mPointSize = 6.0f;

    public ProgramLandmarks() {
        super(vertexShaderCode, fragmentShaderCode);
    }

    @Override
    protected Drawable2d getDrawable2d() {
        return new Drawable2dLandmarks();
    }

    @Override
    protected void getLocations() {
        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "vPosition");
        GlUtil.checkGlError("vPosition");
        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgramHandle, "vColor");
        GlUtil.checkGlError("vColor");
        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "uMVPMatrix");
        GlUtil.checkGlError("glGetUniformLocation");
        mPointSizeHandle = GLES20.glGetUniformLocation(mProgramHandle, "uPointSize");
        GlUtil.checkGlError("uPointSize");
    }

    @Override
    public void drawFrame(int textureId, float[] texMatrix, float[] mvpMatrix) {
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgramHandle);

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(
                mPositionHandle, Drawable2d.COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                Drawable2d.VERTEXTURE_STRIDE, mDrawable2d.vertexArray());

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMtx, 0);

        GLES20.glUniform1f(mPointSizeHandle, mPointSize);

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, mDrawable2d.vertexCount());

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    public void drawFrame(int x, int y, int width, int height) {
        drawFrame(0, null, null, x, y, width, height);
    }

    private final float[] mvpMtx = new float[16];
    private int mCameraType;
    private int mOrientation;
    private int mWidth;
    private int mHeight;

    public void refresh(float[] landmarksData, int width, int height, int orientation, int cameraType) {
        if (mWidth != width || mHeight != height || mOrientation != orientation || mCameraType != cameraType) {
            float[] orthoMtx = new float[16];
            float[] rotateMtx = new float[16];
            Matrix.orthoM(orthoMtx, 0, 0, width, 0, height, -1, 1);
            Matrix.setRotateM(rotateMtx, 0, 360 - orientation, 0.0f, 0.0f, 1.0f);
            if (cameraType == Camera.CameraInfo.CAMERA_FACING_BACK) {
                Matrix.rotateM(rotateMtx, 0, 180, 1.0f, 0.0f, 0.0f);
            }
            Matrix.multiplyMM(mvpMtx, 0, rotateMtx, 0, orthoMtx, 0);

            mWidth = width;
            mHeight = height;
            mOrientation = orientation;
            mCameraType = cameraType;
        }

        updateVertexArray(Arrays.copyOf(landmarksData, landmarksData.length));
    }
}
