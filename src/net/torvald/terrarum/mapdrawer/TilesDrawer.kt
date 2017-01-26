package net.torvald.terrarum.mapdrawer

import net.torvald.terrarum.gameworld.GameWorld
import net.torvald.terrarum.gameworld.PairedMapLayer
import net.torvald.terrarum.Terrarum
import net.torvald.terrarum.tileproperties.Tile
import net.torvald.terrarum.tileproperties.TileCodex
import com.jme3.math.FastMath
import net.torvald.terrarum.blendAlphaMap
import net.torvald.terrarum.concurrent.ThreadParallel
import net.torvald.terrarum.blendMul
import net.torvald.terrarum.blendNormal
import net.torvald.terrarum.mapdrawer.FeaturesDrawer.TILE_SIZE
import net.torvald.terrarum.mapdrawer.LightmapRenderer.normaliseToColour
import net.torvald.terrarum.mapdrawer.MapCamera.x
import net.torvald.terrarum.mapdrawer.MapCamera.y
import net.torvald.terrarum.mapdrawer.MapCamera.height
import net.torvald.terrarum.mapdrawer.MapCamera.width
import org.lwjgl.opengl.GL11
import org.newdawn.slick.*
import java.util.*

/**
 * Created by minjaesong on 16-01-19.
 */
object TilesDrawer {
    private val world: GameWorld = Terrarum.ingame.world
    private val TILE_SIZE = FeaturesDrawer.TILE_SIZE
    private val TILE_SIZEF = FeaturesDrawer.TILE_SIZE.toFloat()

    var tilesTerrain: SpriteSheet = SpriteSheet("./assets/graphics/terrain/terrain.tga", TILE_SIZE, TILE_SIZE)
        private set // Slick has some weird quirks with PNG's transparency. I'm using 32-bit targa here.
    var tilesWire: SpriteSheet = SpriteSheet("./assets/graphics/terrain/wire.tga", TILE_SIZE, TILE_SIZE)
        private set

    val WALL = GameWorld.WALL
    val TERRAIN = GameWorld.TERRAIN
    val WIRE = GameWorld.WIRE

    private val NEARBY_TILE_KEY_UP = 0
    private val NEARBY_TILE_KEY_RIGHT = 1
    private val NEARBY_TILE_KEY_DOWN = 2
    private val NEARBY_TILE_KEY_LEFT = 3

    private val NEARBY_TILE_CODE_UP = 1
    private val NEARBY_TILE_CODE_RIGHT = 2
    private val NEARBY_TILE_CODE_DOWN = 4
    private val NEARBY_TILE_CODE_LEFT = 8

    /**
     * Connectivity group 01 : artificial tiles
     * It holds different shading rule to discriminate with group 02, index 0 is single tile.
     * These are the tiles that only connects to itself, will not connect to colour variants
     */
    val TILES_CONNECT_SELF = arrayOf(
            Tile.ICE_MAGICAL,
            Tile.GLASS_CRUDE,
            Tile.GLASS_CLEAN,
            Tile.ILLUMINATOR_BLACK,
            Tile.ILLUMINATOR_BLUE,
            Tile.ILLUMINATOR_BROWN,
            Tile.ILLUMINATOR_CYAN,
            Tile.ILLUMINATOR_FUCHSIA,
            Tile.ILLUMINATOR_GREEN,
            Tile.ILLUMINATOR_GREEN_DARK,
            Tile.ILLUMINATOR_GREY_DARK,
            Tile.ILLUMINATOR_GREY_LIGHT,
            Tile.ILLUMINATOR_GREY_MED,
            Tile.ILLUMINATOR_ORANGE,
            Tile.ILLUMINATOR_PURPLE,
            Tile.ILLUMINATOR_RED,
            Tile.ILLUMINATOR_TAN,
            Tile.ILLUMINATOR_WHITE,
            Tile.ILLUMINATOR_YELLOW,
            Tile.ILLUMINATOR_BLACK_OFF,
            Tile.ILLUMINATOR_BLUE_OFF,
            Tile.ILLUMINATOR_BROWN_OFF,
            Tile.ILLUMINATOR_CYAN_OFF,
            Tile.ILLUMINATOR_FUCHSIA_OFF,
            Tile.ILLUMINATOR_GREEN_OFF,
            Tile.ILLUMINATOR_GREEN_DARK_OFF,
            Tile.ILLUMINATOR_GREY_DARK_OFF,
            Tile.ILLUMINATOR_GREY_LIGHT_OFF,
            Tile.ILLUMINATOR_GREY_MED_OFF,
            Tile.ILLUMINATOR_ORANGE_OFF,
            Tile.ILLUMINATOR_PURPLE_OFF,
            Tile.ILLUMINATOR_RED_OFF,
            Tile.ILLUMINATOR_TAN_OFF,
            Tile.ILLUMINATOR_WHITE_OFF,
            Tile.ILLUMINATOR_YELLOW,
            Tile.SANDSTONE,
            Tile.SANDSTONE_BLACK,
            Tile.SANDSTONE_DESERT,
            Tile.SANDSTONE_RED,
            Tile.SANDSTONE_WHITE,
            Tile.SANDSTONE_GREEN,
            Tile.DAYLIGHT_CAPACITOR
    )

    /**
     * Connectivity group 02 : natural tiles
     * It holds different shading rule to discriminate with group 01, index 0 is middle tile.
     */
    val TILES_CONNECT_MUTUAL = arrayOf(
            Tile.STONE,
            Tile.STONE_QUARRIED,
            Tile.STONE_TILE_WHITE,
            Tile.STONE_BRICKS,
            Tile.DIRT,
            Tile.GRASS,
            Tile.PLANK_BIRCH,
            Tile.PLANK_BLOODROSE,
            Tile.PLANK_EBONY,
            Tile.PLANK_NORMAL,
            Tile.SAND,
            Tile.SAND_WHITE,
            Tile.SAND_RED,
            Tile.SAND_DESERT,
            Tile.SAND_BLACK,
            Tile.SAND_GREEN,
            Tile.GRAVEL,
            Tile.GRAVEL_GREY,
            Tile.SNOW,
            Tile.ICE_NATURAL,
            Tile.ORE_COPPER,
            Tile.ORE_IRON,
            Tile.ORE_GOLD,
            Tile.ORE_SILVER,
            Tile.ORE_ILMENITE,
            Tile.ORE_AURICHALCUM,

            Tile.WATER,
            Tile.WATER_1,
            Tile.WATER_2,
            Tile.WATER_3,
            Tile.WATER_4,
            Tile.WATER_5,
            Tile.WATER_6,
            Tile.WATER_7,
            Tile.WATER_8,
            Tile.WATER_9,
            Tile.WATER_10,
            Tile.WATER_11,
            Tile.WATER_12,
            Tile.WATER_13,
            Tile.WATER_14,
            Tile.WATER_15,
            Tile.LAVA,
            Tile.LAVA_1,
            Tile.LAVA_2,
            Tile.LAVA_3,
            Tile.LAVA_4,
            Tile.LAVA_5,
            Tile.LAVA_6,
            Tile.LAVA_7,
            Tile.LAVA_8,
            Tile.LAVA_9,
            Tile.LAVA_10,
            Tile.LAVA_11,
            Tile.LAVA_12,
            Tile.LAVA_13,
            Tile.LAVA_14,
            Tile.LAVA_15
    )

    /**
     * Torches, levers, switches, ...
     */
    val TILES_WALL_STICKER = arrayOf(
            Tile.TORCH,
            Tile.TORCH_FROST,
            Tile.TORCH_OFF,
            Tile.TORCH_FROST_OFF
    )

    /**
     * platforms, ...
     */
    val TILES_WALL_STICKER_CONNECT_SELF = arrayOf(
            Tile.PLATFORM_BIRCH,
            Tile.PLATFORM_BLOODROSE,
            Tile.PLATFORM_EBONY,
            Tile.PLATFORM_STONE,
            Tile.PLATFORM_WOODEN
    )

    /**
     * Tiles that half-transparent and has hue
     * will blend colour using colour multiplication
     * i.e. red hues get lost if you dive into the water
     */
    val TILES_BLEND_MUL = arrayOf(
            Tile.WATER,
            Tile.WATER_1,
            Tile.WATER_2,
            Tile.WATER_3,
            Tile.WATER_4,
            Tile.WATER_5,
            Tile.WATER_6,
            Tile.WATER_7,
            Tile.WATER_8,
            Tile.WATER_9,
            Tile.WATER_10,
            Tile.WATER_11,
            Tile.WATER_12,
            Tile.WATER_13,
            Tile.WATER_14,
            Tile.WATER_15,
            Tile.LAVA,
            Tile.LAVA_1,
            Tile.LAVA_2,
            Tile.LAVA_3,
            Tile.LAVA_4,
            Tile.LAVA_5,
            Tile.LAVA_6,
            Tile.LAVA_7,
            Tile.LAVA_8,
            Tile.LAVA_9,
            Tile.LAVA_10,
            Tile.LAVA_11,
            Tile.LAVA_12,
            Tile.LAVA_13,
            Tile.LAVA_14,
            Tile.LAVA_15
    )

    fun update() {
        val player = Terrarum.ingame.player
    }

    val wallOverlayColour = Color(2f/3f, 2f/3f, 2f/3f, 1f)

    fun renderWall(g: Graphics) {
        /**
         * render to camera
         */
        blendNormal()

        tilesTerrain.startUse()
        drawTiles(g, WALL, false)
        tilesTerrain.endUse()

        blendMul()

        g.color = wallOverlayColour
        g.fillRect(MapCamera.x.toFloat(), MapCamera.y.toFloat(),
                MapCamera.width.toFloat() + 1, MapCamera.height.toFloat() + 1
        )

        blendNormal()
    }

    fun renderTerrain(g: Graphics) {
        /**
         * render to camera
         */
        blendNormal()

        tilesTerrain.startUse()
        drawTiles(g, TERRAIN, false) // regular tiles
        tilesTerrain.endUse()
    }

    fun renderFront(g: Graphics, drawWires: Boolean) {
        /**
         * render to camera
         */
        blendMul()

        tilesTerrain.startUse()
        drawTiles(g, TERRAIN, true) // blendmul tiles
        tilesTerrain.endUse()

        if (drawWires) {
            tilesWire.startUse()
            drawTiles(g, WIRE, false)
            tilesWire.endUse()
        }

        blendNormal()
    }

    private val tileDrawLightThreshold = 2

    private fun drawTiles(g: Graphics, mode: Int, drawModeTilesBlendMul: Boolean) {
        val for_y_start = y / TILE_SIZE
        val for_y_end = TilesDrawer.clampHTile(for_y_start + (height / TILE_SIZE) + 2)

        val for_x_start = x / TILE_SIZE - 1
        val for_x_end = for_x_start + (width / TILE_SIZE) + 3

        var zeroTileCounter = 0

        // loop
        for (y in for_y_start..for_y_end) {
            for (x in for_x_start..for_x_end - 1) {

                val thisTile: Int?
                if (mode % 3 == WALL)
                    thisTile = world.getTileFromWall(x, y)
                else if (mode % 3 == TERRAIN)
                    thisTile = world.getTileFromTerrain(x, y)
                else if (mode % 3 == WIRE)
                    thisTile = world.getTileFromWire(x, y)
                else
                    throw IllegalArgumentException()

                val noDamageLayer = mode % 3 == WIRE

                // draw
                try {
                    if ((mode == WALL || mode == TERRAIN) &&  // not an air tile
                        (thisTile ?: 0) != Tile.AIR) {
                    // check if light level of nearby or this tile is illuminated
                        if ( LightmapRenderer.getHighestRGB(x, y) ?: 0 >= tileDrawLightThreshold ||
                             LightmapRenderer.getHighestRGB(x - 1, y) ?: 0 >= tileDrawLightThreshold ||
                             LightmapRenderer.getHighestRGB(x + 1, y) ?: 0 >= tileDrawLightThreshold ||
                             LightmapRenderer.getHighestRGB(x, y - 1) ?: 0 >= tileDrawLightThreshold ||
                             LightmapRenderer.getHighestRGB(x, y + 1) ?: 0 >= tileDrawLightThreshold ||
                             LightmapRenderer.getHighestRGB(x - 1, y - 1) ?: 0 >= tileDrawLightThreshold ||
                             LightmapRenderer.getHighestRGB(x + 1, y + 1) ?: 0 >= tileDrawLightThreshold ||
                             LightmapRenderer.getHighestRGB(x + 1, y - 1) ?: 0 >= tileDrawLightThreshold ||
                             LightmapRenderer.getHighestRGB(x - 1, y + 1) ?: 0 >= tileDrawLightThreshold) {
                                // blackness
                                if (zeroTileCounter > 0) {
                                    /* unable to do anything */

                                    zeroTileCounter = 0
                                }


                                val nearbyTilesInfo: Int
                                if (isPlatform(thisTile)) {
                                    nearbyTilesInfo = getNearbyTilesInfoPlatform(x, y)
                                }
                                else if (isWallSticker(thisTile)) {
                                    nearbyTilesInfo = getNearbyTilesInfoWallSticker(x, y)
                                }
                                else if (isConnectMutual(thisTile)) {
                                    nearbyTilesInfo = getNearbyTilesInfoNonSolid(x, y, mode)
                                }
                                else if (isConnectSelf(thisTile)) {
                                    nearbyTilesInfo = getNearbyTilesInfo(x, y, mode, thisTile)
                                }
                                else {
                                    nearbyTilesInfo = 0
                                }


                                val thisTileX: Int
                                if (!noDamageLayer)
                                    thisTileX = PairedMapLayer.RANGE * ((thisTile ?: 0) % PairedMapLayer.RANGE) + nearbyTilesInfo
                                else
                                    thisTileX = nearbyTilesInfo

                                val thisTileY = (thisTile ?: 0) / PairedMapLayer.RANGE

                                if (drawModeTilesBlendMul) {
                                    if (TilesDrawer.isBlendMul(thisTile)) {
                                        drawTile(mode, x, y, thisTileX, thisTileY)
                                    }
                                }
                                else {
                                    // do NOT add "if (!isBlendMul(thisTile))"!
                                    // or else they will not look like they should be when backed with wall
                                    drawTile(mode, x, y, thisTileX, thisTileY)
                                }
                        } // end if (is illuminated)
                        else {
                            zeroTileCounter++
                            //drawTile(mode, x, y, 1, 0) // black patch
                            GL11.glColor4f(0f, 0f, 0f, 1f)

                            GL11.glTexCoord2f(0f, 0f)
                            GL11.glVertex3f(x * TILE_SIZE.toFloat(), y * TILE_SIZE.toFloat(), 0f)
                            GL11.glTexCoord2f(0f, 0f + TILE_SIZE)
                            GL11.glVertex3f(x * TILE_SIZE.toFloat(), (y + 1) * TILE_SIZE.toFloat(), 0f)
                            GL11.glTexCoord2f(0f + TILE_SIZE, 0f + TILE_SIZE)
                            GL11.glVertex3f((x + 1) * TILE_SIZE.toFloat(), (y + 1) * TILE_SIZE.toFloat(), 0f)
                            GL11.glTexCoord2f(0f + TILE_SIZE, 0f)
                            GL11.glVertex3f((x + 1) * TILE_SIZE.toFloat(), y * TILE_SIZE.toFloat(), 0f)

                            GL11.glColor4f(1f, 1f, 1f, 1f)
                        }
                    } // end if (not an air)
                } catch (e: NullPointerException) {
                    // do nothing. WARNING: This exception handling may hide erratic behaviour completely.
                }

            }
        }
    }

    /**

     * @param x
     * *
     * @param y
     * *
     * @return binary [0-15] 1: up, 2: right, 4: down, 8: left
     */
    fun getNearbyTilesInfo(x: Int, y: Int, mode: Int, mark: Int?): Int {
        val nearbyTiles = IntArray(4)
        nearbyTiles[NEARBY_TILE_KEY_LEFT] = world.getTileFrom(mode, x - 1, y) ?: 4096
        nearbyTiles[NEARBY_TILE_KEY_RIGHT] = world.getTileFrom(mode, x + 1, y) ?: 4096
        nearbyTiles[NEARBY_TILE_KEY_UP] = world.getTileFrom(mode, x    , y - 1) ?: 4906
        nearbyTiles[NEARBY_TILE_KEY_DOWN] = world.getTileFrom(mode, x    , y + 1) ?: 4096

        // try for
        var ret = 0
        for (i in 0..3) {
            if (nearbyTiles[i] == mark) {
                ret += 1 shl i // add 1, 2, 4, 8 for i = 0, 1, 2, 3
            }
        }

        return ret
    }

    fun getNearbyTilesInfoNonSolid(x: Int, y: Int, mode: Int): Int {
        val nearbyTiles = IntArray(4)
        nearbyTiles[NEARBY_TILE_KEY_LEFT] = world.getTileFrom(mode, x - 1, y) ?: 4096
        nearbyTiles[NEARBY_TILE_KEY_RIGHT] = world.getTileFrom(mode, x + 1, y) ?: 4096
        nearbyTiles[NEARBY_TILE_KEY_UP] = world.getTileFrom(mode, x    , y - 1) ?: 4906
        nearbyTiles[NEARBY_TILE_KEY_DOWN] = world.getTileFrom(mode, x    , y + 1) ?: 4096

        // try for
        var ret = 0
        for (i in 0..3) {
            try {
                if (!TileCodex[nearbyTiles[i]].isSolid &&
                    !TileCodex[nearbyTiles[i]].isFluid) {
                    ret += (1 shl i) // add 1, 2, 4, 8 for i = 0, 1, 2, 3
                }
            } catch (e: ArrayIndexOutOfBoundsException) {
            }

        }

        return ret
    }

    fun getNearbyTilesInfoWallSticker(x: Int, y: Int): Int {
        val nearbyTiles = IntArray(4)
        val NEARBY_TILE_KEY_BACK = NEARBY_TILE_KEY_UP
        nearbyTiles[NEARBY_TILE_KEY_LEFT] = world.getTileFrom(TERRAIN, x - 1, y) ?: 4096
        nearbyTiles[NEARBY_TILE_KEY_RIGHT] = world.getTileFrom(TERRAIN, x + 1, y) ?: 4096
        nearbyTiles[NEARBY_TILE_KEY_DOWN] = world.getTileFrom(TERRAIN, x    , y + 1) ?: 4096
        nearbyTiles[NEARBY_TILE_KEY_BACK] = world.getTileFrom(WALL,    x    , y) ?: 4096

        try {
            if (TileCodex[nearbyTiles[NEARBY_TILE_KEY_DOWN]].isSolid)
                // has tile on the bottom
                return 3
            else if (TileCodex[nearbyTiles[NEARBY_TILE_KEY_RIGHT]].isSolid
                     && TileCodex[nearbyTiles[NEARBY_TILE_KEY_LEFT]].isSolid)
                // has tile on both sides
                return 0
            else if (TileCodex[nearbyTiles[NEARBY_TILE_KEY_RIGHT]].isSolid)
                // has tile on the right
                return 2
            else if (TileCodex[nearbyTiles[NEARBY_TILE_KEY_LEFT]].isSolid)
                // has tile on the left
                return 1
            else if (TileCodex[nearbyTiles[NEARBY_TILE_KEY_BACK]].isSolid)
                // has tile on the back
                return 0
            else
                return 3
        } catch (e: ArrayIndexOutOfBoundsException) {
            return if (TileCodex[nearbyTiles[NEARBY_TILE_KEY_DOWN]].isSolid)
                // has tile on the bottom
                3 else 0
        }
    }

    fun getNearbyTilesInfoPlatform(x: Int, y: Int): Int {
        val nearbyTiles = IntArray(4)
        nearbyTiles[NEARBY_TILE_KEY_LEFT] =  world.getTileFrom(TERRAIN, x - 1, y) ?: 4096
        nearbyTiles[NEARBY_TILE_KEY_RIGHT] = world.getTileFrom(TERRAIN, x + 1, y) ?: 4096

        if ((TileCodex[nearbyTiles[NEARBY_TILE_KEY_LEFT]].isSolid &&
            TileCodex[nearbyTiles[NEARBY_TILE_KEY_RIGHT]].isSolid) ||
            isPlatform(nearbyTiles[NEARBY_TILE_KEY_LEFT]) &&
            isPlatform(nearbyTiles[NEARBY_TILE_KEY_RIGHT])) // LR solid || LR platform
            return 0
        else if (TileCodex[nearbyTiles[NEARBY_TILE_KEY_LEFT]].isSolid &&
                 !isPlatform(nearbyTiles[NEARBY_TILE_KEY_LEFT]) &&
                 !TileCodex[nearbyTiles[NEARBY_TILE_KEY_RIGHT]].isSolid &&
                 !isPlatform(nearbyTiles[NEARBY_TILE_KEY_RIGHT])) // L solid and not platform && R not solid and not platform
            return 4
        else if (TileCodex[nearbyTiles[NEARBY_TILE_KEY_RIGHT]].isSolid &&
                 !isPlatform(nearbyTiles[NEARBY_TILE_KEY_RIGHT]) &&
                 !TileCodex[nearbyTiles[NEARBY_TILE_KEY_LEFT]].isSolid &&
                 !isPlatform(nearbyTiles[NEARBY_TILE_KEY_LEFT])) // R solid and not platform && L not solid and nto platform
            return 6
        else if (TileCodex[nearbyTiles[NEARBY_TILE_KEY_LEFT]].isSolid &&
                 !isPlatform(nearbyTiles[NEARBY_TILE_KEY_LEFT])) // L solid && L not platform
            return 3
        else if (TileCodex[nearbyTiles[NEARBY_TILE_KEY_RIGHT]].isSolid &&
                 !isPlatform(nearbyTiles[NEARBY_TILE_KEY_RIGHT])) // R solid && R not platform
            return 5
        else if ((TileCodex[nearbyTiles[NEARBY_TILE_KEY_LEFT]].isSolid ||
                  isPlatform(nearbyTiles[NEARBY_TILE_KEY_LEFT])) &&
                 !TileCodex[nearbyTiles[NEARBY_TILE_KEY_RIGHT]].isSolid &&
                 !isPlatform(nearbyTiles[NEARBY_TILE_KEY_RIGHT])) // L solid or platform && R not solid and not platform
            return 1
        else if ((TileCodex[nearbyTiles[NEARBY_TILE_KEY_RIGHT]].isSolid ||
                  isPlatform(nearbyTiles[NEARBY_TILE_KEY_RIGHT])) &&
                 !TileCodex[nearbyTiles[NEARBY_TILE_KEY_LEFT]].isSolid &&
                 !isPlatform(nearbyTiles[NEARBY_TILE_KEY_LEFT])) // R solid or platform && L not solid and not platform
            return 2
        else
            return 7
    }

    private fun drawTile(mode: Int, tilewisePosX: Int, tilewisePosY: Int, sheetX: Int, sheetY: Int) {
        if (mode == TERRAIN || mode == WALL)
            tilesTerrain.renderInUse(
                    FastMath.floor((tilewisePosX * TILE_SIZE).toFloat()),
                    FastMath.floor((tilewisePosY * TILE_SIZE).toFloat()),
                    sheetX, sheetY
            )
        else if (mode == WIRE)
            tilesWire.renderInUse(
                    FastMath.floor((tilewisePosX * TILE_SIZE).toFloat()),
                    FastMath.floor((tilewisePosY * TILE_SIZE).toFloat()),
                    sheetX, sheetY
            )
        else
            throw IllegalArgumentException()
    }

    fun clampH(x: Int): Int {
        if (x < 0) {
            return 0
        } else if (x > world.height * TILE_SIZE) {
            return world.height * TILE_SIZE
        } else {
            return x
        }
    }

    fun clampWTile(x: Int): Int {
        if (x < 0) {
            return 0
        } else if (x > world.width) {
            return world.width
        } else {
            return x
        }
    }

    fun clampHTile(x: Int): Int {
        if (x < 0) {
            return 0
        } else if (x > world.height) {
            return world.height
        } else {
            return x
        }
    }

    fun getRenderStartX(): Int = x / TILE_SIZE
    fun getRenderStartY(): Int = y / TILE_SIZE

    fun getRenderEndX(): Int = clampWTile(getRenderStartX() + (width / TILE_SIZE) + 2)
    fun getRenderEndY(): Int = clampHTile(getRenderStartY() + (height / TILE_SIZE) + 2)

    fun isConnectSelf(b: Int?): Boolean = TILES_CONNECT_SELF.contains(b)
    fun isConnectMutual(b: Int?): Boolean = TILES_CONNECT_MUTUAL.contains(b)
    fun isWallSticker(b: Int?): Boolean = TILES_WALL_STICKER.contains(b)
    fun isPlatform(b: Int?): Boolean = TILES_WALL_STICKER_CONNECT_SELF.contains(b)
    fun isBlendMul(b: Int?): Boolean = TILES_BLEND_MUL.contains(b)

    fun tileInCamera(x: Int, y: Int) =
            x >= MapCamera.x.div(TILE_SIZE) && y >= MapCamera.y.div(TILE_SIZE) &&
            x <= MapCamera.x.plus(width).div(TILE_SIZE) && y <= MapCamera.y.plus(width).div(TILE_SIZE)
}
