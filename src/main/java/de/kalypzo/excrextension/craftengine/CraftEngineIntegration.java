package de.kalypzo.excrextension.craftengine;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentcrates.CratesPlugin;
import su.nightexpress.nightcore.bridge.item.ItemAdapter;
import su.nightexpress.nightcore.integration.item.ItemBridge;
import su.nightexpress.nightcore.integration.item.ItemPlugins;
import su.nightexpress.nightcore.integration.item.adapter.IdentifiableItemAdapter;
import su.nightexpress.nightcore.util.Plugins;

/**
 * Registers {@link CraftEngineItemAdapter} in nightcore's item bridge.
 * <p>
 * The field is typed as {@link IdentifiableItemAdapter} and the adapter is only instantiated behind the
 * plugin check on purpose, so that no {@code net.momirealms} class is ever resolved on servers without
 * CraftEngine.
 */
public class CraftEngineIntegration {

    private static IdentifiableItemAdapter adapter;

    public static void setup(@NotNull CratesPlugin plugin) {
        adapter = null;

        if (!Plugins.isInstalled(ItemPlugins.CRAFT_ENGINE)) return;

        IdentifiableItemAdapter created = new CraftEngineItemAdapter(plugin);
        ItemBridge.register(created);
        adapter = created;

        plugin.info("Registered CraftEngine item adapter '" + CraftEngineItemAdapter.NAME + "' (replaces the outdated nightcore one).");
    }

    /**
     * A nightcore reload wipes the whole adapter registry and only restores nightcore's own adapters,
     * which would bring the broken CraftEngine adapter back. Re-register ours whenever it went missing.
     */
    public static void ensureRegistered() {
        IdentifiableItemAdapter current = adapter;
        if (current == null) return;

        ItemAdapter<?> registered = ItemBridge.getAdapter(CraftEngineItemAdapter.NAME);
        if (registered != current) {
            ItemBridge.register(current);
        }
    }

    public static void clear() {
        adapter = null;
    }
}
