package com.torvald.terrarum.mapdrawer

import com.torvald.terrarum.gamemap.GameMap
import com.torvald.terrarum.gamemap.PairedMapLayer
import com.torvald.terrarum.Terrarum
import com.torvald.terrarum.tileproperties.TileNameCode
import com.torvald.terrarum.tileproperties.TilePropCodex
import com.jme3.math.FastMath
import com.torvald.terrarum.setBlendMul
import com.torvald.terrarum.setBlendNormal
import org.lwjgl.opengl.GL11
import org.newdawn.slick.GameContainer
import org.newdawn.slick.Graphics
import org.newdawn.slick.SlickException
import org.newdawn.slick.SpriteSheet
import java.util.*

/**
 * Created by minjaesong on 16-01-19.
 */
object MapCamera {
    private val map: GameMap = Terrarum.game.map;

    var cameraX = 0
        private set
    var cameraY = 0
        private set

    private val TSIZE = MapDrawer.TILE_SIZE

    private var tilesWall: SpriteSheet = SpriteSheet("./res/graphics/terrain/wall.png", TSIZE, TSIZE)
    private var tilesTerrain: SpriteSheet = SpriteSheet("./res/graphics/terrain/terrain.png", TSIZE, TSIZE)
    private var tilesWire: SpriteSheet = SpriteSheet("./res/graphics/terrain/wire.png", TSIZE, TSIZE)
    private var tilesetBook: Array<SpriteSheet> = arrayOf(tilesWall, tilesTerrain, tilesWire)

    private val WALL = GameMap.WALL
    private val TERRAIN = GameMap.TERRAIN
    private val WIRE = GameMap.WIRE

    private var renderWidth: Int = 0
    private var renderHeight: Int = 0

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
    private val TILES_CONNECT_SELF = arrayOf(
              TileNameCode.ICE_MAGICAL
            , TileNameCode.ILLUMINATOR_BLACK
            , TileNameCode.ILLUMINATOR_BLUE
            , TileNameCode.ILLUMINATOR_BROWN
            , TileNameCode.ILLUMINATOR_CYAN
            , TileNameCode.ILLUMINATOR_FUCHSIA
            , TileNameCode.ILLUMINATOR_GREEN
            , TileNameCode.ILLUMINATOR_GREEN_DARK
            , TileNameCode.ILLUMINATOR_GREY_DARK
            , TileNameCode.ILLUMINATOR_GREY_LIGHT
            , TileNameCode.ILLUMINATOR_GREY_MED
            , TileNameCode.ILLUMINATOR_ORANGE
            , TileNameCode.ILLUMINATOR_PURPLE
            , TileNameCode.ILLUMINATOR_RED
            , TileNameCode.ILLUMINATOR_TAN
            , TileNameCode.ILLUMINATOR_WHITE
            , TileNameCode.ILLUMINATOR_YELLOW
            , TileNameCode.ILLUMINATOR_BLACK_OFF
            , TileNameCode.ILLUMINATOR_BLUE_OFF
            , TileNameCode.ILLUMINATOR_BROWN_OFF
            , TileNameCode.ILLUMINATOR_CYAN_OFF
            , TileNameCode.ILLUMINATOR_FUCHSIA_OFF
            , TileNameCode.ILLUMINATOR_GREEN_OFF
            , TileNameCode.ILLUMINATOR_GREEN_DARK_OFF
            , TileNameCode.ILLUMINATOR_GREY_DARK_OFF
            , TileNameCode.ILLUMINATOR_GREY_LIGHT_OFF
            , TileNameCode.ILLUMINATOR_GREY_MED_OFF
            , TileNameCode.ILLUMINATOR_ORANGE_OFF
            , TileNameCode.ILLUMINATOR_PURPLE_OFF
            , TileNameCode.ILLUMINATOR_RED_OFF
            , TileNameCode.ILLUMINATOR_TAN_OFF
            , TileNameCode.ILLUMINATOR_WHITE_OFF
            , TileNameCode.ILLUMINATOR_YELLOW
            , TileNameCode.SANDSTONE
            , TileNameCode.SANDSTONE_BLACK
            , TileNameCode.SANDSTONE_DESERT
            , TileNameCode.SANDSTONE_RED
            , TileNameCode.SANDSTONE_WHITE
            , TileNameCode.SANDSTONE_GREEN
    )

    /**
     * Connectivity group 02 : natural tiles
     * It holds different shading rule to discriminate with group 01, index 0 is middle tile.
     */
    private val TILES_CONNECT_MUTUAL = arrayOf(
              TileNameCode.STONE
            , TileNameCode.DIRT
            , TileNameCode.GRASS
            , TileNameCode.PLANK_BIRCH
            , TileNameCode.PLANK_BLOODROSE
            , TileNameCode.PLANK_EBONY
            , TileNameCode.PLANK_NORMAL
            , TileNameCode.SAND
            , TileNameCode.SAND_WHITE
            , TileNameCode.SAND_RED
            , TileNameCode.SAND_DESERT
            , TileNameCode.SAND_BLACK
            , TileNameCode.SAND_GREEN
            , TileNameCode.GRAVEL
            , TileNameCode.GRAVEL_GREY
            , TileNameCode.SNOW
            , TileNameCode.ICE_NATURAL
            , TileNameCode.ORE_COPPER
            , TileNameCode.ORE_IRON
            , TileNameCode.ORE_GOLD
            , TileNameCode.ORE_SILVER
            , TileNameCode.ORE_ILMENITE
            , TileNameCode.ORE_AURICHALCUM

            , TileNameCode.WATER
            , TileNameCode.WATER_1
            , TileNameCode.WATER_2
            , TileNameCode.WATER_3
            , TileNameCode.WATER_4
            , TileNameCode.WATER_5
            , TileNameCode.WATER_6
            , TileNameCode.WATER_7
            , TileNameCode.WATER_8
            , TileNameCode.WATER_9
            , TileNameCode.WATER_10
            , TileNameCode.WATER_11
            , TileNameCode.WATER_12
            , TileNameCode.WATER_13
            , TileNameCode.WATER_14
            , TileNameCode.WATER_15
            , TileNameCode.LAVA
            , TileNameCode.LAVA_1
            , TileNameCode.LAVA_2
            , TileNameCode.LAVA_3
            , TileNameCode.LAVA_4
            , TileNameCode.LAVA_5
            , TileNameCode.LAVA_6
            , TileNameCode.LAVA_7
            , TileNameCode.LAVA_8
            , TileNameCode.LAVA_9
            , TileNameCode.LAVA_10
            , TileNameCode.LAVA_11
            , TileNameCode.LAVA_12
            , TileNameCode.LAVA_13
            , TileNameCode.LAVA_14
            , TileNameCode.LAVA_15
    )

    /**
     * Torches, levers, switches, ...
     */
    private val TILES_WALL_STICKER = arrayOf(
              TileNameCode.TORCH
            , TileNameCode.TORCH_FROST
            , TileNameCode.TORCH_OFF
            , TileNameCode.TORCH_FROST_OFF
    )

    /**
     * platforms, ...
     */
    private val TILES_WALL_STICKER_CONNECT_SELF = arrayOf<Int>(

    )

    /**
     * Tiles that half-transparent and has hue
     * will blend colour using colour multiplication
     * i.e. red hues get lost if you dive into the water
     */
    private val TILES_BLEND_MUL = arrayOf(
              TileNameCode.WATER
            , TileNameCode.WATER_1
            , TileNameCode.WATER_2
            , TileNameCode.WATER_3
            , TileNameCode.WATER_4
            , TileNameCode.WATER_5
            , TileNameCode.WATER_6
            , TileNameCode.WATER_7
            , TileNameCode.WATER_8
            , TileNameCode.WATER_9
            , TileNameCode.WATER_10
            , TileNameCode.WATER_11
            , TileNameCode.WATER_12
            , TileNameCode.WATER_13
            , TileNameCode.WATER_14
            , TileNameCode.WATER_15
            , TileNameCode.LAVA
            , TileNameCode.LAVA_1
            , TileNameCode.LAVA_2
            , TileNameCode.LAVA_3
            , TileNameCode.LAVA_4
            , TileNameCode.LAVA_5
            , TileNameCode.LAVA_6
            , TileNameCode.LAVA_7
            , TileNameCode.LAVA_8
            , TileNameCode.LAVA_9
            , TileNameCode.LAVA_10
            , TileNameCode.LAVA_11
            , TileNameCode.LAVA_12
            , TileNameCode.LAVA_13
            , TileNameCode.LAVA_14
            , TileNameCode.LAVA_15
    )

    fun update(gc: GameContainer, delta_t: Int) {
        val player = Terrarum.game.player

        renderWidth = FastMath.ceil(Terrarum.WIDTH / Terrarum.game.screenZoom) // div, not mul
        renderHeight = FastMath.ceil(Terrarum.HEIGHT / Terrarum.game.screenZoom)

        // position - (WH / 2)
        cameraX = Math.round(FastMath.clamp(
                player.hitbox!!.centeredX - renderWidth / 2, TSIZE.toFloat(), map.width * TSIZE - renderWidth - TSIZE.toFloat()))
        cameraY = Math.round(FastMath.clamp(
                player.hitbox!!.centeredY - renderHeight / 2, TSIZE.toFloat(), map.height * TSIZE - renderHeight - TSIZE.toFloat()))
    }

    fun renderBehind(gc: GameContainer, g: Graphics) {
        /**
         * render to camera
         */
        setBlendNormal()
        drawTiles(WALL, false)
        drawTiles(TERRAIN, false)
    }

    fun renderFront(gc: GameContainer, g: Graphics) {
        setBlendMul()
        drawTiles(TERRAIN, true)
        setBlendNormal()
    }

    private fun drawTiles(mode: Int, drawModeTilesBlendMul: Boolean) {
        val for_y_start = div16(cameraY)
        val for_x_start = div16(cameraX)

        val for_y_end = clampHTile(for_y_start + div16(renderHeight) + 2)
        val for_x_end = clampWTile(for_x_start + div16(renderWidth) + 2)

        // initialise
        tilesetBook[mode].startUse()

        // loop
        for (y in for_y_start..for_y_end - 1) {
            for (x in for_x_start..for_x_end - 1) {

                val thisTile: Int?
                if (mode % 3 == WALL)
                    thisTile = map.getTileFromWall(x, y)
                else if (mode % 3 == TERRAIN)
                    thisTile = map.getTileFromTerrain(x, y)
                else if (mode % 3 == WIRE)
                    thisTile = map.getTileFromWire(x, y)
                else
                    throw IllegalArgumentException()

                val noDamageLayer = mode % 3 == WIRE

                // draw
                try {
                    if (

                    (mode == WALL || mode == TERRAIN)       // not an air tile

                            && (thisTile ?: 0) > 0
                            &&
                            // check if light level of nearby or this tile is illuminated
                    (    LightmapRenderer.getValueFromMap(x, y) ?: 0 > 0
                         || LightmapRenderer.getValueFromMap(x - 1, y) ?: 0 > 0
                         || LightmapRenderer.getValueFromMap(x + 1, y) ?: 0 > 0
                         || LightmapRenderer.getValueFromMap(x, y - 1) ?: 0 > 0
                         || LightmapRenderer.getValueFromMap(x, y + 1) ?: 0 > 0
                         || LightmapRenderer.getValueFromMap(x - 1, y - 1) ?: 0 > 0
                         || LightmapRenderer.getValueFromMap(x + 1, y + 1) ?: 0 > 0
                         || LightmapRenderer.getValueFromMap(x + 1, y - 1) ?: 0 > 0
                         || LightmapRenderer.getValueFromMap(x - 1, y + 1) ?: 0 > 0)
                    ) {

                        val nearbyTilesInfo: Int
                        if (isWallSticker(thisTile)) {
                            nearbyTilesInfo = getNearbyTilesInfoWallSticker(x, y)
                        } else if (isConnectMutual(thisTile)) {
                            nearbyTilesInfo = getNearbyTilesInfoNonSolid(x, y, mode)
                        } else if (isConnectSelf(thisTile)) {
                            nearbyTilesInfo = getNearbyTilesInfo(x, y, mode, thisTile)
                        } else {
                            nearbyTilesInfo = 0
                        }


                        val thisTileX: Int
                        if (!noDamageLayer)
                            thisTileX = PairedMapLayer.RANGE * ((thisTile ?: 0) % PairedMapLayer.RANGE) + nearbyTilesInfo
                        else
                            thisTileX = nearbyTilesInfo

                        val thisTileY = (thisTile ?: 0) / PairedMapLayer.RANGE

                        if (drawModeTilesBlendMul) {
                            if (isBlendMul(thisTile)) {
                                drawTile(mode, x, y, thisTileX, thisTileY)
                            }
                        } else {
                            // do NOT add "if (!isBlendMul(thisTile))"!
                            // or else they will not look like they should be when backed with wall
                            drawTile(mode, x, y, thisTileX, thisTileY)
                        }
                    }
                } catch (e: NullPointerException) {
                    // do nothing. This exception handling may hide erratic behaviour completely.
                }

            }
        }

        tilesetBook[mode].endUse()
    }

    private fun getGrassInfo(x: Int, y: Int, from: Int, to: Int): Int {
        return 0
    }

    /**

     * @param x
     * *
     * @param y
     * *
     * @return [0-15] 1: up, 2: right, 4: down, 8: left
     */
    private fun getNearbyTilesInfo(x: Int, y: Int, mode: Int, mark: Int?): Int {
        val nearbyTiles = IntArray(4)
        nearbyTiles[NEARBY_TILE_KEY_LEFT] = map.getTileFrom(mode, x - 1, y) ?: 4096
        nearbyTiles[NEARBY_TILE_KEY_RIGHT] = map.getTileFrom(mode, x + 1, y) ?: 4096
        nearbyTiles[NEARBY_TILE_KEY_UP] = map.getTileFrom(mode, x, y - 1) ?: 4906
        nearbyTiles[NEARBY_TILE_KEY_DOWN] = map.getTileFrom(mode, x, y + 1) ?: 4096

        // try for
        var ret = 0
        for (i in 0..3) {
            if (nearbyTiles[i] == mark) {
                ret += 1 shl i // add 1, 2, 4, 8 for i = 0, 1, 2, 3
            }
        }

        return ret
    }

    private fun getNearbyTilesInfoNonSolid(x: Int, y: Int, mode: Int): Int {
        val nearbyTiles = IntArray(4)
        nearbyTiles[NEARBY_TILE_KEY_LEFT] = map.getTileFrom(mode, x - 1, y) ?: 4096
        nearbyTiles[NEARBY_TILE_KEY_RIGHT] = map.getTileFrom(mode, x + 1, y) ?: 4096
        nearbyTiles[NEARBY_TILE_KEY_UP] = map.getTileFrom(mode, x, y - 1) ?: 4096
        nearbyTiles[NEARBY_TILE_KEY_DOWN] = map.getTileFrom(mode, x, y + 1) ?: 4096

        // try for
        var ret = 0
        for (i in 0..3) {
            try {
                if (!TilePropCodex.getProp(nearbyTiles[i]).isSolid) {
                    ret += (1 shl i) // add 1, 2, 4, 8 for i = 0, 1, 2, 3
                }
            } catch (e: ArrayIndexOutOfBoundsException) {
            }

        }

        return ret
    }

    private fun getNearbyTilesInfoWallSticker(x: Int, y: Int): Int {
        val nearbyTiles = IntArray(4)
        val NEARBY_TILE_KEY_BACK = NEARBY_TILE_KEY_UP
        nearbyTiles[NEARBY_TILE_KEY_LEFT] = map.getTileFrom(TERRAIN, x - 1, y) ?: 4096
        nearbyTiles[NEARBY_TILE_KEY_RIGHT] = map.getTileFrom(TERRAIN, x + 1, y) ?: 4096
        nearbyTiles[NEARBY_TILE_KEY_DOWN] = map.getTileFrom(TERRAIN, x, y + 1) ?: 4096
        nearbyTiles[NEARBY_TILE_KEY_BACK] = map.getTileFrom(WALL, x, y) ?: 4096

        try {
            if (TilePropCodex.getProp(nearbyTiles[NEARBY_TILE_KEY_RIGHT]).isSolid
                    && TilePropCodex.getProp(nearbyTiles[NEARBY_TILE_KEY_LEFT]).isSolid) {
                if (TilePropCodex.getProp(nearbyTiles[NEARBY_TILE_KEY_BACK]).isSolid)
                    return 0
                else
                    return 3
            } else if (TilePropCodex.getProp(nearbyTiles[NEARBY_TILE_KEY_RIGHT]).isSolid) {
                return 2
            } else if (TilePropCodex.getProp(nearbyTiles[NEARBY_TILE_KEY_LEFT]).isSolid) {
                return 1
            } else if (TilePropCodex.getProp(nearbyTiles[NEARBY_TILE_KEY_BACK]).isSolid) {
                return 0
            } else
                return 3
        } catch (e: ArrayIndexOutOfBoundsException) {
            return if (TilePropCodex.getProp(nearbyTiles[NEARBY_TILE_KEY_BACK]).isSolid)
                0
            else
                3
        }

    }

    private fun drawTile(mode: Int, tilewisePosX: Int, tilewisePosY: Int, sheetX: Int, sheetY: Int) {
        if (Terrarum.game.screenZoom == 1f) {
            tilesetBook[mode].renderInUse(
                    FastMath.floor((tilewisePosX * TSIZE).toFloat()), FastMath.floor((tilewisePosY * TSIZE).toFloat()), sheetX, sheetY)
        } else {
            tilesetBook[mode].getSprite(
                    sheetX, sheetY).drawEmbedded(
                    Math.round(tilewisePosX.toFloat() * TSIZE.toFloat() * Terrarum.game.screenZoom).toFloat(), Math.round(tilewisePosY.toFloat() * TSIZE.toFloat() * Terrarum.game.screenZoom).toFloat(), FastMath.ceil(TSIZE * Terrarum.game.screenZoom).toFloat(), FastMath.ceil(TSIZE * Terrarum.game.screenZoom).toFloat())
        }
    }

    fun div16(x: Int): Int = x and 0x7FFFFFFF shr 4
    fun mod16(x: Int): Int = x and 15
    fun quantise16(x: Int): Int = x and 0xFFFFFFF0.toInt()

    fun clampW(x: Int): Int {
        if (x < 0) {
            return 0
        } else if (x > map.width * TSIZE) {
            return map.width * TSIZE
        } else {
            return x
        }
    }

    fun clampH(x: Int): Int {
        if (x < 0) {
            return 0
        } else if (x > map.height * TSIZE) {
            return map.height * TSIZE
        } else {
            return x
        }
    }

    fun clampWTile(x: Int): Int {
        if (x < 0) {
            return 0
        } else if (x > map.width) {
            return map.width
        } else {
            return x
        }
    }

    fun clampHTile(x: Int): Int {
        if (x < 0) {
            return 0
        } else if (x > map.height) {
            return map.height
        } else {
            return x
        }
    }

    fun getRenderWidth(): Int = renderWidth
    fun getRenderHeight(): Int = renderHeight

    fun getRenderStartX(): Int = div16(cameraX)
    fun getRenderStartY(): Int = div16(cameraY)

    fun getRenderEndX(): Int = clampWTile(getRenderStartX() + div16(renderWidth) + 2)
    fun getRenderEndY(): Int = clampHTile(getRenderStartY() + div16(renderHeight) + 2)

    private fun isConnectSelf(b: Int?): Boolean = TILES_CONNECT_SELF.contains(b)
    private fun isConnectMutual(b: Int?): Boolean = TILES_CONNECT_MUTUAL.contains(b)
    private fun isWallSticker(b: Int?): Boolean = TILES_WALL_STICKER.contains(b)
    private fun isPlatform(b: Int?): Boolean = TILES_WALL_STICKER_CONNECT_SELF.contains(b)

    private fun isBlendMul(b: Int?): Boolean = TILES_BLEND_MUL.contains(b)

}