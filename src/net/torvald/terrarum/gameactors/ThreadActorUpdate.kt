package net.torvald.terrarum.gameactors

import com.badlogic.gdx.Gdx
import net.torvald.terrarum.Terrarum

/**
 * Created by minjaesong on 16-05-25.
 */
class ThreadActorUpdate(val startIndex: Int, val endIndex: Int) : Runnable {
    override fun run() {
        for (i in startIndex..endIndex) {
            val it = Terrarum.ingame!!.actorContainer[i]
            it.update(Gdx.graphics.deltaTime)

            if (it is Pocketed) {
                it.inventory.forEach { inventoryEntry ->
                    inventoryEntry.item.effectWhileInPocket(Gdx.graphics.deltaTime)
                    if (it.equipped(inventoryEntry.item)) {
                        inventoryEntry.item.effectWhenEquipped(Gdx.graphics.deltaTime)
                    }
                }
            }
        }
    }
}