# Phase 1 Security Testing Checklist

**Date Started**: 2026-07-19  
**Tester(s)**: Shane + Brother (Alpha Pentest)  
**Status**: Ready for Testing

---

## Test Environment Setup

- [ ] Build compiled for alpha version
- [ ] Modpack contains Waystones mod
- [ ] Modpack contains Flux Networks mod
- [ ] Modpack contains AE2 (Applied Energistics 2) mod
- [ ] Modpack contains Draconic Evolution mod
- [ ] Main world loaded and tested

---

## Phase 1 Test Cases

### Block 1: Teleportation Escape Prevention

#### Test 1.1: Waystone Escape
- [ ] Enter simulation dimension
- [ ] Create/place a Waystone in simulation
- [ ] Create/place a Waystone in overworld
- [ ] Try to teleport from simulation to overworld Waystone
- **Expected Result**: Teleport blocked, player stays in simulation
- **Actual Result**: _________________
- **Status**: ⬜ PASS / ⬜ FAIL

#### Test 1.2: Draconic Warp Escape
- [ ] Enter simulation dimension
- [ ] Set a Draconic Warp point in simulation
- [ ] Set a Draconic Warp point in overworld
- [ ] Try to warp from simulation to overworld warp point
- **Expected Result**: Warp blocked, player stays in simulation
- **Actual Result**: _________________
- **Status**: ⬜ PASS / ⬜ FAIL

#### Test 1.3: Command Teleport
- [ ] Enter simulation dimension
- [ ] Try `/tp @s <overworld coords>` to teleport to overworld
- **Expected Result**: Command blocked, player stays in simulation
- **Actual Result**: _________________
- **Status**: ⬜ PASS / ⬜ FAIL

#### Test 1.4: Enderpearl Escape
- [ ] Enter simulation dimension
- [ ] Throw enderpearl upward or to distant location
- **Expected Result**: Can move around sim, but can't escape dimension
- **Actual Result**: _________________
- **Status**: ⬜ PASS / ⬜ FAIL

---

### Block 2: Item Removal on Exit

#### Test 2.1: Quantum Singularity Removal
- [ ] Enter simulation dimension
- [ ] Craft or obtain Quantum Singularity (AE2)
- [ ] Check inventory contains singularity
- [ ] Exit simulation dimension
- [ ] Check inventory in overworld
- **Expected Result**: Quantum singularity is gone
- **Actual Result**: _________________
- **Status**: ⬜ PASS / ⬜ FAIL
- **Server Log Shows**: _________________

#### Test 2.2: Flux Plug Removal
- [ ] Enter simulation dimension
- [ ] Craft or obtain Flux Plug (Flux Networks)
- [ ] Check inventory contains flux plug
- [ ] Exit simulation dimension
- [ ] Check inventory in overworld
- **Expected Result**: Flux plug is gone
- **Actual Result**: _________________
- **Status**: ⬜ PASS / ⬜ FAIL

#### Test 2.3: Waystone Block Removal
- [ ] Enter simulation dimension
- [ ] Place Waystone block in inventory/inventory
- [ ] Exit simulation dimension
- [ ] Check inventory in overworld
- **Expected Result**: Waystone block is gone
- **Actual Result**: _________________
- **Status**: ⬜ PASS / ⬜ FAIL

#### Test 2.4: Draconic Warp Core Removal
- [ ] Enter simulation dimension
- [ ] Craft or obtain Draconic Warp Core
- [ ] Check inventory contains warp core
- [ ] Exit simulation dimension
- [ ] Check inventory in overworld
- **Expected Result**: Warp core is gone
- **Actual Result**: _________________
- **Status**: ⬜ PASS / ⬜ FAIL

---

### Block 3: Cross-Dimensional Power/Item Transfer Prevention

#### Test 3.1: Flux Network Isolation
- [ ] Create Flux Network in overworld
- [ ] Enter simulation dimension
- [ ] Try to connect Flux plug to same network from simulation
- **Expected Result**: Cannot connect to overworld network
- **Actual Result**: _________________
- **Status**: ⬜ PASS / ⬜ FAIL

#### Test 3.2: AE2 Quantum Bridge Isolation
- [ ] Create AE2 network in overworld
- [ ] Enter simulation dimension
- [ ] Try to create Quantum Bridge linking to overworld network
- **Expected Result**: Cannot create cross-dimensional bridge
- **Actual Result**: _________________
- **Status**: ⬜ PASS / ⬜ FAIL

---

### Block 4: Client-Side Entity Resync

#### Test 4.1: Block Entity Resync After Exit
- [ ] Enter simulation dimension
- [ ] Place several block entities (machines, storage, etc)
- [ ] Exit simulation dimension
- [ ] Re-enter simulation dimension
- [ ] Check if block entities show correct state
- **Expected Result**: Block entities display correct, non-stale data
- **Actual Result**: _________________
- **Status**: ⬜ PASS / ⬜ FAIL

---

## Additional Observations

### Server Logs - Security Events
- [ ] Check for "SECURITY:" log messages
- [ ] Verify dimension travel blocks are logged
- [ ] Verify item removals are logged

**Log Entries Found**:
```
[paste relevant security log entries here]
```

---

### Performance Impact
- [ ] No noticeable FPS drop on exit
- [ ] No server TPS lag when exiting
- [ ] Block entity resync doesn't cause stutter

**Performance Notes**: _________________

---

### Edge Cases Discovered

#### Edge Case 1
- **Description**: _________________
- **Reproduction**: _________________
- **Result**: _________________
- **Status**: ⬜ Needs Phase 2 / ⬜ Already Handled

#### Edge Case 2
- **Description**: _________________
- **Reproduction**: _________________
- **Result**: _________________
- **Status**: ⬜ Needs Phase 2 / ⬜ Already Handled

---

## Summary

**Total Tests Passed**: ______ / 14  
**Total Tests Failed**: ______ / 14  

### Pass Rate: ______%

---

## Recommendations for Phase 2

Based on testing, Phase 2 should prioritize:
1. _________________
2. _________________
3. _________________

---

## Notes from Pentest

**Date Tested**: _________________  
**Testers**: Shane + Brother  
**Duration**: _________________  

**General Feedback**:
```
[Add any general observations, exploit attempts, or security concerns discovered during pentest]
```

---

## Sign-Off

- Tester 1: _________________ Date: _________________
- Tester 2: _________________ Date: _________________

