package net.torvald.terrarum.itemproperties

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import net.torvald.terrarum.KVHashMap
import net.torvald.terrarum.Terrarum
import net.torvald.terrarum.blockproperties.Fluid
import net.torvald.terrarum.gameworld.GameWorld
import net.torvald.terrarum.modulebasegame.Ingame
import net.torvald.terrarum.modulebasegame.gameactors.CanBeAnItem
import net.torvald.terrarum.worlddrawer.BlocksDrawer
import java.util.*

/**
 * Created by minjaesong on 2016-03-15.
 */
object ItemCodex {

    /**
     * <ItemID or RefID for Actor, TheItem>
     * Will return corresponding Actor if ID >= ACTORID_MIN
     */
    val itemCodex = HashMap<ItemID, GameItem>()
    private val dynamicItemDescription = HashMap<ItemID, KVHashMap>()

    val ITEM_TILES = 0..GameWorld.TILES_SUPPORTED - 1
    val ITEM_WALLS = GameWorld.TILES_SUPPORTED..GameWorld.TILES_SUPPORTED * 2 - 1
    val ITEM_WIRES = GameWorld.TILES_SUPPORTED * 2..GameWorld.TILES_SUPPORTED * 2 + 255
    val ITEM_STATIC = ITEM_WIRES.endInclusive + 1..0x0F_FFFF
    val ITEM_DYNAMIC = 0x10_0000..0x0FFF_FFFF
    val ACTORID_MIN = ITEM_DYNAMIC.endInclusive + 1

    private val itemImagePlaceholder = TextureRegion(Texture("./assets/item_kari_24.tga"))

    //private val ingame = Terrarum.ingame!! as Ingame // WARNING you can't put this here, ExceptionInInitializerError


    // TODO: when generalised, there's no guarantee that blocks will be used as an item. Write customised item prop loader and init it on the Ingame

    init {
        //val ingame = Terrarum.ingame!! as Ingame // WARNING you can't put this here, ExceptionInInitializerError


        /*println("[ItemCodex] recording item ID ")

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
                    print("$originalID ")
                }

                override fun startPrimaryUse(delta: Float): Boolean {
                    return false
                    // TODO base punch attack
                }

                override fun startSecondaryUse(delta: Float): Boolean {
                    val mousePoint = Point2d(Terrarum.mouseTileX.toDouble(), Terrarum.mouseTileY.toDouble())

                    // check for collision with actors (BLOCK only)
                    if (this.inventoryCategory == Category.BLOCK) {
                        ingame.actorContainer.forEach {
                            if (it is ActorWBMovable && it.hIntTilewiseHitbox.intersects(mousePoint))
                                return false
                        }
                    }

                    // return false if the tile is already there
                    if (this.inventoryCategory == Category.BLOCK &&
                        this.dynamicID == ingame.world.getTileFromTerrain(Terrarum.mouseTileX, Terrarum.mouseTileY) ||
                        this.inventoryCategory == Category.WALL &&
                        this.dynamicID - ITEM_WALLS.start == ingame.world.getTileFromWall(Terrarum.mouseTileX, Terrarum.mouseTileY) ||
                        this.inventoryCategory == Category.WIRE &&
                        this.dynamicID - ITEM_WIRES.start == ingame.world.getTileFromWire(Terrarum.mouseTileX, Terrarum.mouseTileY)
                            )
                        return false

                    // filter passed, do the job
                    // FIXME this is only useful for Player
                    if (i in ITEM_TILES) {
                        ingame.world.setTileTerrain(
                                Terrarum.mouseTileX,
                                Terrarum.mouseTileY,
                                i
                        )
                    }
                    else {
                        ingame.world.setTileWall(
                                Terrarum.mouseTileX,
                                Terrarum.mouseTileY,
                                i
                        )
                    }

                    return true
                }
            }
        }*/

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

            override fun startPrimaryUse(delta: Float): Boolean {
                val mousePoint = Point2d(Terrarum.mouseTileX.toDouble(), Terrarum.mouseTileY.toDouble())
                val actorvalue = ingame.actorNowPlaying.actorValue


                using = true

                // linear search filter (check for intersection with tilewise mouse point and tilewise hitbox)
                // return false if hitting actors
                ingame.actorContainer.forEach {
                    if (it is ActorWBMovable && it.hIntTilewiseHitbox.intersects(mousePoint))
                        return false
                }

                // return false if there's no tile
                if (Block.AIR == ingame.world.getTileFromTerrain(Terrarum.mouseTileX, Terrarum.mouseTileY))
                    return false


                // filter passed, do the job
                val swingDmgToFrameDmg = delta.toDouble() / actorvalue.getAsDouble(AVKey.ACTION_INTERVAL)!!

                ingame.world.inflictTerrainDamage(
                        Terrarum.mouseTileX,
                        Terrarum.mouseTileY,
                        Calculate.pickaxePower(ingame.actorNowPlaying, material) * swingDmgToFrameDmg
                )
                return true
            }

            override fun endPrimaryUse(delta: Float): Boolean {
                using = false
                // reset action timer to zero
                ingame.actorNowPlaying.actorValue[AVKey.__ACTION_TIMER] = 0.0
                return true
            }
        }*/


        // test water bucket
        itemCodex[9000] = object : GameItem() {
            override var dynamicID: ItemID = 9000
            override val originalID: ItemID = 9000

            override val isUnique: Boolean = true
            override val originalName: String = "Infinite Water Bucket"

            override var baseMass: Double = 1000.0
            override var baseToolSize: Double? = null

            override var inventoryCategory: String = "tool"
            override var stackable: Boolean = false

            override val isDynamic: Boolean = false
            override val material: Material = Material(1,1,1,1,1,1,1,1,1,1.0)

            override val equipPosition: Int = EquipPosition.HAND_GRIP

            override fun startPrimaryUse(delta: Float): Boolean {
                val ingame = Terrarum.ingame!! as Ingame // must be in here
                ingame.world.setFluid(Terrarum.mouseTileX, Terrarum.mouseTileY, Fluid.WATER, 4f)
                return true
            }
        }


        // test lava bucket
        itemCodex[9001] = object : GameItem() {
            override var dynamicID: ItemID = 9001
            override val originalID: ItemID = 9001

            override val isUnique: Boolean = true
            override val originalName: String = "Infinite Lava Bucket"

            override var baseMass: Double = 1000.0
            override var baseToolSize: Double? = null

            override var inventoryCategory: String = "tool"
            override var stackable: Boolean = false

            override val isDynamic: Boolean = false
            override val material: Material = Material(1,1,1,1,1,1,1,1,1,1.0)

            override val equipPosition: Int = EquipPosition.HAND_GRIP

            override fun startPrimaryUse(delta: Float): Boolean {
                val ingame = Terrarum.ingame!! as Ingame // must be in here
                ingame.world.setFluid(Terrarum.mouseTileX, Terrarum.mouseTileY, Fluid.LAVA, 4f)
                return true
            }
        }


        // read from save (if applicable) and fill dynamicItemDescription



        println()
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
            val a = (Terrarum.ingame!! as Ingame).getActorByID(code) // actor item
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

    fun getItemImage(item: GameItem?): TextureRegion? {
        if (item == null) return null

        return getItemImage(item.originalID)
    }

    fun getItemImage(itemOriginalID: Int): TextureRegion {
        // terrain
        if (itemOriginalID in ITEM_TILES) {
            return BlocksDrawer.tilesTerrain.get(
                    (itemOriginalID % 16) * 16,
                     itemOriginalID / 16
            )
        }
        // wall
        else if (itemOriginalID in ITEM_WALLS) {
            return BlocksDrawer.tileItemWall.get(
                    (itemOriginalID.minus(ITEM_WALLS.first) % 16),
                    (itemOriginalID.minus(ITEM_WALLS.first) / 16)
            )
        }
        // wire
        else if (itemOriginalID in ITEM_WIRES) {
            return BlocksDrawer.tilesWire.get((itemOriginalID % 16) * 16, itemOriginalID / 16)
        }
        // TODO get it real, using originalID...?
        else
            return itemImagePlaceholder
    }

    fun hasItem(itemID: Int): Boolean = dynamicItemDescription.containsKey(itemID)
}