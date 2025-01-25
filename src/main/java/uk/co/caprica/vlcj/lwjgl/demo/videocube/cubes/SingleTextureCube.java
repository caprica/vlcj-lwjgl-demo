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

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDrawElements;

/**
 * A cube that renders the same texture on all faces.
 *
 * This is not used by the demo application, but is kept here as a how-to for future reference.
 */
public class SingleTextureCube extends BaseCube {
    private final int textureId;

    protected final IntBuffer indexBuffer;

    public SingleTextureCube(int textureId) {
        this.textureId = textureId;
        int[] indices = generateIndices();
        this.indexBuffer = BufferUtils.createIntBuffer(indices.length).put(indices).flip();
    }

    private int[] generateIndices() {
        return new int[]{
            // Front face
            0, 1, 2, 0, 2, 3,
            // Back face
            4, 5, 6, 4, 6, 7,
            // Top face
            8, 9, 10, 8, 10, 11,
            // Bottom face
            12, 13, 14, 12, 14, 15,
            // Left face
            16, 17, 18, 16, 18, 19,
            // Right face
            20, 21, 22, 20, 22, 23};
    }

    public void onRender() {
        // Draw the cube using the index buffer
        glBindTexture(GL_TEXTURE_2D, textureId);
        glDrawElements(GL_TRIANGLES, indexBuffer);
    }
}
