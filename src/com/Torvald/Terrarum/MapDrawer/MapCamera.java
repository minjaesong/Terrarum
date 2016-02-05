package com.Torvald.Terrarum.MapDrawer;

import com.Torvald.Terrarum.Actors.Player;
import com.Torvald.Terrarum.Terrarum;
import com.Torvald.Terrarum.Game;
import com.Torvald.Terrarum.GameMap.GameMap;
import com.Torvald.Terrarum.GameMap.MapLayer;
import com.jme3.math.FastMath;
import org.newdawn.slick.*;

/**
 * Created by minjaesong on 16-01-19.
 */
public class MapCamera {

    private static GameMap map;
    private static Player player;

    private static int cameraX = 0;
    private static int cameraY = 0;

    private static SpriteSheet tilesWall;
    private static SpriteSheet tilesTerrain;
    private static SpriteSheet tilesWire;

    private static int TSIZE = MapDrawer.TILE_SIZE;


    private static SpriteSheet[] tilesetBook;

    private static final int WALL = 0;
    private static final int TERRAIN = 1;
    private static final int WIRE = 2;

    private static int renderWidth;
    private static int renderHeight;

    private static final int TILE_AIR = 0;

    private static final int NEARBY_TILE_KEY_UP = 0;
    private static final int NEARBY_TILE_KEY_RIGHT = 1;
    private static final int NEARBY_TILE_KEY_DOWN = 2;
    private static final int NEARBY_TILE_KEY_LEFT = 3;

    private static final int NEARBY_TILE_CODE_UP = 0b0001;
    private static final int NEARBY_TILE_CODE_RIGHT = 0b0010;
    private static final int NEARBY_TILE_CODE_DOWN = 0b0100;
    private static final int NEARBY_TILE_CODE_LEFT = 0b1000;

    /**
     * @param map
     * @param tileSize
     */
    public MapCamera(GameMap map, int tileSize) throws SlickException {
        this.map = map;
        player = Game.getPlayer();

        tilesWall = new SpriteSheet("./res/graphics/terrain/wall.png"
                , TSIZE
                , TSIZE
        );

        tilesTerrain = new SpriteSheet("./res/graphics/terrain/terrainplusplus.png"
                , TSIZE
                , TSIZE
        );

        tilesWire = new SpriteSheet("./res/graphics/terrain/wire.png"
                , TSIZE
                , TSIZE
        );

        tilesetBook = new SpriteSheet[9];
        tilesetBook[WALL] = tilesWall;
        tilesetBook[TERRAIN] = tilesTerrain;
        tilesetBook[WIRE] = tilesWire;
    }

    public static void update(GameContainer gc, int delta_t) {
        renderWidth = FastMath.ceil(Terrarum.WIDTH / Game.screenZoom);
        renderHeight = FastMath.ceil(Terrarum.HEIGHT / Game.screenZoom);

        // position - (WH / 2)
        cameraX = clamp(
                Math.round(player.pointedPosX() - (renderWidth / 2))
                , map.width * TSIZE - renderWidth
        );
        cameraY = clamp(
                Math.round(player.pointedPosY() - (renderHeight / 2))
                , map.height * TSIZE - renderHeight
        );
    }

    public static void render(GameContainer gc, Graphics g) {
        /**
         * render to camera
         */
        drawTiles(WALL);
        drawTiles(TERRAIN);
    }

    private static void drawTiles(int mode) {
        int for_y_start = div16(cameraY);
        int for_x_start = div16(cameraX);

        int for_y_end = clampHTile(for_y_start + div16(renderHeight) + 2);
        int for_x_end = clampWTile(for_x_start + div16(renderWidth) + 2);

        MapLayer currentLayer = (mode % 3 == WALL) ? map.getLayerWall()
                                               : (mode % 3 == TERRAIN) ? map.getLayerTerrain()
                                                                   : map.getLayerWire();

        // initialise
        tilesetBook[mode].startUse();

        // loop
        for (int y = for_y_start; y < for_y_end; y++) {
            for (int x = for_x_start; x < for_x_end; x++) {

                int thisTile = currentLayer.getTile(x, y);
                int thisTerrainTile = map.getTileFromTerrain(x, y);

                // draw
                if (

                        (
                                (       // wall and not blocked
                                        (mode == WALL) && (!isOpaque(thisTerrainTile))
                                )
                                        ||
                                        (mode == TERRAIN)
                        )       // not an air tile
                                && (thisTile > 0)
                                &&
                                // check if light level of upper tile is zero and
                                // that of this tile is also zero
                                (((y > 0)
                                        && !((LightmapRenderer.getValueFromMap(x, y) == 0)
                                                && (LightmapRenderer.getValueFromMap(x, y - 1) == 0))
                                )
                                ||
                                        // check if light level of this tile is zero, for y = 0
                                ((y == 0)
                                        && (LightmapRenderer.getValueFromMap(x, y) > 0)
                                        ))
                        ) {

                    if (mode == TERRAIN) {
                        int nearbyTilesInfo = getNearbyTilesInfo(x, y, TILE_AIR);

                        int thisTileX = nearbyTilesInfo;
                        int thisTileY = thisTile;

                        drawTile(TERRAIN, x, y, thisTileX, thisTileY);
                    }
                    else {
                        drawTile(mode, x, y, mod16(thisTile), div16(thisTile));
                    }
                }

            }
        }

        tilesetBook[mode].endUse();
    }

    /**
     *
     * @param x
     * @param y
     * @return [0-15] 1: up, 2: right, 4: down, 8: left
     */
    private static int getNearbyTilesInfo(int x, int y, int mark) {
        int[] nearbyTiles = new int[4];
        if (x == 0) { nearbyTiles[NEARBY_TILE_KEY_LEFT] = 0; }
        if (x == map.width - 1) { nearbyTiles[NEARBY_TILE_KEY_RIGHT] = 0; }
        if (y == 0) { nearbyTiles[NEARBY_TILE_KEY_UP] = 0; }
        if (y == map.height - 1) { nearbyTiles[NEARBY_TILE_KEY_DOWN] = 0; }
        try {
            nearbyTiles[NEARBY_TILE_KEY_UP] = map.getTileFromTerrain(x, y - 1);
            nearbyTiles[NEARBY_TILE_KEY_DOWN] = map.getTileFromTerrain(x, y + 1);
            nearbyTiles[NEARBY_TILE_KEY_LEFT] = map.getTileFromTerrain(x - 1, y);
            nearbyTiles[NEARBY_TILE_KEY_RIGHT] = map.getTileFromTerrain(x + 1, y);
        }
        catch (ArrayIndexOutOfBoundsException e) { }

        int ret = 0;
        for (int i = 0; i < 4; i++) {
            if (nearbyTiles[i] == mark) {
                ret += (1 << i); // write 1, 2, 4, 8 for i = 0, 1, 2, 3
            }
        }

        return ret;

    }

    private static void drawTile(int mode, int tilewisePosX, int tilewisePosY, int sheetX, int sheetY) {
        if (Game.screenZoom == 1) {
            tilesetBook[mode].renderInUse(
                    FastMath.floor(tilewisePosX * TSIZE)
                    , FastMath.floor(tilewisePosY * TSIZE)
                    , sheetX
                    , sheetY
            );
        }
        else {
            tilesetBook[mode].getSprite(
                    sheetX
                    , sheetY
            ).drawEmbedded(
                    Math.round(tilewisePosX * TSIZE * Game.screenZoom)
                    , Math.round(tilewisePosY * TSIZE * Game.screenZoom)
                    , FastMath.ceil(TSIZE * Game.screenZoom)
                    , FastMath.ceil(TSIZE * Game.screenZoom)
            );
        }
    }

    private static int div16(int x) {
        return (x & 0x7FFF_FFFF) >> 4;
    }

    private static int mod16(int x) {
        return x & 0b1111;
    }

    private static int quantise16(int x) {
        return (x & 0xFFFF_FFF0);
    }

    private static int clampW(int x) {
        if (x < 0) {
            return 0;
        }
        else if (x > map.width * TSIZE) {
            return map.width * TSIZE;
        }
        else {
            return x;
        }
    }

    private static int clampH(int x) {
        if (x < 0) {
            return 0;
        }
        else if (x > map.height * TSIZE) {
            return map.height * TSIZE;
        }
        else {
            return x;
        }
    }

    private static int clampWTile(int x) {
        if (x < 0) {
            return 0;
        }
        else if (x > map.width) {
            return map.width;
        }
        else {
            return x;
        }
    }

    private static int clampHTile(int x) {
        if (x < 0) {
            return 0;
        }
        else if (x > map.height) {
            return map.height;
        }
        else {
            return x;
        }
    }

    private static int clamp(int x, int lim) {
        if (x < 0) {
            return 0;
        }
        else if (x > lim) {
            return lim;
        }
        else {
            return x;
        }
    }

    private static Image getTileByIndex(SpriteSheet s, int i) {
        return s.getSprite(i % 16, i / 16);
    }

    private static boolean isOpaque(int x) {
        return (x >= 1 && x <= 38)
                || (x >= 41 && x <= 44)
                || (x >= 46 && x <= 47)
                || (x >= 64 && x <= 86)
                || (x >= 88 && x <= 116);
    }

    public static int getCameraX() {
        return cameraX;
    }

    public static int getCameraY() {
        return cameraY;
    }

    public static int getRenderWidth() {
        return renderWidth;
    }

    public static int getRenderHeight() {
        return renderHeight;
    }

    public static int getRenderStartX() {
        return div16(cameraX);
    }

    public static int getRenderStartY() {
        return div16(cameraY);
    }

    public static int getRenderEndX() {
        return clampWTile(getRenderStartX() + div16(renderWidth) + 2);
    }

    public static int getRenderEndY() {
        return clampHTile(getRenderStartY() + div16(renderHeight) + 2);
    }
}
