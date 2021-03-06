# Faction documentation

```
{
  "factionname" : "player",

  "factionamicable" : ["follower"],
  "factionneutral" : [],
  "factionhostile" : ["wildlifehostile"],
  "factionfearful" : ["wildlifeflee"]
}
```

- factionname: Identifier of the faction data
- factionamicable: Amicable faction. Players can give/take items of actors of this faction, in plus to the ability of barter. Assigned actors (usually follower) will follow players in game.
- factionneutral: Explicit neutrality. If the assigned actor is intelligent (NPC AV), players can barter with them. Unassigned faction will be treated like "disliked"
- factionhostile: Assigned faction will attack players on the sight. e.g. 'wildlifehostile'
- factionfearful: Assigned faction will flee from players on the sight. e.g. 'wildlifeflee'


## Generic faction information

* player: Self-explanatory
* follower: Self-explanatory
* controlledvillager: Villagers of your hamlet
* wildlifehostile: Hostile mobs, self-explanatory
* wildlifeflee: Fleeing mobs, self-explanatory
* wildhamletvillager: Generic villager of the pre-generated hamlets.
* wildhamletcontroller: Ruler of the pre-generated hamlets.
