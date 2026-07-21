-- Stasis Simulation Entry Program
-- Interfaces with the Stasis Chamber peripheral via CC: Tweaked

-- Find the chamber peripheral (should be on the block below the computer)
local chamber = peripheral.find("stasis_chamber")

if not chamber then
    print("Error: No stasis chamber peripheral found!")
    print("Make sure the computer is placed on top of the chamber.")
    return
end

-- Get FE settings
local activationCost = chamber.getActivationCost()
local current, max = chamber.getEnergy()

-- Main loop - keep running until player teleports
while true do
    -- Display menu
    term.clear()
    term.setCursorPos(1, 1)

    print("=" .. string.rep("=", 38) .. "=")
    print("  STASIS SIMULATION CHAMBER INTERFACE")
    print("=" .. string.rep("=", 38) .. "=")
    print()

    -- Display power level and settings
    current, max = chamber.getEnergy()
    local percent = math.floor((current / max) * 100)
    print("Power:       " .. current .. "/" .. max .. " FE (" .. percent .. "%)")
    print("Activation:  " .. activationCost .. " FE")
    print()

    -- Check if ready
    if current >= activationCost then
        print("Chamber: Ready for entry")
    else
        print("Chamber: Insufficient power (" .. (activationCost - current) .. " FE needed)")
    end
    print()

    print("[E] - Enter Simulation")
    print("[Q] - Quit")
    print()
    print("Initializing neural interface...")

    local event, key = os.pullEvent("key")

    if key == keys.e then
        print()
        print("Engaging consciousness transfer protocol...")
        sleep(0.5)
        print("Synchronizing with quantum entanglement matrices...")
        sleep(0.5)
        print("Commencing neural digitization...")
        sleep(0.5)

        -- Call the chamber's startSimulation method
        local success, message = chamber.startSimulation()

        if success then
            print()
            print("Transfer complete. Welcome to the simulation.")
            sleep(2)
            return
        else
            print()
            print("Failed to start simulation: " .. tostring(message))
            sleep(2)
        end
    elseif key == keys.q then
        print()
        print("Interface disengaged.")
        sleep(1)
        return
    end
    -- Any other key is ignored, loop continues
end
