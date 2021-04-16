# Game Phases

Game Phases is a Fabric alternative to Game Stages by Darkhax. 
It supports the idea of separating the game into "phases," each of which unlocks new content.
Each phase has a prerequisite, and until that phase is unlocked, all content inside it will be unusable.

Game Phases relies on KubeJS for configuration and setup. All Game Phase scripts are in `server_scripts`.

---
## Creating Stages

Define a stage:
```javascript
onEvent('gamephases.initialize', event => {
    event.phase('modpack:one');
});
```

Grant the stage to the user when they obtain stone:
```json
{
  "parent": "minecraft:recipes/root",
  "rewards": {
    "phase": [
      "modpack:one"
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

Game Phases allows you to restrict access to items based on stages.
As a general overview, each stage can blacklist specific items. If a player does not meet the requirements
for any stage that blacklists a given item, they will not be able to use it.

General blacklist restrictions include:
 - Blocked out item tooltip
 - Unable to use (right-click) the item
 - Prevent item pickups
 - Cancel any recipes involving the item [NYI]
 - Drop when equipped [NYI]
 - Invisible in REI and creative inventory [NYI]

### Gating items behind a stage:
```javascript
onEvent('gamephases.initialize', event => {
    event.phase('modpack:one');
        .item('minecraft:iron_ingot')
        .item('minecraft:*');
});
```

---

## Dimension Restrictions

You can prevent access to dimensions through dimension restrictions. 

General dimension restrictions include:
 - Any teleport to this dimension will be cancelled.

### Gating dimensions behind a stage:
```javascript
onEvent('gamephases.initialize', event => {
    # Lock Nether Entry behind 'modpack:one'
    event.phase('modpack:one')
        .dimension('minecraft:the_nether');
});
```