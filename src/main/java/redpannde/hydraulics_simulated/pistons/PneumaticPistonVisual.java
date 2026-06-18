package redpannde.hydraulics_simulated.pistons;

import com.simibubi.create.content.kinetics.base.OrientedRotatingVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import dev.simulated_team.simulated.index.SimPartialModels;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import redpannde.hydraulics_simulated.pistons.pneumatic_piston.PneumaticPistonBlockEntity;

public class PneumaticPistonVisual extends OrientedRotatingVisual<PneumaticPistonBlockEntity> {
    /**
     * @param context
     * @param blockEntity
     * @param partialTick    The model to spin.
     */
    public PneumaticPistonVisual(VisualizationContext context, PneumaticPistonBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick, Direction.SOUTH, blockEntity.getBlockState().getValue(BlockStateProperties.FACING).getOpposite(), Models.partial(SimPartialModels.SHAFT_SIXTEENTH));
    }

}
