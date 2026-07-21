# Stasis Mod - Design Document

## Overview
Stasis Mod is a Minecraft NeoForge 1.21.1 mod that allows players to enter an isolated simulation dimension to prototype and build without affecting the multiplayer server.

## Current Status: Version 0.0.1

### ✅ Completed Features

#### Core Teleportation System
- **Player State Management**: Direct field manipulation (no NBT serialization)
  - Captures: inventory (36 slots), health, hunger, XP, position, rotation, gamemode
  - Backs up full NBT for Curios/neoforge:attachments restoration
  - Full state restoration on exit, including Curios attachments
  - **Survives disconnect/reconnect via persistent snapshots**
  - **Survives server crash/restart via disk persistence** (NEW)
  
- **Simplified Architecture**: Direct position-saving (no complex state machine)
  - Player position/rotation saved on entry
  - Player restored to saved location on exit
  - No chamber occupancy tracking needed
  - Supports concurrent multi-player use

- **Exit Mechanisms**:
  - ✅ Exit Beacon item (right-click to exit) - memory-backed
  - ✅ Debug command: `/stasis debug exit <player>` - memory-backed
  - ✅ Crash/restart recovery - disk-backed (auto-detects and restores on rejoin)
  - ✅ Darkness effect on exit (visual transition)
  - ✅ Proper teleportation to overworld with saved position

#### Security & Exploit Prevention

**Phase 1 (Critical)**:
- ✅ Cross-dimensional teleportation blocking (Waystones, Draconic Warp, enderpearls)
- ✅ Exploit item removal on exit (quantum singularities, flux plugs, waystones, etc.)
- ✅ Block entity resync after dimension change (prevents client spoofing)
- ✅ Proper teleportation event filtering (allows legitimate exits)

**Phase 2 (High Priority)**:
- ✅ Waystone placement blocking in simulation (all variants)
- ✅ Chunkloader blocking (FTB Chunks, Chunkloaders mod)
- ✅ Immersive Portals dimensional portal blocking
- ✅ User-friendly error messages for blocked placements

#### Activation Core Block
- ✅ Directional placement (respects player facing, horizontal only N/E/S/W)
- ✅ Create wrench rotation support (cycles N → E → S → W)
- ✅ Create wrench instant pickup to inventory (shift+click)
- ✅ Item drops when broken in survival
- ✅ No culling of adjacent block faces (occlusion shape override)

#### CC:Tweaked Integration
- ✅ Preloaded Lua script (`stasis.lua`)
- ✅ Chamber ready status checks FE level
- ✅ "Insufficient power" message with FE deficit calculation
- ✅ Clean interface with neural digitization flavor text

#### Create Integration
- ✅ Mechanical Crafting recipe for Stasis Activation Core
- ✅ 8×8 pattern (horizontal-only, no up/down)
- ✅ Auto-integrates with JEI/EMI (data-driven JSON)
- ✅ Follows Create's standard format exactly

#### Configuration System
- ✅ ServerConfig with FE settings (NeoForge ConfigSpec)
- ✅ Chamber capacity, activation cost, max transfer rate
- ✅ ConfigCache pattern for safe runtime access
- ✅ Settings synced on ModConfigEvent.Reloading

#### Custom Dimension & Plot System
- ✅ Stasis dimension with checkerboard world generation
- ✅ CheckerboardLevelSource chunk generator (3D checkerboard)
- ✅ Plot allocation and invisible barrier system
- ✅ Plot border visualization (glowing particle lines)
- ✅ Block break/place prevention outside plots

## Architecture

### Key Classes

**Block Entities & Blocks:**
- `ChamberTile.java` - Block entity managing chamber energy state (100k FE capacity, 2k FE per activation)
- `ActivationCoreBlock.java` - Activation core block with directional placement (N/E/S/W), Create wrench support, proper occlusion culling
- `ChamberBlocks.java` - Block/item registration (only ACTIVATION_CORE and EXIT_BEACON, removed template blocks)

**State Management:**
- `PlayerStateManager.java` - Direct field manipulation for complete state capture/restoration
  - Captures: inventory (36 slots), health, hunger, XP, position, rotation, gamemode, effects
  - Backs up full NBT for Curios/neoforge:attachments restoration
  - Removes exploit items on exit (Phase 1 security)
  - **Persists to disk** (`stasis_player_states/` directory) for crash/restart recovery
  - Survives disconnect via persistent snapshots AND disk persistence
- `PlayerStateSnapshot.java` - Serializable state snapshot
  - `serialize()` - writes to NBT for disk persistence
  - `deserialize()` - reads from disk on server startup

**Exit & Activation:**
- `ExitBeacon.java` - Item given to players in simulation for exiting (right-click to exit)
- CC:Tweaked Lua script - Preloaded interface with FE status display

**Dimension & Plots:**
- `CheckerboardLevelSource.java` - Custom chunk generator (3D checkerboard pattern)
- `CheckerboardGeneratorSettings.java` - Generator configuration
- `StasisDimensions.java` - Dimension resource keys and registration
- `PlotManager.java` - Plot allocation and lookup
- `PlotData.java` - Individual plot data (owner, index, bounds)
- `PlotBorderRenderer.java` - Glowing particle border visualization

**Configuration & Events:**
- `ServerConfig.java` - FE settings (capacity, activation cost, transfer rate)
- `ConfigCache.java` - Runtime config access
- `ServerEvents.java` - Event handlers:
  - `onPlayerLoggedIn()` - Restore state on reconnect
  - `onPlayerLoggedOut()` - Preserve state on disconnect
  - `onEntityTravelDimension()` - Block unauthorized escapes (Phase 1)
  - `onPlayerDimensionChange()` - Block entity resync (Phase 1)
  - `onPlayerInteractBlock()` - Create wrench rotation & pickup
  - `onBlockPlace()` - Block exploit placements (Phase 2: Waystones, chunkloaders, portals)
  - `onMobSpawn()` - Prevent mobs in simulation
- `PlotExploitPreventionEvent.java` - Block break/place prevention outside plots

**Commands:**
- `StasisCommand.java` - Debug commands (`/stasis debug enter`, `/stasis debug exit <player>`)

## Design Decisions

### State Management Architecture
- **Direct Field Manipulation**: Bypasses NBT serialization overhead, captures all player state directly (inventory, effects, position, rotation, gamemode, health, hunger, XP)
- **Full NBT Backup**: Additionally backs up complete player NBT to preserve Curios/neoforge:attachments
- **Dual Persistence**:
  - **Normal Exit (via beacon/debug)**: Uses in-memory snapshot with full inventory
  - **Crash/Restart Exit**: Uses disk-persisted snapshots loaded on server startup
  - **Storage**:
    - `{UUID}_snapshot.dat` - Basic state (position, health, hunger, XP, gamemode)
    - `{UUID}_curios.dat` - Full player NBT backup (has complete Inventory + Curios attachments)
  - **Recovery Process**:
    1. On server startup: All .dat files loaded into memory
    2. Player joins in simulation: onPlayerLoggedIn detects and calls restoreFromSimulation()
    3. Inventory restoration: If snapshot inventory empty, pulls from curios.dat NBT
    4. Curios restoration: Applies neoforge:attachments from curios.dat
    5. Cleanup: Deletes .dat files after successful restoration
  - **Both paths (normal + crash)** fully restore player state to original location
- **No Complex State Machine**: Simplified from chamber occupancy tracking to direct position-saving

### Activation Core Block Design
- **Horizontal-Only Placement**: getStateForPlacement() forces NORTH default if player looking up/down
- **Create Wrench Integration**: Right-click cycles N→E→S→W, shift+click adds to inventory
- **Proper Occlusion**: Empty occlusion shape prevents culling of adjacent block faces

### Energy System
- Uses Minecraft's standard Forge Energy (FE) capability
- Create mod cables can connect and transfer power
- Configurable costs: 100,000 FE capacity, 2,000 FE per activation, 500 FE/tick max transfer
- Block entities persist energy across disconnect/reconnect via NBT save/load

### Plot System
- Currently **100x100 blocks** for testing (configurable)
- Glowing particle borders for visualization
- Invisible barrier system prevents boundary crossing
- Block break/place prevention outside plots

### Security Architecture (Multi-Phase)
**Phase 1 - Teleportation Control**:
- Blocks all cross-dimensional teleports from simulation EXCEPT legitimate exits (validated via saved state)
- Block entity resync after dimension change (prevents client-side spoofing)
- Exploit item blacklist: quantum singularities, flux plugs, waystones, command blocks, etc.

**Phase 2 - Placement Blocking**:
- Prevents Waystone placement in simulation
- Prevents Chunkloader placement (FTB Chunks, Chunkloaders mod)
- Prevents Immersive Portals dimensional portals
- User-friendly error messages

**Phase 3 - (Future)**:
- Coordinate overflow validation
- Comprehensive logging
- Dimension ACL system

### Exit Process Safety (Fully Backed Up)
- **Entry**: Player state captured to memory AND disk immediately
- **Normal Exit (Beacon/Debug)**: Restores from in-memory snapshot with full data
- **Crash Exit (Server down)**: Restores from disk on rejoin, pulls inventory from backup NBT
- **File Cleanup**: Persisted state deleted after successful restoration
- **Result**: No way to get stuck in simulation - always get brought home with full inventory/state

## Known Issues / TODO

### Completed (No Open Issues)
✅ Teleportation system stable and robust
✅ Inventory restoration working (full 36 slots)
✅ Curios restoration working (backed up and restored with attachments)
✅ Player state survives disconnect/reconnect
✅ Player state persists across server restart/crash
✅ Activation Core directional placement working
✅ Create wrench support working (rotate & pickup)
✅ Block culling fixed on adjacent blocks
✅ FE status properly displayed in Lua script

### Phase 3 - Future Security Hardening
1. **Coordinate Overflow Validation** - Validate position values are within safe bounds
2. **Comprehensive Logging** - Track all exploit prevention events
3. **Dimension ACL System** - More granular dimension access control

### Polish & Optional Features
1. **Block Models** - Replace placeholder purple/black checkerboard with proper textures
2. **Particle Effects** - Teleportation effects, chamber activation effects
3. **Admin Tools** - Plot management, player teleportation tools
4. **Performance Optimization** - Profile and optimize for large player counts
5. **Fog System** - Custom fog rendering for plot boundaries (currently vanilla fog)

## Testing Checklist

### Core Functionality ✅
- ✅ Build completes successfully
- ✅ Mod loads without errors
- ✅ Dimension creates and generates properly
- ✅ Checkerboard pattern generates correctly
- ✅ Activation Core block places and faces correct direction
- ✅ Create wrench rotates block (N→E→S→W only, no vertical)
- ✅ Create wrench picks up block to inventory
- ✅ Breaking block in survival drops item

### Teleportation ✅
- ✅ Player can enter simulation via chamber (energy required)
- ✅ Player teleports to stasis dimension correctly
- ✅ Player position saved on entry
- ✅ Player restored to original location on exit
- ✅ Exit via beacon works reliably
- ✅ Exit via debug command works reliably
- ✅ Darkness effect shown on exit

### Player State ✅
- ✅ Inventory restored on exit (all 36 slots)
- ✅ Curios restored on exit (backed up and reapplied)
- ✅ Health/hunger/XP restored on exit
- ✅ Effects/potion effects restored on exit
- ✅ Gamemode restored on exit
- ✅ State survives normal disconnect/reconnect (memory + disk)
- ✅ State survives server restart (disk persistence)
- ✅ State survives server crash (disk persistence with auto-recovery)
- ✅ State files auto-cleaned after restoration

### Security ✅
- ✅ Cannot teleport out via Waystones
- ✅ Cannot teleport out via enderpearls
- ✅ Cannot place Waystones in simulation
- ✅ Cannot place chunkloaders in simulation
- ✅ Cannot place portals in simulation
- ✅ Exploit items removed on exit (quantum singularities, flux plugs, etc.)

### Plot System ✅
- ✅ Plot borders show as particle lines
- ✅ Player cannot break blocks outside plot
- ✅ Player cannot place blocks outside plot
- ✅ Player cannot cross plot boundaries (barrier)

### Integration ✅
- ✅ Create mechanical crafting recipe works
- ✅ CC:Tweaked script loads and runs
- ✅ FE status displays correctly in Lua script
- ✅ FE power check gates chamber entry
- ✅ Configuration system works (stasismod-common.toml created)
- ✅ Config values can be edited by players
- ✅ Config changes persist across restarts

## Next Steps

1. **Phase 3 Security Hardening** - Coordinate validation, ACL system, comprehensive logging
2. **Alpha Testing** - Pentest with user's brother using testing checklists
3. **Polish & Models** - Replace placeholder block textures
4. **Performance Testing** - Profile with multiple concurrent players
5. **Optional: Admin Tools** - Plot management, teleportation utilities

## Current Configuration

### Energy (FE) Settings
- **Chamber Capacity**: 100,000 FE (configurable)
- **Activation Cost**: 2,000 FE per entry (configurable)
- **Max Transfer Rate**: 500 FE/tick (in/out, configurable)

### Configuration System (Server-Side Only)
- **Config File**: `stasismod-server.toml` (server-only, stored in world save directory)
- **Config Type**: SERVER (clients cannot edit without server admin tools)
- **Config Classes**:
  - `ServerConfig.java` - NeoForge ConfigSpec with ranges and comments
  - `ConfigCache.java` - Safe runtime access with defaults, reloads on config change
  - `Config.java` - Event listener that syncs ConfigSpec → ConfigCache
- **Used By**:
  - `ChamberEnergyHandler` - Gets FE values from ConfigCache
  - `ChamberTile` - Displays config values to players
  - `ChamberPeripheral` - Exposes values to CC:Tweaked Lua scripts
- **How It Works**:
  1. On server startup, config file created from ConfigSpec in `world/config/stasismod-server.toml`
  2. On config load/reload: ConfigEvents.onReload() calls ConfigCache.reload()
  3. Cache syncs with ConfigSpec values
  4. Block entities use ConfigCache methods (getChamberCapacity(), getActivationCost(), getMaxTransferRate())
  5. Only server admin can edit - client changes are ignored
  6. Values persist across restarts

### Block Configuration
- **Activation Core**:
  - Horizontal placement only (N/E/S/W)
  - Create wrench compatible (rotate & pickup)
  - Drop item when broken in survival
  - Block entity ticker every server tick
  - No occlusion culling on adjacent blocks

### Simulation Dimension
- **World Type**: Checkerboard pattern
- **Building Area**: Y=0 to Y=255 (full height)
- **Pattern**: 3D checkerboard (white concrete + snow blocks, Y=0-63)
- **Mob Spawning**: Disabled
- **Daylight Cycle**: Frozen at noon
- **Weather**: Disabled

### Security Exploit Item Blacklist
- AE2: quantum singularities
- Flux Networks: flux plugs/points/cores
- Waystones: waystone blocks and warp scrolls
- Immersive Portals: end frames
- Chunkloaders: various chunkloader blocks
- Draconic Evolution: warp cores
- Vanilla: command blocks, structure blocks

## Build Command
```bash
./gradlew build -x test
```

## Run Dev Client
```bash
./gradlew runClient
```

## Files Modified This Session

### Core Implementation
- `src/main/java/com/shane/stasismod/player/PlayerStateManager.java` - Complete rewrite for direct field manipulation
- `src/main/java/com/shane/stasismod/blockentity/ChamberTile.java` - Simplified state machine
- `src/main/java/com/shane/stasismod/block/ActivationCoreBlock.java` - Directional placement, wrench support
- `src/main/java/com/shane/stasismod/event/ServerEvents.java` - All event handlers (teleport blocking, exploit prevention, wrench support)
- `src/main/java/com/shane/stasismod/config/ServerConfig.java` - FE settings configuration

### Data & Resources
- `src/main/resources/data/stasismod/recipes/activation_core.json` - Create mechanical crafting recipe
- `src/main/resources/data/computercraft/lua/rom/programs/stasis.lua` - CC:Tweaked interface script
- `src/main/resources/assets/stasismod/blockstates/activation_core.json` - Block rotation variants

### Version & Dependencies
- `gradle.properties` - Version 0.0.1 (updated from 1.0.0)
- `neoforge.mods.toml` - Clean dependencies (Create, Curios, CC:Tweaked, JEI)
- `en_us.json` - Cleaned up translations

## Git Worktree Isolation
Agent work uses `isolation: "worktree"` to avoid conflicts when modifying files in parallel.
