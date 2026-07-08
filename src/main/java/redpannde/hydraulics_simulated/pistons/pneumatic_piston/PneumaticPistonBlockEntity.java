package redpannde.hydraulics_simulated.pistons.pneumatic_piston;

import com.simibubi.create.content.kinetics.base.*;
import com.simibubi.create.content.kinetics.transmission.sequencer.SequencerInstructions;
import dev.simulated_team.simulated.data.SimLang;
import dev.simulated_team.simulated.util.extra_kinetics.ExtraBlockPos;
import dev.simulated_team.simulated.util.extra_kinetics.ExtraKinetics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;
import redpannde.hydraulics_simulated.pistons.AbstractPistonBlockEntity;

import java.util.List;

public class PneumaticPistonBlockEntity extends AbstractPistonBlockEntity implements ExtraKinetics{

    private final PneumaticPistonShaftBlockEntity shaft;

    public PneumaticPistonBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);

        this.shaft = new PneumaticPistonShaftBlockEntity(typeIn, pos, state, this);
    }

    @Override
    protected float getExtensionSpeed() {
        return this.shaft.getSpeed();
    }


    @Override
    public @NotNull KineticBlockEntity getExtraKinetics() {
        return this.shaft;
    }

    @Override
    public boolean shouldConnectExtraKinetics() {
        return false;
    }



    public static class PneumaticPistonShaftBlockEntity extends KineticBlockEntity implements ExtraKineticsBlockEntity {

        public static final IRotate EXTRA_SHAFT_CONFIG = new IRotate() {

            public static final BooleanProperty AXIS_ALONG_FIRST_COORDINATE = DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;

            @Override
            public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
                return face.getAxis() == getRotationAxis(state);

            }

            @Override
            public Direction.Axis getRotationAxis(BlockState state) {
                Direction.Axis pistonAxis = state.getValue(DirectionalKineticBlock.FACING)
                        .getAxis();
                boolean alongFirst = state.getValue(AXIS_ALONG_FIRST_COORDINATE);

                if (pistonAxis == Direction.Axis.X)
                    return alongFirst ? Direction.Axis.Y : Direction.Axis.Z;
                if (pistonAxis == Direction.Axis.Y)
                    return alongFirst ? Direction.Axis.X : Direction.Axis.Z;
                if (pistonAxis == Direction.Axis.Z)
                    return alongFirst ? Direction.Axis.X : Direction.Axis.Y;

                throw new IllegalStateException("Unknown axis??");
            }


        };

        private final PneumaticPistonBlockEntity parent;

        public PneumaticPistonShaftBlockEntity(final BlockEntityType<?> typeIn, final BlockPos pos, final BlockState state, final PneumaticPistonBlockEntity parent) {
            super(typeIn, new ExtraBlockPos(pos), state);
            this.parent = parent;
        }

        @Override
        public void onSpeedChanged(final float previousSpeed) {
            super.onSpeedChanged(previousSpeed);

            if (this.speed != 0.0 && !this.parent.isAssembled()) {
                this.parent.assembleNextTick = true;
            }

            if (this.sequenceContext != null && this.sequenceContext.instruction() == SequencerInstructions.TURN_ANGLE) {
                this.parent.sequencedExtensionLimit = this.sequenceContext.getEffectiveValue(this.getTheoreticalSpeed());
            }
        }

        @Override
        public KineticBlockEntity getParentBlockEntity() {
            return this.parent;
        }

        @Override
        protected void addStressImpactStats(final List<Component> tooltip, final float stressAtBase) {
            super.addStressImpactStats(tooltip, stressAtBase);
        }

        @Override
        protected boolean canPropagateDiagonally(final IRotate block, final BlockState state) {
            return true;
        }

        @Override
        public Component getKey() {
            return SimLang.translate("extra_kinetics.extra_cogwheel").component();
        }
    }
}
