package redpannde.hydraulics_simulated.pistons.pneumatic_piston;

import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.content.contraptions.bearing.BearingBlock;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.item.TooltipHelper;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.ryanhcode.sable.api.physics.PhysicsPipeline;
import dev.ryanhcode.sable.api.physics.constraint.rotary.RotaryConstraintConfiguration;
import dev.ryanhcode.sable.api.physics.constraint.rotary.RotaryConstraintHandle;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.plot.LevelPlot;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import dev.simulated_team.simulated.content.blocks.swivel_bearing.SwivelBearingBlock;
import dev.simulated_team.simulated.content.blocks.swivel_bearing.SwivelBearingBlockEntity;
import dev.simulated_team.simulated.content.blocks.swivel_bearing.link_block.SwivelBearingPlateBlock;
import dev.simulated_team.simulated.content.blocks.swivel_bearing.link_block.SwivelBearingPlateBlockEntity;
import dev.simulated_team.simulated.data.SimLang;
import dev.simulated_team.simulated.data.advancements.SimAdvancements;
import dev.simulated_team.simulated.index.SimBlocks;
import dev.simulated_team.simulated.index.SimSoundEvents;
import dev.simulated_team.simulated.util.SimAssemblyHelper;
import dev.simulated_team.simulated.util.SimLevelUtil;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import redpannde.hydraulics_simulated.registry.HydraulicsSimBlocks;

import java.util.List;
import java.util.UUID;

import static net.minecraft.ChatFormatting.GOLD;

public class PneumaticPistonBlockEntity extends KineticBlockEntity {

    /**
     * If the bearing should assemble next tick
     */
    public boolean assembleNextTick;
    protected AssemblyException lastException;
    /**
     * The target angle degrees from the last tick
     */
    private double lastTargetAngleDegrees = 0;
    /**
     * The current target angle in degrees
     */
    private double targetAngleDegrees = 0;
    /**
     * The angle limit from sequenced contexts
     */
    private double sequencedAngleLimit = -1;
    /**
     * The ID of the attached sub-level
     */
    @Nullable
    private UUID subLevelID;
    /**
     * The block position of the attached {@link SwivelBearingPlateBlock}
     */
    @Nullable
    private BlockPos pneumaticPlatePos;
    /**
     * The current constraint handle between this swivel and the attached sub-level
     */
    @Nullable
    private RotaryConstraintHandle handle;
    /**
     * If this BE is being destroyed as a part of assembly
     */
    private boolean assembling;
    /**
     * The locked default scroll option
     */
    private ScrollOptionBehaviour<SwivelBearingBlockEntity.LockingSetting> lockedDefaultOption;
    public PneumaticPistonBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        this.assembleNextTick = false;
    }

    private boolean isValidForOptionPanel(final BlockState state, final Direction direction) {
        final Direction facing = state.getValue(SwivelBearingBlock.FACING);
        final Direction.Axis currentAxis = facing.getAxis();

        return direction.getAxis() != currentAxis;
    }

    @Override
    public void tick() {

        super.tick();


        // assemble or disassemble
        if (this.assembleNextTick) {
            if (!this.isAssembled()) {
                this.assemble();
            } else {
                this.disassemble();
            }
        }

        final SubLevel attached = this.getAttachedSubLevel();

        // update our powered state and reattach constraints
        final int bestSignal = this.level.getBestNeighborSignal(this.getBlockPos());
        final boolean shouldLock = this.lockedDefaultOption.get().shouldLock(bestSignal);

        if (shouldLock && !this.isLocking()) {
            this.level.setBlockAndUpdate(this.getBlockPos(), this.getBlockState().setValue(BlockStateProperties.POWERED, true));

            if (this.handle != null && attached != null) {
                //update our constraint
                this.reattachConstraint(attached, false);
            }

            if (attached != null && this.getPlatePos() != null) {
                final BlockState plateBlock = this.level.getBlockState(this.getPlatePos());

                if (plateBlock.is(SimBlocks.SWIVEL_BEARING_LINK_BLOCK)) {
                    this.setTargetAngleFromCurrentOrientation(plateBlock, attached);
                }
            }
        } else if (!shouldLock && this.isLocking()) {
            this.level.setBlockAndUpdate(this.getBlockPos(), this.getBlockState().setValue(BlockStateProperties.POWERED, false));

            if (this.handle != null && attached != null) {
                //update our constraint
                this.reattachConstraint(attached, false);
            }
        }

        // check persistence to make sure we keep our sublevel after reload
        if (this.getSubLevelID() != null) {
            this.checkPersistence(this.getSubLevelID());
        }

        // update our target angles
        this.lastTargetAngleDegrees = this.targetAngleDegrees;
        float angularSpeed = convertToAngular(this.limitCogSpeed(this.cogwheel.getSpeed()));

        boolean shouldUpdateAngle = true;

        if (this.sequencedAngleLimit >= 0) {
            angularSpeed = (float) Mth.clamp(angularSpeed, -this.sequencedAngleLimit, this.sequencedAngleLimit);
            this.sequencedAngleLimit = Math.max(0, this.sequencedAngleLimit - Math.abs(angularSpeed));
        } else {
            final SubLevelPhysicsSystem physicsSystem = SubLevelPhysicsSystem.get(this.level);
            // if rotation is not sequenced (go to a set angle) and physics is paused, do not update target angle
            if (physicsSystem == null || physicsSystem.getPaused()) {
                shouldUpdateAngle = false;
            }
        }

        if (shouldUpdateAngle) {
            // for negative facing directions, we need to negate the angular speed
            if (this.getBlockState().getValue(SwivelBearingBlock.FACING).getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
                angularSpeed *= -1.0f;
            }

            this.targetAngleDegrees += angularSpeed;
            this.targetAngleDegrees %= 360;

            if (attached != null && this.isAssembled() && this.handle != null) {
                final SubLevel containing = this.getContainingSubLevel();

                if (angularSpeed != 0.0) {
                    final PhysicsPipeline pipeline = ((ServerSubLevelContainer) SubLevelContainer.getContainer(this.level)).physicsSystem().getPipeline();

                    if (containing instanceof final ServerSubLevel serverSubLevel) {
                        pipeline.wakeUp(serverSubLevel);
                    }

                    if (attached instanceof final ServerSubLevel serverSubLevel) {
                        pipeline.wakeUp(serverSubLevel);
                    }
                }
            }
        }

        this.assembleNextTick = false;
    }

    @Override
    public boolean addToTooltip(final List<Component> tooltip, final boolean isPlayerSneaking) {
        if (super.addToTooltip(tooltip, isPlayerSneaking))
            return true;

        if (isPlayerSneaking)
            return false;

        if (this.cogwheel.getSpeed() == 0)
            return false;

        if (this.isAssembled()) {
            if (this.isTooFast()) {
                SimLang.translate("swivel_bearing.too_fast")
                        .style(GOLD)
                        .forGoggles(tooltip);

                final MutableComponent component = SimLang.translate("swivel_bearing.too_fast_error")
                        .component();

                final List<Component> cutString = TooltipHelper.cutTextComponent(component, FontHelper.Palette.GRAY_AND_WHITE);
                tooltip.addAll(cutString);

                return true;
            }

            return false;
        }
        final BlockState state = this.getBlockState();
        if (!(state.getBlock() instanceof SwivelBearingBlock))
            return false;

        final BlockState attachedState = this.level.getBlockState(this.worldPosition.relative(state.getValue(BearingBlock.FACING)));
        if (attachedState.canBeReplaced())
            return false;
        TooltipHelper.addHint(tooltip, "hint.empty_bearing");
        return true;
    }

    public void assemble() {
        final BlockPos pos = this.getBlockPos();
        final BlockPos toAssemble = pos.relative(this.getBlockState().getValue(SwivelBearingBlock.FACING));
        final SimAssemblyHelper.AssemblyResult result;

        try {
            result = SimAssemblyHelper.assembleFromSingleBlock(this.level, pos, toAssemble, false, false);
            this.lastException = null;
        } catch (final AssemblyException e) {
            this.lastException = e;
            this.sendData();
            return;
        }

        this.sendData();

        final ServerSubLevel assembledSubLevel;
        final BlockPos assembleOffset;
        final BlockState link = SimBlocks.SWIVEL_BEARING_LINK_BLOCK.getDefaultState()
                .setValue(SwivelBearingPlateBlock.FACING, this.getBlockState().getValue(SwivelBearingBlock.FACING));

        if (result != null) {
            assembledSubLevel = (ServerSubLevel) result.subLevel();
            assembleOffset = result.offset();
        } else {
            final ServerSubLevelContainer container = (ServerSubLevelContainer) SubLevelContainer.getContainer(this.level);

            final Pose3d pose = new Pose3d();
            pose.position().set(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

            assembledSubLevel = (ServerSubLevel) container.allocateNewSubLevel(pose);
            final LevelPlot plot = assembledSubLevel.getPlot();

            final ChunkPos center = plot.getCenterChunk();
            plot.newEmptyChunk(center);
            plot.getEmbeddedLevelAccessor().setBlock(BlockPos.ZERO, link, 3);

            final BlockPos plotAnchor = plot.getCenterBlock();
            final Vector3dc centerOfMass = assembledSubLevel.getMassTracker().getCenterOfMass();
            final Vector3d subLevelCenter = JOMLConversion.atLowerCornerOf(pos);

            if (centerOfMass != null) {
                subLevelCenter.add(centerOfMass.x() - plotAnchor.getX(), centerOfMass.y() - plotAnchor.getY(), centerOfMass.z() - plotAnchor.getZ());
            } else {
                assembledSubLevel.logicalPose().rotationPoint()
                        .set(plotAnchor.getX() + 0.5, plotAnchor.getY() + 0.5, plotAnchor.getZ() + 0.5);
            }

            assembledSubLevel.logicalPose().position().set(subLevelCenter.x, subLevelCenter.y, subLevelCenter.z);
            assembleOffset = plotAnchor.subtract(pos);

            final SubLevelPhysicsSystem physicsSystem = container.physicsSystem();
            final PhysicsPipeline pipeline = physicsSystem.getPipeline();

            final SubLevel containingSubLevel = this.getContainingSubLevel();
            if (containingSubLevel != null) {
                SubLevelAssemblyHelper.kickFromContainingSubLevel((ServerLevel) this.level, physicsSystem, pipeline, assembledSubLevel, containingSubLevel);
            }

            pipeline.teleport(assembledSubLevel, assembledSubLevel.logicalPose().position(), assembledSubLevel.logicalPose().orientation());
            assembledSubLevel.updateLastPose();

            this.level.playSound(null, pos, SimSoundEvents.SIMULATED_CONTRAPTION_MOVES.event(), SoundSource.BLOCKS, 1.0f, 1.0f);
        }

        this.getLevel().setBlockAndUpdate(pos, this.getBlockState().setValue(SwivelBearingBlock.ASSEMBLED, true));

        this.attachConstraints(assembledSubLevel, this.getConstraintPos(toAssemble, assembleOffset));
        this.setSubLevelID(assembledSubLevel.getUniqueId());

        final BlockPos plotPos = pos.offset(assembleOffset);
        if (result != null) {
            this.getLevel().setBlockAndUpdate(plotPos, link);
        }
        final BlockEntity be = this.getLevel().getBlockEntity(plotPos);

        if (be instanceof final SwivelBearingPlateBlockEntity plateBE) {
            plateBE.setParent(this);
            this.setPlatePos(plotPos);
        }

        SimAdvancements.YOU_SPIN_ME_RIGHT_ROUND.awardToNearby(pos, this.getLevel());
    }

    public void disassemble() {
        if (this.isRemoved()) {
            return;
        }

        this.removeHandle();
        if (this.getSubLevelID() != null) {
            final SubLevel subLevel = SubLevelContainer.getContainer(this.level).getSubLevel(this.getSubLevelID());
            if (subLevel != null) {
                final BlockPos platePos = this.getPlatePos();
                if (platePos != null) {
                    this.destroyPlate();

                    // if destroying the plate removed the sub-level, skip disassembling
                    if (!subLevel.isRemoved()) {
                        SimAssemblyHelper.disassembleSubLevel(this.level, subLevel, platePos, this.getBlockPos(), Rotation.NONE, true);
                    } else {
                        this.level.playSound(null, platePos, SimSoundEvents.SIMULATED_CONTRAPTION_STOPS.event(), SoundSource.BLOCKS, 1.0f, 1.0f);
                    }
                }
            }
        }

        this.getLevel().setBlockAndUpdate(this.getBlockPos(), this.getBlockState().setValue(SwivelBearingBlock.ASSEMBLED, false));

        this.setSubLevelID(null);
        this.setPlatePos(null);
        this.targetAngleDegrees = 0;
    }

    private void checkPersistence(final UUID id) {
        if (this.getPlatePos() != null && SimLevelUtil.isAreaActuallyLoaded(this.getLevel(), this.getPlatePos(), 1)) {
            if (!this.getLevel().getBlockState(this.getPlatePos()).is(SimBlocks.SWIVEL_BEARING_LINK_BLOCK)) {
                return;
            }
        }

        final SubLevel subLevel = SubLevelContainer.getContainer(this.getLevel()).getSubLevel(id);
        if (this.handle != null && !this.handle.isValid()) {
            this.handle = null;
        }

        if (subLevel != null && this.handle == null) {
            this.reattachConstraint(subLevel, true);
        }
    }

    public void reattachConstraint(final SubLevel toAttach, final boolean updatePlate) {
        //we also want to "reset" the plate BE here too, so it's correct
        final BlockPos platePos = this.getPlatePos();
        if (platePos != null) {
            if (this.handle != null) {
                this.handle.remove();
            }

            if (updatePlate) {
                this.associatePlateWithParent();
            }

            final BlockState plateState = this.level.getBlockState(platePos);
            if (!plateState.is(HydraulicsSimBlocks.PNEUMATIC_PISTON_BLOCK)) return;

            final Direction plateFacing = plateState.getValue(SwivelBearingPlateBlock.FACING);
            this.attachConstraints(toAttach, JOMLConversion.toJOML(platePos.relative(plateFacing).getCenter()));
        }
    }

    private void attachConstraints(final SubLevel toAttach, final Vector3d attachPos) {
        final BlockPos platePos = this.getPlatePos();

        if (platePos == null) return;
        final BlockState plateState = this.level.getBlockState(platePos);

        if (!plateState.is(SimBlocks.SWIVEL_BEARING_LINK_BLOCK)) return;

        final Vector3d anchorPos = JOMLConversion.toJOML(this.getBlockPos().relative(this.getBlockState().getValue(DirectionalKineticBlock.FACING)).getCenter());
        final Vec3 facingVec = Vec3.atLowerCornerOf(this.getBlockState().getValue(DirectionalKineticBlock.FACING).getNormal());
        final Vec3 plateFacingVec = Vec3.atLowerCornerOf(plateState.getValue(DirectionalKineticBlock.FACING).getNormal());

        final RotaryConstraintConfiguration constraint = new RotaryConstraintConfiguration(
                anchorPos,
                attachPos.sub(JOMLConversion.toJOML(plateFacingVec.scale(0.001f))),
                JOMLConversion.toJOML(facingVec),
                JOMLConversion.toJOML(plateFacingVec)
        );

        final ServerSubLevelContainer container = SubLevelContainer.getContainer((ServerLevel) this.getLevel());
        final PhysicsPipeline pipeline = container.physicsSystem().getPipeline();

        this.handle = pipeline.addConstraint((ServerSubLevel) Sable.HELPER.getContaining(this), (ServerSubLevel) toAttach, constraint);
    }
}
