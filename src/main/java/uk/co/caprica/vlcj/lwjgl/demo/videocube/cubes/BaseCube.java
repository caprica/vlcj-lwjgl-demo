/*
 * This file is part of VLCJ.
 *
 * VLCJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VLCJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with VLCJ.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2009-2025 Caprica Software Limited.
 */

package uk.co.caprica.vlcj.lwjgl.demo.videocube.cubes;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_NORMAL_ARRAY;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_COORD_ARRAY;
import static org.lwjgl.opengl.GL11.GL_VERTEX_ARRAY;
import static org.lwjgl.opengl.GL11.glDisableClientState;
import static org.lwjgl.opengl.GL11.glEnableClientState;
import static org.lwjgl.opengl.GL11.glNormalPointer;
import static org.lwjgl.opengl.GL11.glTexCoordPointer;
import static org.lwjgl.opengl.GL11.glVertexPointer;

/**
 * Base implementation of a cube.
 * <p>
 * Sets up all the required vertices, texture coordinates and normals, and then sets up the context for rendering
 * (which is delegated to concrete subclasses).
 */
public abstract class BaseCube {
    protected final FloatBuffer vertexBuffer;
    protected final FloatBuffer texCoordBuffer;
    protected final FloatBuffer normalBuffer;

    public BaseCube() {
        float[] vertices = generateVertices();
        float[] texCoords = generateTexCoords();
        float[] normals = generateNormals();
        this.vertexBuffer = BufferUtils.createFloatBuffer(vertices.length).put(vertices).flip();
        this.texCoordBuffer = BufferUtils.createFloatBuffer(texCoords.length).put(texCoords).flip();
        this.normalBuffer = BufferUtils.createFloatBuffer(normals.length).put(normals).flip();
    }

    private float[] generateVertices() {
        return new float[] {
            // Top face (y = 1.0f)
            0.5f, 0.5f, -0.5f,
            -0.5f, 0.5f, -0.5f,
            -0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,

            // Bottom face (y = -1.0f)
            0.5f, -0.5f, 0.5f,
            -0.5f, -0.5f, 0.5f,
            -0.5f, -0.5f, -0.5f,
            0.5f, -0.5f, -0.5f,

            // Front face  (z = 1.0f)
            0.5f, 0.5f, 0.5f,
            -0.5f, 0.5f, 0.5f,
            -0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, 0.5f,

            // Back face (z = -1.0f)
            0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f,
            -0.5f, 0.5f, -0.5f,
            0.5f, 0.5f, -0.5f,

            // Left face (x = -1.0f)
            -0.5f, 0.5f, 0.5f,
            -0.5f, 0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f, 0.5f,

            // Right face (x = 1.0f)
            0.5f, 0.5f, -0.5f,
            0.5f, 0.5f, 0.5f,
            0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, -0.5f,
        };
    }

    private float[] generateTexCoords() {
        return new float[]{
            // Front face
            1.0f, 0.0f,  // Bottom-right corner
            0.0f, 0.0f,  // Bottom-left corner
            0.0f, 1.0f,  // Top-left corner
            1.0f, 1.0f,  // Top-right corner

            // Back face
            1.0f, 0.0f,  // Bottom-right corner
            0.0f, 0.0f,  // Bottom-left corner
            0.0f, 1.0f,  // Top-left corner
            1.0f, 1.0f,  // Top-right corner

            // Top face
            0.0f, 1.0f,  // Bottom-left corner
            1.0f, 1.0f,  // Bottom-right corner
            1.0f, 0.0f,  // Top-right corner
            0.0f, 0.0f,  // Top-left corner

            // Bottom face
            0.0f, 0.0f,  // Bottom-left corner
            1.0f, 0.0f,  // Bottom-right corner
            1.0f, 1.0f,  // Top-right corner
            0.0f, 1.0f,  // Top-left corner

            // Left face
            0.0f, 0.0f,  // Bottom-left corner
            1.0f, 0.0f,  // Bottom-right corner
            1.0f, 1.0f,  // Top-right corner
            0.0f, 1.0f,  // Top-left corner

            // Right face
            0.0f, 0.0f,  // Bottom-left corner
            1.0f, 0.0f,  // Bottom-right corner
            1.0f, 1.0f,  // Top-right corner
            0.0f, 1.0f   // Top-left corner
        };
    }

    private float[] generateNormals() {
        return new float[]{
            // Front face normals (0, 0, 1)
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,

            // Back face normals (0, 0, -1)
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,

            // Right face normals (1, 0, 0)
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,

            // Left face normals (-1, 0, 0)
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,

            // Top face normals (0, 1, 0)
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,

            // Bottom face normals (0, -1, 0)
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f
        };
    }

    public void render() {
        GL11.glEnable(GL_TEXTURE_2D);

        // Enable client states for vertex, normal, and texture coordinate arrays
        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_NORMAL_ARRAY);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);

        // Specify the pointers to the buffers
        glVertexPointer(3, GL_FLOAT, 0, vertexBuffer);
        glTexCoordPointer(2, GL_FLOAT, 0, texCoordBuffer);
        glNormalPointer(GL_FLOAT, 0, normalBuffer);

        onRender();

        // Disable client states after drawing
        glDisableClientState(GL_VERTEX_ARRAY);
        glDisableClientState(GL_NORMAL_ARRAY);
        glDisableClientState(GL_TEXTURE_COORD_ARRAY);

        GL11.glDisable(GL_TEXTURE_2D);
    }

    protected abstract void onRender();
}
