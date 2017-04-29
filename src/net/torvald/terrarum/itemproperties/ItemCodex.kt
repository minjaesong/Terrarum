package net.torvald.terrarum.itemproperties

import net.torvald.point.Point2d
import net.torvald.terrarum.KVHashMap
import net.torvald.terrarum.gameactors.CanBeAnItem
import net.torvald.terrarum.Terrarum
import net.torvald.terrarum.gameactors.ActorWithPhysics
import net.torvald.terrarum.gamecontroller.mouseTileX
import net.torvald.terrarum.gamecontroller.mouseTileY
import net.torvald.terrarum.gameworld.GameWorld
import net.torvald.terrarum.worlddrawer.BlocksDrawer
import net.torvald.terrarum.blockproperties.BlockCodex
import net.torvald.terrarum.worlddrawer.FeaturesDrawer.TILE_SIZE
import org.newdawn.slick.GameContainer
import org.newdawn.slick.Image
import java.util.*

/**
 * Created by minjaesong on 16-03-15.
 */
object ItemCodex {

    /**
     * <ItemID or RefID for Actor, TheItem>
     * Will return corresponding Actor if ID >= ACTORID_MIN
     */
    private val itemCodex = HashMap<ItemID, GameItem>()
    private val dynamicItemDescription = HashMap<ItemID, KVHashMap>()

    val ITEM_TILES = 0..GameWorld.TILES_SUPPORTED - 1
    val ITEM_WALLS = GameWorld.TILES_SUPPORTED..GameWorld.TILES_SUPPORTED * 2 - 1
    val ITEM_WIRES = GameWorld.TILES_SUPPORTED * 2..GameWorld.TILES_SUPPORTED * 2 + 255
    val ITEM_STATIC = ITEM_WIRES.endInclusive + 1..32767
    val ITEM_DYNAMIC = 32768..0x0FFF_FFFF
    val ACTORID_MIN = ITEM_DYNAMIC.endInclusive + 1


    private val itemImagePlaceholder = Image("./assets/item_kari_24.tga")


    init {
        // blocks.csvs are loaded by ModMgr beforehand
        // block items (blocks and walls are the same thing basically)
        for (i in ITEM_TILES + ITEM_WALLS) {
            itemCodex[i] = object : GameItem() {
                override val originalID = i
                override var dynamicID = i
                override val isUnique: Boolean = false
                override var baseMass: Double = BlockCodex[i].density / 1000.0
                override var baseToolSize: Double? = null
                override var equipPosition = EquipPosition.HAND_GRIP
                override val originalName = BlockCodex[i % ITEM_WALLS.first].nameKey
                override var stackable = true
                override var inventoryCategory = if (i in ITEM_TILES) Category.BLOCK else Category.WALL
                override var isDynamic = false
                override val material = Material(0,0,0,0,0,0,0,0,0,0.0)

                init {

                }

                override fun primaryUse(gc: GameContainer, delta: Int): Boolean {
                    return false
                    // TODO base punch attack
                }

                override fun secondaryUse(gc: GameContainer, delta: Int): Boolean {
                    val mousePoint = Point2d(gc.mouseTileX.toDouble(), gc.mouseTileY.toDouble())

                    // check for collision with actors (BLOCK only)
                    if (this.inventoryCategory == Category.BLOCK) {
                        Terrarum.ingame!!.actorContainer.forEach {
                            if (it is ActorWithPhysics && it.tilewiseHitbox.intersects(mousePoint))
                                return false
                        }
                    }

                    // return false if the tile is already there
                    if (this.inventoryCategory == Category.BLOCK &&
                        this.dynamicID == Terrarum.ingame!!.world.getTileFromTerrain(gc.mouseTileX, gc.mouseTileY) ||
                        this.inventoryCategory == Category.WALL &&
                        this.dynamicID - ITEM_WALLS.start == Terrarum.ingame!!.world.getTileFromWall(gc.mouseTileX, gc.mouseTileY) ||
                        this.inventoryCategory == Category.WIRE &&
                        this.dynamicID - ITEM_WIRES.start == Terrarum.ingame!!.world.getTileFromWire(gc.mouseTileX, gc.mouseTileY)
                            )
                        return false

                    // filter passed, do the job
                    // FIXME this is only useful for Player
                    if (i in ITEM_TILES) {
                        Terrarum.ingame!!.world.setTileTerrain(
                                gc.mouseTileX,
                                gc.mouseTileY,
                                i
                        )
                    }
                    else {
                        Terrarum.ingame!!.world.setTileWall(
                                gc.mouseTileX,
                                gc.mouseTileY,
                                i
                        )
                    }

                    return true
                }
            }
        }

        // test copper pickaxe
        /*itemCodex[ITEM_STATIC.first] = object : GameItem() {
            override val originalID = ITEM_STATIC.first
            override var dynamicID = originalID
            override val isUnique = false
            override val originalName = ""
            override var baseMass = 10.0
            override var baseToolSize: Double? = 10.0
            override var stackable = true
            override var maxDurability = 147//606
            override var durability = maxDurability.toFloat()
            override var equipPosition = EquipPosition.HAND_GRIP
            override var inventoryCategory = Category.TOOL
            override val isDynamic = true
            override val material = Material(0,0,0,0,0,0,0,0,1,0.0)

            init {
                itemProperties[IVKey.ITEMTYPE] = IVKey.ItemType.PICK
                name = "Stone pickaxe"
            }

            override fun primaryUse(gc: GameContainer, delta: Int): Boolean {
                val mousePoint = Point2d(gc.mouseTileX.toDouble(), gc.mouseTileY.toDouble())
                val actorvalue = Terrarum.ingame!!.player!!.actorValue


                using = true

                // linear search filter (check for intersection with tilewise mouse point and tilewise hitbox)
                // return false if hitting actors
                Terrarum.ingame!!.actorContainer.forEach {
                    if (it is ActorWithPhysics && it.tilewiseHitbox.intersects(mousePoint))
                        return false
                }

                // return false if there's no tile
                if (Block.AIR == Terrarum.ingame!!.world.getTileFromTerrain(gc.mouseTileX, gc.mouseTileY))
                    return false


                // filter passed, do the job
                val swingDmgToFrameDmg = delta.toDouble() / actorvalue.getAsDouble(AVKey.ACTION_INTERVAL)!!

                Terrarum.ingame!!.world.inflictTerrainDamage(
                        gc.mouseTileX,
                        gc.mouseTileY,
                        Calculate.pickaxePower(Terrarum.ingame!!.player!!, material) * swingDmgToFrameDmg
                )
                return true
            }

            override fun endPrimaryUse(gc: GameContainer, delta: Int): Boolean {
                using = false
                // reset action timer to zero
                Terrarum.ingame!!.player!!.actorValue[AVKey.__ACTION_TIMER] = 0.0
                return true
            }
        }*/


        // read from save (if applicable) and fill dynamicItemDescription
    }

    /**
     * Returns clone of the item in the Codex
     */
    operator fun get(code: ItemID): GameItem {
        if (code <= ITEM_STATIC.endInclusive) // generic item
            return itemCodex[code]!!.clone() // from CSV
        else if (code <= ITEM_DYNAMIC.endInclusive) {
            TODO("read from dynamicitem description (JSON)")
        }
        else {
            val a = Terrarum.ingame!!.getActorByID(code) // actor item
            if (a is CanBeAnItem) return a.itemData

            throw IllegalArgumentException("Attempted to get item data of actor that cannot be an item. ($a)")
        }
    }

    /**
     * Mainly used by GameItemLoader
     */
    operator fun set(code: ItemID, item: GameItem) {
        itemCodex[code] = item
    }

    fun getItemImage(item: GameItem): Image {
        // terrain
        if (item.originalID in ITEM_TILES) {
            return BlocksDrawer.tilesTerrain.getSubImage(
                    (item.originalID % 16) * 16,
                    item.originalID / 16
            )
        }
        // wall
        else if (item.originalID in ITEM_WALLS) {
            return BlocksDrawer.tileItemWall.getSubImage(
                    (item.originalID.minus(ITEM_WALLS.first) % 16) * TILE_SIZE,
                    (item.originalID.minus(ITEM_WALLS.first) / 16) * TILE_SIZE,
                    TILE_SIZE, TILE_SIZE
            )
        }
        // wire
        else if (item.originalID in ITEM_WIRES) {
            return BlocksDrawer.tilesWire.getSubImage((item.originalID % 16) * 16, item.originalID / 16)
        }
        // TODO get it real, using originalID...?
        else
            return itemImagePlaceholder
    }

    fun hasItem(itemID: Int): Boolean = dynamicItemDescription.containsKey(itemID)
}