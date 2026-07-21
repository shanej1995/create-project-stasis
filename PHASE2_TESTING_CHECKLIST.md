# Phase 2 Security Testing Checklist

**Date Started**: 2026-07-19  
**Tester(s)**: Shane + Brother (Alpha Pentest)  
**Status**: Ready for Testing

---

## Phase 2 Test Cases

### Block 1: Waystone Blocking

#### Test 2.1: Waystone Placement Blocked
- [ ] Enter simulation dimension
- [ ] Try to place Waystone block
- **Expected Result**: Placement canceled, message: "Waystones cannot be placed in the simulation dimension"
- **Actual Result**: _________________
- **Status**: ⬜ PASS / ⬜ FAIL
- **Server Log Shows**: _________________

#### Test 2.2: All Waystone Variants Blocked
- [ ] Try to place Waystone (regular)
- [ ] Try to place Waystone Rusty
- [ ] Try to place Waystone Sandy
- [ ] Try to place Waystone Mossy
- [ ] Try to place Waystone Deepslate
- **Expected Result**: All variants blocked
- **Actual Result**: _________________
- **Status**: ⬜ PASS / ⬜ FAIL

---

### Block 2: Chunkloader Blocking

#### Test 2.3: Chunkloader Placement Blocked
- [ ] Enter simulation dimension
- [ ] Try to place FTB Chunks Chunk Loader
- **Expected Result**: Placement canceled, message: "Chunk loaders cannot operate in the simulation dimension"
- **Actual Result**: _________________
- **Status**: ⬜ PASS / ⬜ FAIL
- **Server Log Shows**: _________________

#### Test 2.4: Other Chunkloader Variants
- [ ] Try to place Chunkloaders mod chunk loader
- **Expected Result**: Blocked
- **Actual Result**: _________________
- **Status**: ⬜ PASS / ⬜ FAIL

---

### Block 3: Dimensional Portal Blocking

#### Test 2.5: Immersive Portals Frame Blocked
- [ ] Enter simulation dimension
- [ ] Try to place End Frame (Immersive Portals)
- **Expected Result**: Placement canceled, message: "Dimensional portals cannot be created in the simulation dimension"
- **Actual Result**: _________________
- **Status**: ⬜ PASS / ⬜ FAIL
- **Server Log Shows**: _________________

---

### Block 4: Edge Cases

#### Test 2.6: Placing in Creative Mode
- [ ] Enter simulation in creative mode
- [ ] Try to place Waystone block in creative mode
- **Expected Result**: Still blocked
- **Actual Result**: _________________
- **Status**: ⬜ PASS / ⬜ FAIL

#### Test 2.7: Placing Outside Simulation
- [ ] Return to overworld
- [ ] Place Waystone, Chunkloader, Portal frame in overworld
- **Expected Result**: All placement succeeds
- **Actual Result**: _________________
- **Status**: ⬜ PASS / ⬜ FAIL

---

## Summary

**Total Tests Passed**: ______ / 7  
**Total Tests Failed**: ______ / 7  

### Pass Rate: ______%

---

## Notes from Pentest

**Additional Exploit Attempts Made**:
```
[Document any creative ways tested to bypass Phase 2 blocking]
```

**Results**:
```
[Document what happened when bypasses were attempted]
```

---

## Sign-Off

- Tester 1: _________________ Date: _________________
- Tester 2: _________________ Date: _________________

