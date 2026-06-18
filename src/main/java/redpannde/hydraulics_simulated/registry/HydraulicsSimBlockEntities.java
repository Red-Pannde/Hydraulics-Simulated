package redpannde.hydraulics_simulated.registry;

import com.simibubi.create.content.kinetics.base.OrientedRotatingVisual;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import dev.simulated_team.simulated.index.SimPartialModels;
import redpannde.hydraulics_simulated.HydraulicsSimulated;
import redpannde.hydraulics_simulated.pistons.pneumatic_piston.PneumaticPistonBlockEntity;
import redpannde.hydraulics_simulated.pistons.PneumaticPistonRenderer;
import redpannde.hydraulics_simulated.pistons.PneumaticPistonVisual;
import redpannde.hydraulics_simulated.pistons.pneumatic_piston.plate.AbstractPistonPlateBlockEntity;
import redpannde.hydraulics_simulated.pistons.pneumatic_piston.plate.AbstractPistonPlateBlockRenderer;

public class HydraulicsSimBlockEntities {

    private static final HydraulicsSimRegistrate REGISTRATE = HydraulicsSimulated.getRegistrate();

    public static final BlockEntityEntry<PneumaticPistonBlockEntity> PNEUMATIC_PISTON_BLOCK_ENTITY = REGISTRATE
            .blockEntity("pneumatic_piston", PneumaticPistonBlockEntity::new)
            .visual(() -> PneumaticPistonVisual::new)
            .validBlock(HydraulicsSimBlocks.PNEUMATIC_PISTON_BLOCK)
            .renderer(() -> PneumaticPistonRenderer::new)
            .register();

    public static final BlockEntityEntry<AbstractPistonPlateBlockEntity> PISTON_LINK_BLOCK_ENTITY = REGISTRATE
            .blockEntity("piston_link_block", AbstractPistonPlateBlockEntity::new)
            .visual(() -> OrientedRotatingVisual.of(SimPartialModels.SHAFT_SIXTEENTH))
            .validBlocks(HydraulicsSimBlocks.PISTON_LINK_BLOCK)
            .renderer(() -> AbstractPistonPlateBlockRenderer::new)
            .register();



    public static void init() {

    }
}
