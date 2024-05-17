package com.qiniu.pili.droid.shortvideo.demo.utils;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class TextureReader {
    private static final String TAG = "TextureReader";
    public static final float[] IDENTITY_MATRIX;

    static {
        IDENTITY_MATRIX = new float[16];
        Matrix.setIdentityM(IDENTITY_MATRIX, 0);
    }

    private static final String TEXTURE_VS =
            "attribute vec2 a_pos;\n" +
                    "attribute vec2 a_tex;\n" +
                    "varying vec2 v_tex_coord;\n" +
                    "uniform mat4 u_mvp;\n" +
                    "uniform mat4 u_tex_trans;\n" +
                    "void main() {\n" +
                    "   gl_Position = u_mvp * vec4(a_pos, 0.0, 1.0);\n" +
                    "   v_tex_coord = (u_tex_trans * vec4(a_tex, 0.0, 1.0)).st;\n" +
                    "}\n";

    private static final String TEXTURE_2D_FS =
            "precision mediump float;\n" +
                    "uniform sampler2D u_tex;\n" +
                    "varying vec2 v_tex_coord;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = texture2D(u_tex, v_tex_coord);\n" +
                    "}\n";

    private static final String TEXTURE_EXTERNAL_FS =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "uniform samplerExternalOES u_tex;\n" +
                    "varying vec2 v_tex_coord;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = texture2D(u_tex, v_tex_coord);\n" +
                    "}\n";

    private int mProgram;
    private int mVboVertices;
    private int mVboTexCoords;
    private int mVerticesLoc;
    private int mTexCoordsLoc;
    private int mMVPMatrixLoc;
    private int mTexTransMatrixLoc;
    private static final float[] CUBE = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f,
    };
    private static final float[] TEXTURE_COORDINATE = {
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
    };
    private static final float[] FLIP_COORDINATE = {
            0.0F, 1.0F,
            1.0F, 1.0F,
            0.0F, 0.0F,
            1.0F, 0.0F
    };
    private float[] mVertexPosition = CUBE;
    private float[] mTextureCoordinate = FLIP_COORDINATE;

    private boolean mTransformOES;

    protected int mFbo;
    protected int mOutTex;

    private int mWidth = 0;
    private int mHeight = 0;
    private ByteBuffer mReadBuffer = null;

    public TextureReader(int width, int height, boolean transformOES) {
        mWidth = width;
        mHeight = height;
        mTransformOES = transformOES;
        setupShaders();
        setupLocations();
        setupBuffers();
        setupFBO();
    }

    public ByteBuffer read(int texId) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFbo);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, mOutTex, 0);

        mReadBuffer.position(0);

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        GLES20.glUseProgram(mProgram);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(getTextureTarget(), texId);

        setupVBO();

        GLES20.glUniformMatrix4fv(mMVPMatrixLoc, 1, false, IDENTITY_MATRIX, 0);
        GLES20.glUniformMatrix4fv(mTexTransMatrixLoc, 1, false, IDENTITY_MATRIX, 0);
        GLES20.glViewport(0, 0, mWidth, mHeight);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glReadPixels(0, 0, mWidth, mHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, mReadBuffer);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        GLES20.glBindTexture(getTextureTarget(), 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        return mReadBuffer;
    }

    public void release() {
        deleteProgram();
        deleteVBO();
        deleteFBO();
    }

    private void deleteProgram() {
        if (mProgram != 0) {
            GLES20.glDeleteProgram(mProgram);
            mProgram = 0;
        }
    }

    private void deleteVBO() {
        if (mVboVertices != 0) {
            GLES20.glDeleteBuffers(1, new int[]{mVboVertices}, 0);
            mVboVertices = 0;
        }
        if (mVboTexCoords != 0) {
            GLES20.glDeleteBuffers(1, new int[]{mVboTexCoords}, 0);
            mVboTexCoords = 0;
        }
    }

    private void deleteFBO() {
        if (mFbo != 0) {
            GLES20.glDeleteFramebuffers(1, new int[]{mFbo}, 0);
            mFbo = 0;
        }
        if (mOutTex != 0) {
            GLES20.glDeleteTextures(1, new int[]{mOutTex}, 0);
            mOutTex = 0;
        }
    }

    private void setupFBO() {
        int[] fbo = new int[1];
        GLES20.glGenFramebuffers(1, fbo, 0);
        mFbo = fbo[0];
        mOutTex = GLUtils.createImageTexture(null, mWidth, mHeight, GLES20.GL_RGBA);
        mReadBuffer = ByteBuffer.allocateDirect(mWidth * mHeight * 4);
    }

    private void setupBuffers() {
        float[] vertexPosition = mVertexPosition;

        ByteBuffer bb = ByteBuffer.allocateDirect(4 * vertexPosition.length);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer mVertices = bb.asFloatBuffer();
        mVertices.put(vertexPosition);
        mVertices.rewind();

        float[] textureCoordinate = mTextureCoordinate;

        bb = ByteBuffer.allocateDirect(4 * textureCoordinate.length);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer mTexCoords = bb.asFloatBuffer();
        mTexCoords.put(textureCoordinate);
        mTexCoords.rewind();

        // upload data to vbo
        int[] bufs = new int[2];
        GLES20.glGenBuffers(2, bufs, 0);
        mVboVertices = bufs[0];
        mVboTexCoords = bufs[1];

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVboVertices);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, 8 * 4, mVertices, GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVboTexCoords);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, 8 * 4, mTexCoords, GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        // setup vbo
        setupVBO();

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    private void setupVBO() {
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVboVertices);
        GLES20.glEnableVertexAttribArray(mVerticesLoc);
        GLES20.glVertexAttribPointer(mVerticesLoc, 2, GLES20.GL_FLOAT, false, 0, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVboTexCoords);
        GLES20.glEnableVertexAttribArray(mTexCoordsLoc);
        GLES20.glVertexAttribPointer(mTexCoordsLoc, 2, GLES20.GL_FLOAT, false, 0, 0);
    }

    private boolean setupShaders() {
        String[] sources = getShaderSources();
        mProgram = loadProgram(sources[0], sources[1]);
        return mProgram != 0;
    }

    private void setupLocations() {
        mVerticesLoc = GLES20.glGetAttribLocation(mProgram, "a_pos");
        mTexCoordsLoc = GLES20.glGetAttribLocation(mProgram, "a_tex");
        mMVPMatrixLoc = GLES20.glGetUniformLocation(mProgram, "u_mvp");
        mTexTransMatrixLoc = GLES20.glGetUniformLocation(mProgram, "u_tex_trans");
    }

    private int getTextureTarget() {
        if (mTransformOES) {
            return GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
        } else {
            return GLES20.GL_TEXTURE_2D;
        }
    }

    private String[] getShaderSources() {
        if (mTransformOES) {
            return new String[]{TEXTURE_VS, TEXTURE_EXTERNAL_FS};
        } else {
            return new String[]{TEXTURE_VS, TEXTURE_2D_FS};
        }
    }

    private int loadProgram(final String strVSource, final String strFSource) {
        int iVShader;
        int iFShader;
        int iProgId;
        int[] link = new int[1];
        iVShader = loadShader(strVSource, GLES20.GL_VERTEX_SHADER);
        if (iVShader == 0) {
            Log.e(TAG, "Load Program : Vertex Shader Failed");
            return 0;
        }
        iFShader = loadShader(strFSource, GLES20.GL_FRAGMENT_SHADER);
        if (iFShader == 0) {
            Log.e(TAG, "Load Program : Fragment Shader Failed");
            return 0;
        }

        iProgId = GLES20.glCreateProgram();

        GLES20.glAttachShader(iProgId, iVShader);
        GLES20.glAttachShader(iProgId, iFShader);

        GLES20.glLinkProgram(iProgId);

        GLES20.glGetProgramiv(iProgId, GLES20.GL_LINK_STATUS, link, 0);
        if (link[0] <= 0) {
            Log.e(TAG, "Load Program : Linking Failed");
            return 0;
        }
        GLES20.glDeleteShader(iVShader);
        GLES20.glDeleteShader(iFShader);
        return iProgId;
    }

    private int loadShader(final String strSource, final int iType) {
        int[] compiled = new int[1];
        int iShader = GLES20.glCreateShader(iType);
        GLES20.glShaderSource(iShader, strSource);
        GLES20.glCompileShader(iShader);
        GLES20.glGetShaderiv(iShader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(TAG, "Load Shader Failed : Compilation\n" + GLES20.glGetShaderInfoLog(iShader));
            return 0;
        }
        return iShader;
    }
}
