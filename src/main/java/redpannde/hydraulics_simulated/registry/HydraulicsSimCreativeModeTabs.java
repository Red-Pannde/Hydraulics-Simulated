package redpannde.hydraulics_simulated.registry;

import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import redpannde.hydraulics_simulated.HydraulicsSimulated;

import java.util.ArrayList;
import java.util.List;

public class HydraulicsSimCreativeModeTabs {
    private static final CreativeModeTab REGISTER = HydraulicsSimulated.TAB;

    public void init() {
    }

    public static class RegistrateDisplayItemsGenerator implements CreativeModeTab.DisplayItemsGenerator {
        public RegistrateDisplayItemsGenerator(boolean b, CreativeModeTab baseCreativeTab) {
        }

        @Override
        public void accept(CreativeModeTab.@NotNull ItemDisplayParameters itemDisplayParameters, CreativeModeTab.Output output) {
            List<Item> items = collectItems();
            acceptAll(output, items);

        }

        public List<Item> collectItems() {
            List<Item> items = new ArrayList<>();
            for (RegistryEntry<Item, Item> entry : HydraulicsSimulated.getRegistrate().getAll(Registries.ITEM)) {
                items.add(entry.get());
            }
            return items;
        }

        public void acceptAll(CreativeModeTab.Output output, List<Item> items) {
            for (Item item : items) {
                output.accept(item);
            }
        }
    }


}
