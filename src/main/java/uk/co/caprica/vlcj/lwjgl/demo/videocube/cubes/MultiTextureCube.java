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

import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDrawArrays;

public class MultiTextureCube extends BaseCube {
    private final int[] textureIds;

    public MultiTextureCube(int[] textureIds) {
        super();
        this.textureIds = textureIds;
    }

    public void onRender() {
        // Front face
        glBindTexture(GL_TEXTURE_2D, textureIds[0]);
        glDrawArrays(GL_QUADS, 0, 4);  // Front face vertices

        // Back face
        glBindTexture(GL_TEXTURE_2D, textureIds[1]);
        glDrawArrays(GL_QUADS, 4, 4);  // Back face vertices

        // Left face
        glBindTexture(GL_TEXTURE_2D, textureIds[2]);
        glDrawArrays(GL_QUADS, 8, 4);  // Left face vertices

        // Right face
        glBindTexture(GL_TEXTURE_2D, textureIds[3]);
        glDrawArrays(GL_QUADS, 12, 4);  // Right face vertices

        // Top face
        glBindTexture(GL_TEXTURE_2D, textureIds[4]);
        glDrawArrays(GL_QUADS, 16, 4);  // Top face vertices

        // Bottom face
        glBindTexture(GL_TEXTURE_2D, textureIds[5]);
        glDrawArrays(GL_QUADS, 20, 4);  // Bottom face vertices
    }
}
