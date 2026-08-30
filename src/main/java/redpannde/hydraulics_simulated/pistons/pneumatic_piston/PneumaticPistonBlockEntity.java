package redpannde.hydraulics_simulated.pistons.pneumatic_piston;

import com.simibubi.create.content.fluids.pump.PumpBlock;
import com.simibubi.create.content.fluids.pump.PumpBlockEntity;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import redpannde.hydraulics_simulated.HydraulicsSimulated;
import redpannde.hydraulics_simulated.pistons.AbstractPistonBlockEntity;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class PneumaticPistonBlockEntity extends AbstractPistonBlockEntity {
    protected List<Direction> attachedFluidPumpDirections;


    public PneumaticPistonBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    protected double getExtension() {
        AtomicReference<Double> extension = new AtomicReference<>(this.targetLength);
        final float speedReductionConstant = 0.00625f;
        for (int length = this.pistonLength - 1; length >= 0; length--) {
            if (this.level.getBlockEntity(this.getBlockPos().relative(this.getBlockState().getValue(PneumaticPistonBlock.FACING).getOpposite(), length)) instanceof PneumaticPistonBlockEntity pistonBlockEntity) {
                if (pistonBlockEntity.attachedFluidPumpDirections != null && !pistonBlockEntity.attachedFluidPumpDirections.isEmpty()) {
                    pistonBlockEntity.attachedFluidPumpDirections.forEach(pumpDirection -> {
                        if (this.level.getBlockEntity(pistonBlockEntity.getBlockPos().relative(pumpDirection)) instanceof PumpBlockEntity fluidPump) {
                            if (this.level.getBlockState(fluidPump.getBlockPos().relative(pumpDirection)).getBlock().equals(Blocks.AIR)) {

                                float speed = Math.abs(fluidPump.getSpeed());
                                if (Objects.equals(level.getBlockEntity(fluidPump.getBlockPos().relative(fluidPump.getBlockState().getValue(PumpBlock.FACING))), pistonBlockEntity)) {
                                    extension.set(extension.get() + speed * speedReductionConstant);

                                } else {
                                    extension.set(extension.get() - speed * speedReductionConstant);                                }
                            }
                        }
                    });
                }
            }
        }
        return extension.get();
    }

    public void getAttachedFluidPumps() {
        List<Direction> attachedFluidPumps = new ArrayList<>();
        List<Direction.Axis> axisList = new ArrayList<>(Arrays.stream(Direction.Axis.values()).toList());
        Direction facing = this.getBlockState().getValue(PneumaticPistonBlock.FACING);
        Direction.Axis facingAxis = facing.getAxis();
        BlockPos blockPos = this.getBlockPos();
        axisList.remove(facingAxis);
        for (Direction.Axis axis : axisList) {
            for (Direction direction : Direction.values()) {
                if (this.level.getBlockEntity(blockPos.relative(direction)) instanceof PumpBlockEntity pumpBlockEntity) {
                    if (pumpBlockEntity.getBlockState().getValue(PumpBlock.FACING).getAxis().equals(axis)) {
                        attachedFluidPumps.add(direction);

                    }
                }
            }
        }
        this.attachedFluidPumpDirections = attachedFluidPumps;
    }

    public void updateAttachedFluidPumps(BlockPos pos, BlockPos neighbor) {
        if (this.attachedFluidPumpDirections == null) {
            return;
        }
        if (this.level.getBlockEntity(neighbor) instanceof PumpBlockEntity pumpBlockEntity) {
            Direction direction = pumpBlockEntity.getBlockState().getValue(PumpBlock.FACING);
            if (neighbor.relative(direction).equals(pos) && !this.attachedFluidPumpDirections.contains(direction.getOpposite())) {
                this.attachedFluidPumpDirections.add(direction.getOpposite());
            } else if (neighbor.relative(direction.getOpposite()).equals(pos) && !this.attachedFluidPumpDirections.contains(direction)) {
                this.attachedFluidPumpDirections.add(direction);
            }
        } else {
            Vec3 vec3 = new Vec3(neighbor.getCenter().subtract(pos.getCenter()).toVector3f());
            attachedFluidPumpDirections.remove(Direction.fromDelta((int) vec3.x, (int) vec3.y, (int) vec3.z));
        }
    }

    @Override
    public void assemble() {
        this.targetLength = 0f;
        super.assemble();
    }

    @Override
    public void disassemble() {
        this.targetLength = 0f;
        super.disassemble();
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        HydraulicsSimulated.LOGGER.debug("write {}", compound.toString());
        super.write(compound, registries, clientPacket);
        List<Integer> intList = new IntArrayList();
        if (this.attachedFluidPumpDirections != null) {
            this.attachedFluidPumpDirections.forEach(pumpDirection -> intList.add(pumpDirection.get3DDataValue()));
        }
        compound.putIntArray("AttachedFluidPumpDirections", intList);
    }


    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {

        HydraulicsSimulated.LOGGER.debug("read {}", compound.toString());
        super.read(compound, registries, clientPacket);
        List<Direction> attachedFluidPumpDirections = new ArrayList<>();
        for (int i : compound.getIntArray("AttachedFluidPumpDirections")) {
            attachedFluidPumpDirections.add(Direction.from3DDataValue(i));
        }
        this.attachedFluidPumpDirections = attachedFluidPumpDirections;
    }
}


