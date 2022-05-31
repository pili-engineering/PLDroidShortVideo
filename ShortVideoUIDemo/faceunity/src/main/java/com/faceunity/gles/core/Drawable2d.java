/*
 * Copyright 2014 Google Inc. All rights reserved.
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

package com.faceunity.gles.core;

import java.nio.FloatBuffer;

/**
 * Base class for stuff we like to draw.
 */
public class Drawable2d {

    public static final int SIZEOF_FLOAT = 4;
    public static final int COORDS_PER_VERTEX = 2;
    public static final int TEXTURE_COORD_STRIDE = 2 * SIZEOF_FLOAT;
    public static final int VERTEXTURE_STRIDE = COORDS_PER_VERTEX * SIZEOF_FLOAT;

    private FloatBuffer mTexCoordArray;
    private FloatBuffer mVertexArray;
    private int mVertexCount;

    public Drawable2d() {
    }

    /**
     * Prepares a drawable from a "pre-fabricated" shape definition.
     * <p>
     * Does no EGL/GL operations, so this can be done at any time.
     */
    public Drawable2d(float[] FULL_RECTANGLE_COORDS, float[] FULL_RECTANGLE_TEX_COORDS) {
        updateVertexArray(FULL_RECTANGLE_COORDS);
        updateTexCoordArray(FULL_RECTANGLE_TEX_COORDS);
    }

    public void updateVertexArray(float[] FULL_RECTANGLE_COORDS) {
        mVertexArray = GlUtil.createFloatBuffer(FULL_RECTANGLE_COORDS);
        mVertexCount = FULL_RECTANGLE_COORDS.length / COORDS_PER_VERTEX;
    }

    public void updateTexCoordArray(float[] FULL_RECTANGLE_TEX_COORDS) {
        mTexCoordArray = GlUtil.createFloatBuffer(FULL_RECTANGLE_TEX_COORDS);
    }

    /**
     * Returns the array of vertices.
     * <p>
     * To avoid allocations, this returns internal state.  The caller must not modify it.
     */
    public FloatBuffer vertexArray() {
        return mVertexArray;
    }

    /**
     * Returns the array of texture coordinates.
     * <p>
     * To avoid allocations, this returns internal state.  The caller must not modify it.
     */
    public FloatBuffer texCoordArray() {
        return mTexCoordArray;
    }

    /**
     * Returns the number of vertices stored in the vertex array.
     */
    public int vertexCount() {
        return mVertexCount;
    }

}
