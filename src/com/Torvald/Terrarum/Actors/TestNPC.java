package com.Torvald.Terrarum.Actors;

import com.Torvald.Terrarum.Actors.Faction.Faction;
import com.Torvald.Terrarum.GameItem.InventoryItem;
import org.newdawn.slick.GameContainer;

import java.util.HashSet;

/**
 * Created by minjaesong on 16-01-31.
 */
public class TestNPC extends ActorWithBody implements AIControlled, Pocketed, CanBeStoredAsItem,
        Factionable {

    private InventoryItem itemData;
    // private ActorAI ai;
    private ActorInventory inventory;

    private HashSet<Faction> factionSet = new HashSet<>();

    @Override
    public void attachAI() {

    }

    @Override
    public void assignFaction(Faction f) {
        factionSet.add(f);
    }

    @Override
    public void unassignFaction(Faction f) {
        factionSet.remove(f);
    }

    @Override
    public HashSet<Faction> getAssignedFactions() {
        return factionSet;
    }

    @Override
    public void clearFactionAssigning() {
        factionSet.clear();
    }

    @Override
    public void attachItemData() {
        itemData = new InventoryItem() {
            @Override
            public float getWeight() {
                return getMass();
            }

            /** Set no effect */
            @Override
            public void effectWhileInPocket(GameContainer gc, int delta_t) {

            }

            /** Set no effect */
            @Override
            public void effectWhenPickedUp(GameContainer gc, int delta_t) {

            }

            /** Set no effect */
            @Override
            public void primaryUse(GameContainer gc, int delta_t) {

            }

            /** Set no effect */
            @Override
            public void secondaryUse(GameContainer gc, int delta_t) {

            }

            /** Set no effect */
            @Override
            public void effectWhenThrownAway(GameContainer gc, int delta_t) {

            }
        };
    }

    @Override
    public float getItemWeight() {
        return super.getMass();
    }

    @Override
    public void stopUpdateAndDraw() {
        super.setUpdate(false);
        super.setVisible(false);
    }

    @Override
    public void resumeUpdateAndDraw() {
        super.setUpdate(true);
        super.setVisible(true);
    }

    @Override
    public InventoryItem getItemData() {
        return itemData;
    }

    @Override
    public ActorInventory getInventory() {
        return null;
    }

    @Override
    public void overwriteInventory(ActorInventory inventory) {
        this.inventory = inventory;
    }
}
