package com.Torvald.Terrarum.Actors;

import com.Torvald.JsonFetcher;
import com.Torvald.Terrarum.Actors.Faction.Faction;
import com.Torvald.Terrarum.Game;
import com.Torvald.spriteAnimation.SpriteAnimation;
import com.google.gson.JsonObject;
import org.lwjgl.Sys;
import org.newdawn.slick.SlickException;

import java.io.IOException;

/**
 * Created by minjaesong on 16-02-03.
 */
public class PBFSigrid {

    private static String FACTION_PATH = "./res/raw/";

    public Player build() throws SlickException {
        Player p = new Player();

        p.sprite = new SpriteAnimation();
        p.sprite.setDimension(28, 50);
        p.sprite.setSpriteImage("res/graphics/sprites/test_player.png");
        p.sprite.setDelay(200);
        p.sprite.setRowsAndFrames(1, 1);
        p.sprite.setAsVisible();
        p.sprite.composeSprite();

        p.spriteGlow = new SpriteAnimation();
        p.spriteGlow.setDimension(28, 50);
        p.spriteGlow.setSpriteImage("res/graphics/sprites/test_player_glow.png");
        p.spriteGlow.setDelay(200);
        p.spriteGlow.setRowsAndFrames(1, 1);
        p.spriteGlow.setAsVisible();
        p.spriteGlow.composeSprite();


        p.actorValue = new ActorValue();
        p.actorValue.set("scale", 1.0f);
        p.actorValue.set("speed", 4.0f);
        p.actorValue.set("speedmult", 1.0f);
        p.actorValue.set("accel", Player.WALK_ACCEL_BASE);
        p.actorValue.set("accelmult", 1.0f);

        p.actorValue.set("jumppower", 5f);

        p.actorValue.set("basemass", 80f);

        p.actorValue.set("physiquemult", 1); // Constant 1.0 for player, meant to be used by random mobs
        /**
         * fixed value, or 'base value', from creature strength of Dwarf Fortress.
         * Human race uses 1000. (see CreatureHuman.json)
         */
        p.actorValue.set("strength", 1414);
        p.actorValue.set("encumbrance", 1000);

        p.actorValue.set("name", "Sigrid");

        p.actorValue.set("intelligent", true);

        p.actorValue.set("luminosity", 22819);

        p.setHitboxDimension(18, 46, 8, 0);

        p.inventory = new ActorInventory(0x7FFFFFFF, true);

        p.setPosition(4096 * 16, 300 * 16);

        p.assignFaction(loadFactioningData("FactionSigrid.json"));

        return p;
    }

    private Faction loadFactioningData(String filename) {
        JsonObject jsonObject = null;
        try {
            jsonObject = JsonFetcher.readJson(FACTION_PATH + filename);
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        Faction faction = new Faction(jsonObject.get("factionname").getAsString());

        jsonObject.get("factionamicable").getAsJsonArray().forEach(
                jobj -> faction.addFactionAmicable(jobj.getAsString())
        );
        jsonObject.get("factionneutral").getAsJsonArray().forEach(
                jobj -> faction.addFactionNeutral(jobj.getAsString())
        );
        jsonObject.get("factionhostile").getAsJsonArray().forEach(
                jobj -> faction.addFactionHostile(jobj.getAsString())
        );
        jsonObject.get("factionfearful").getAsJsonArray().forEach(
                jobj -> faction.addFactionFearful(jobj.getAsString())
        );

        return faction;
    }
}
