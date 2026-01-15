package com.example.fastermining;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemTool;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemToolSpec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.Map;

public class FasterMining extends JavaPlugin {

    private static FasterMining instance;
    private float miningSpeedMultiplier = 2.0f;
    private int modifiedToolsCount = 0;
    private int modifiedSpecsCount = 0;

    public FasterMining(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
        getLogger().at(Level.INFO).log("Faster Mining loaded!");
    }

    @Override
    protected void setup() {
        getLogger().at(Level.INFO).log("Setting up plugin...");
    }

    @Override
    protected void start() {
        getLogger().at(Level.INFO).log("===========================================");
        getLogger().at(Level.INFO).log("FASTER MINING ENABLED!");
        getLogger().at(Level.INFO).log("Multiplier: %.1fx", miningSpeedMultiplier);
        getLogger().at(Level.INFO).log("===========================================");

        modifyAllTools();

        getLogger().at(Level.INFO).log("===========================================");
        getLogger().at(Level.INFO).log("✓ MODIFICATION COMPLETE!");
        getLogger().at(Level.INFO).log("  Tools modified: %d", modifiedToolsCount);
        getLogger().at(Level.INFO).log("  Specs modified: %d", modifiedSpecsCount);
        getLogger().at(Level.INFO).log("===========================================");
    }

    @Override
    protected void shutdown() {
        getLogger().at(Level.INFO).log("Faster Mining disabled!");
        getLogger().at(Level.INFO).log("Stats: %d tools, %d specs", modifiedToolsCount, modifiedSpecsCount);
    }

    /**
     * Modifies all tools in the game
     */
    private void modifyAllTools() {
        getLogger().at(Level.INFO).log("Scanning items...");

        try {
            DefaultAssetMap<String, Item> itemMap = Item.getAssetMap();

            if (itemMap == null) {
                getLogger().at(Level.SEVERE).log("❌ Unable to retrieve item map!");
                return;
            }

            Map<String, Item> assets = itemMap.getAssetMap();
            int totalItems = assets.size();

            for (Item item : assets.values()) {
                ItemTool tool = item.getTool();

                if (tool != null) {
                    modifyTool(item, tool);
                }
            }

            getLogger().at(Level.INFO).log("Items scanned: %d", totalItems);

        } catch (Exception e) {
            getLogger().at(Level.SEVERE).log("❌ ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Modifies a specific tool
     */
    private void modifyTool(Item item, ItemTool tool) {
        try {
            ItemToolSpec[] specs = tool.getSpecs();

            if (specs == null || specs.length == 0) {
                return;
            }

            boolean itemModified = false;

            // Modify each spec of the tool
            for (ItemToolSpec spec : specs) {
                if (modifyToolSpec(spec)) {
                    modifiedSpecsCount++;
                    itemModified = true;
                }
            }

            if (itemModified) {
                modifiedToolsCount++;

                // Detailed log for the first 3 tools
                if (modifiedToolsCount <= 3) {
                    getLogger().at(Level.INFO).log("✓ Tool modified: %s (%d specs)",
                            item.getId(), specs.length);
                }
            }

        } catch (Exception e) {
            getLogger().at(Level.SEVERE).log("❌ Error on %s: %s",
                    item.getId(), e.getMessage());
        }
    }

    /**
     * Modifies an ItemToolSpec (the actual mining power)
     */
    private boolean modifyToolSpec(ItemToolSpec spec) {
        try {
            float originalPower = spec.getPower();

            // Only modify if power > 0
            if (originalPower <= 0) {
                return false;
            }

            float newPower = originalPower * miningSpeedMultiplier;

            // Use reflection to modify the 'power' field
            Field powerField = ItemToolSpec.class.getDeclaredField("power");
            powerField.setAccessible(true);
            powerField.set(spec, newPower);

            // Log for the first 3
            if (modifiedSpecsCount < 3) {
                getLogger().at(Level.INFO).log("  Power: %.2f → %.2f (x%.1f)",
                        originalPower, newPower, miningSpeedMultiplier);
            }

            return true;

        } catch (NoSuchFieldException e) {
            getLogger().at(Level.SEVERE).log("❌ Field 'power' not found!");
            return false;
        } catch (IllegalAccessException e) {
            getLogger().at(Level.SEVERE).log("❌ Cannot access 'power' field!");
            return false;
        }
    }

    /**
     * Reloads tools with a new multiplier
     */
    public void reloadWithNewMultiplier(float newMultiplier) {
        if (newMultiplier <= 0) {
            getLogger().at(Level.WARNING).log("Multiplier must be > 0!");
            return;
        }

        getLogger().at(Level.INFO).log("Reloading with x%.1f...", newMultiplier);
        this.miningSpeedMultiplier = newMultiplier;
        this.modifiedToolsCount = 0;
        this.modifiedSpecsCount = 0;
        modifyAllTools();
    }

    // Getters
    public static FasterMining getInstance() {
        return instance;
    }

    public float getMiningSpeedMultiplier() {
        return miningSpeedMultiplier;
    }

    public int getModifiedToolsCount() {
        return modifiedToolsCount;
    }

    public int getModifiedSpecsCount() {
        return modifiedSpecsCount;
    }
}