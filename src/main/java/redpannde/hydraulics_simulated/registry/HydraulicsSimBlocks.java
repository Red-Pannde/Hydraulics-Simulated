package redpannde.hydraulics_simulated.registry;

import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import dev.simulated_team.simulated.content.blocks.swivel_bearing.link_block.SwivelBearingPlateBlock;
import net.minecraft.tags.BlockTags;
import redpannde.hydraulics_simulated.HydraulicsSimulated;
import redpannde.hydraulics_simulated.pistons.pneumatic_piston.PneumaticPistonBlock;
import redpannde.hydraulics_simulated.pistons.plate.AbstractPistonPlateBlock;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;

public class HydraulicsSimBlocks {
    private static final HydraulicsSimRegistrate REGISTRATE = HydraulicsSimulated.getRegistrate();

    public static void init() {

    }

    public static final BlockEntry<PneumaticPistonBlock> PNEUMATIC_PISTON_BLOCK =
            REGISTRATE.block("pneumatic_piston", PneumaticPistonBlock::new)
                    .item()
                    .transform(customItemModel())
                    .register();

    public static final BlockEntry<AbstractPistonPlateBlock> PISTON_LINK_BLOCK =
            REGISTRATE.block("swivel_bearing_link_block", AbstractPistonPlateBlock::new)
                    /* .blockstate((ctx, prov) ->
                            prov.directionalBlock(ctx.getEntry(), blockState -> prov.models().getExistingFile(prov.modLoc("block/piston/piston_plate"))))

                     */
                    .initialProperties(SharedProperties::netheriteMetal)
                    .properties(properties -> properties
                            .destroyTime(5f))
                    .loot((p, b) -> p.dropOther(b, PNEUMATIC_PISTON_BLOCK.get()))
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .register();
}
