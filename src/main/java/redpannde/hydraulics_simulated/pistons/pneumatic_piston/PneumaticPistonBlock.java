package redpannde.hydraulics_simulated.pistons.pneumatic_piston;

import com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.foundation.block.IBE;
import dev.simulated_team.simulated.util.extra_kinetics.ExtraKinetics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import redpannde.hydraulics_simulated.pistons.AbstractPistonBlock;
import redpannde.hydraulics_simulated.registry.HydraulicsSimBlockEntities;

public class PneumaticPistonBlock extends AbstractPistonBlock implements IBE<PneumaticPistonBlockEntity>, ExtraKinetics.ExtraKineticsBlock {
    public PneumaticPistonBlock(Properties properties) {
        super(properties);
    }

    public static final BooleanProperty AXIS_ALONG_FIRST_COORDINATE = DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return state.getValue(FACING)
                .getAxis() == face.getAxis();
    }
    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING)
                .getAxis();
    }

    @Override
    public Class<PneumaticPistonBlockEntity> getBlockEntityClass() {
        return PneumaticPistonBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PneumaticPistonBlockEntity> getBlockEntityType() {
        return HydraulicsSimBlockEntities.PNEUMATIC_PISTON_BLOCK_ENTITY.get();
    }

    @Override
    public IRotate getExtraKineticsRotationConfiguration() {
        return PneumaticPistonBlockEntity.PneumaticPistonShaftBlockEntity.EXTRA_SHAFT_CONFIG;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS_ALONG_FIRST_COORDINATE);
        super.createBlockStateDefinition(builder);
    }


}
