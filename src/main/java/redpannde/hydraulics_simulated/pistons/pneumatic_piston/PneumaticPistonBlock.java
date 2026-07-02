package redpannde.hydraulics_simulated.pistons.pneumatic_piston;

import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import redpannde.hydraulics_simulated.pistons.AbstractPistonBlock;
import redpannde.hydraulics_simulated.registry.HydraulicsSimBlockEntities;

public class PneumaticPistonBlock extends AbstractPistonBlock implements IBE<PneumaticPistonBlockEntity> {
    public PneumaticPistonBlock(Properties properties) {
        super(properties);
    }

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


}
