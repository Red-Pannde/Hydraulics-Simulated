package redpannde.hydraulics_simulated.pistons.pneumatic_piston;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class PneumaticPistonRenderer<T extends KineticBlockEntity> extends KineticBlockEntityRenderer<T> {
    public PneumaticPistonRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }
}
