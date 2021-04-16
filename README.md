# Game Phases

Game Phases is a Fabric alternative to Game Stages by Darkhax. 
It supports the idea of separating the game into "phases," each of which unlocks new content.
Each phase has a prerequisite, and until that phase is unlocked, all content inside it will be unusable.

Game Phases relies on KubeJS for configuration and setup. All Game Phase scripts are in `server_scripts`.

---
## Dependencies

Due to Game Phases being a 'modpack-mod' (one that users would not normally install on their own),
it does not include/JIJ dependencies with it. You will have to install these separately:
- [FibLib](https://github.com/Haven-King/FibLib) by HavenKing
- [Cardinal Components Base & World](https://www.curseforge.com/minecraft/mc-mods/cardinal-components) by Onyx Studios
- [KubeJS](https://www.curseforge.com/minecraft/mc-mods/kubejs-fabric) by shedaniel & Latvian
- [Rhino](https://www.curseforge.com/minecraft/mc-mods/rhino) by Latvian
- [Architectury](https://www.curseforge.com/minecraft/mc-mods/architectury-fabric) by shedaniel

### Compatibility

Game Phases has built-in compatibility with the following mods:
- [Roughly Enough Items](https://www.curseforge.com/minecraft/mc-mods/roughly-enough-items) by shedaniel
    - Restricted items are hidden from the item view


---
## Creating Phases

Define a phase:
```javascript
onEvent('gamephases.initialize', event => {
    event.phase('one');
});
```

Grant the phase to the user when they obtain stone:
```json
{
  "parent": "minecraft:recipes/root",
  "rewards": {
    "phase": [
      "one"
    ]
  },
  "criteria": {
    "has_cobblestone": {
      "trigger": "minecraft:inventory_changed",
      "conditions": {
        "items": [
          {
            "item": "minecraft:cobblestone"
          }
        ]
      }
    }
  },
  "requirements": [
    [
      "has_cobblestone"
    ]
  ]
}
```

---

## Item Restrictions

Game Phases allows you to restrict access to items based on phases.
As a general overview, each phase can blacklist specific items. If a player does not meet the requirements
for any phase that blacklists a given item, they will not be able to use it.

General blacklist restrictions include:
 - Blocked out item tooltip
 - Unable to use (right-click) the item
 - Prevent item pickups
 - Cancel any recipes involving the item
 - Drop when equipped
 - Invisible in REI and creative inventory [NYI]

### Gating items behind a phase:
```javascript
onEvent('gamephases.initialize', event => {
    // Lock Iron Ingot & all entries under the mymod namespace under phase one
    event.phase('one');
        .item('minecraft:iron_ingot')
        .item('mymod:*');
});
```

---

## Block Restrictions

Block Restrictions are similar to Item Restrictions. 
[FibLib](https://github.com/Haven-King/FibLib) is used to restrict the visibility of hidden blocks,
and additional tweaks are implemented to make the hidden block as non-visible as possible.

General blacklist restrictions include:
- Full visibility change through FibLib
- Altered break drops

### Gating blocks behind a phase:
*The following example hides Diamond Ore as stone*.
```javascript
onEvent('gamephases.initialize', event => {
    // Replace stone with Diamond Ore
    event.phase('one');
        .block('minecraft:diamond_ore', 'minecraft:stone');
});
```

---

## Dimension Restrictions

You can prevent access to dimensions through dimension restrictions. 

General dimension restrictions include:
 - Any teleport to this dimension will be cancelled.

### Gating dimensions behind a phase:
```javascript
onEvent('gamephases.initialize', event => {
    // Lock Nether Entry behind 'one'
    event.phase('one')
        .dimension('minecraft:the_nether');
});
```

---

## Entity Restrictions

You can prevent mob spawns through entity restrictions.
When a mob attempts to spawn naturally, it will check phase restrictions.
If any phase restricts the mob and no nearby players have passed the phase, the spawn will fail.

By default, mob spawns check for players 128 blocks out from their position.
By lowering this counter, you can prevent spawns near the player based on phase, while still allowing
the mob to spawn further out, regardless of the current phase.

### Gating entities behind a phase:
```javascript
onEvent('gamephases.initialize', event => {
    // Prevent creepers from spawning around players that have not passed phase one
    event.phase('one')
        .entity('minecraft:creeper');
});
```

---

## Other KubeJS utilities

Game Phases exposes several utility methods you can use with KubeJS to depend on phases.
Here is an example script which prints an extra message to the player if they have the `one` game phase:

*Note: phases are synced S2C, which allows for client-side KubeJS scripts that depend on phases.*
```javascript
onEvent('player.chat', event => {
    var hasOne = event.player.hasPhase('one');
    
    // If the player has unlocked the 'one' rank, print an extra message.
    if(hasOne) {
        event.player.tell('You are powerful.')
    }
});
```
---

### License
Game Phases is available under MIT.
