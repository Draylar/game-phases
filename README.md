# Game Phases

Game Phases is a Fabric alternative to Game Stages by Darkhax. 
It supports the idea of separating the game into "phases," each of which unlocks new content.
Each phase has a prerequisite, and until that phase is unlocked, all content inside it will be unusable.

Game Phases relies on KubeJS for configuration and setup. All Game Phase scripts are in `server_scripts`.

### Working with Stages

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

Gating items behind a stage:
```javascript
onEvent('gamephases.initialize', event => {
    event.phase('modpack:one');
    
    # Lock Iron Ingots behind 'modpack:one'
    event.item('modpack:one', 'minecraft:iron_ingot');
    
    # Lock all items from a namespace behind 'modpack:one'
    event.item('modpack:one', 'minecraft:*');
});
```

Gating dimensions behind a stage:
```javascript
onEvent('gamephases.initialize', event => {
    event.phase('modpack:one');
    
    # Lock Nether Entry behind 'modpack:one'
    event.dimension('modpack:one', 'minecraft:the_nether');
});
```