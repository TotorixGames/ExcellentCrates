package de.kalypzo.excrextension.craftengine;

import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.bukkit.item.BukkitItemDefinition;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentcrates.CratesPlugin;
import su.nightexpress.nightcore.integration.item.adapter.IdentifiableItemAdapter;
import su.nightexpress.nightcore.integration.item.data.ItemIdData;

/**
 * Item adapter for CraftEngine (net.momirealms).
 * <p>
 * Nightcore ships its own {@code CraftEngineAdapter}, but it is compiled against the removed
 * {@code core.item.CustomItem} API and blows up with a {@link NoSuchMethodError} as soon as an item
 * has to be built. This adapter is registered under the very same name and therefore replaces it in
 * the {@link su.nightexpress.nightcore.integration.item.ItemBridge} registry.
 */
public class CraftEngineItemAdapter extends IdentifiableItemAdapter {

    public static final String NAME = "craftengine";

    private final CratesPlugin plugin;

    private boolean apiErrorReported;

    public CraftEngineItemAdapter(@NotNull CratesPlugin plugin) {
        super(NAME);
        this.plugin = plugin;
    }

    @Override
    @Nullable
    public String getItemId(@NotNull ItemStack itemStack) {
        try {
            Key itemId = CraftEngineItems.getCustomItemId(itemStack);
            return itemId == null ? null : itemId.asString();
        }
        catch (Throwable error) {
            this.reportApiError("CraftEngineItems#getCustomItemId", error);
            return null;
        }
    }

    @Override
    @Nullable
    public ItemStack createItem(@NotNull String id) {
        try {
            // String overload also resolves plain paths (without namespace).
            BukkitItemDefinition definition = CraftEngineItems.byId(id);
            return definition == null ? null : definition.buildBukkitItem();
        }
        catch (Throwable error) {
            this.reportApiError("CraftEngineItems#byId", error);
            return null;
        }
    }

    @Override
    public boolean canHandle(@NotNull ItemStack itemStack) {
        try {
            return CraftEngineItems.isCustomItem(itemStack);
        }
        catch (Throwable error) {
            this.reportApiError("CraftEngineItems#isCustomItem", error);
            return false;
        }
    }

    @Override
    public boolean canHandle(@NotNull ItemIdData data) {
        try {
            return CraftEngineItems.byId(data.getItemId()) != null;
        }
        catch (Throwable error) {
            this.reportApiError("CraftEngineItems#byId", error);
            return false;
        }
    }

    /**
     * CraftEngine throws while its item manager is not ready yet, and a future API break must degrade
     * to placeholder items instead of tearing down the menu or dialog that asked for the item.
     */
    private void reportApiError(@NotNull String method, @NotNull Throwable error) {
        if (this.apiErrorReported) return;
        this.apiErrorReported = true;

        this.plugin.warn("CraftEngine integration: " + method + " failed, custom items will fall back to placeholders. Check your CraftEngine version.", error);
    }
}
