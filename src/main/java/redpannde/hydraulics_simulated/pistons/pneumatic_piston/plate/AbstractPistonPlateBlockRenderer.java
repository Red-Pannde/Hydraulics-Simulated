package redpannde.hydraulics_simulated.pistons.pneumatic_piston.plate;

import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class AbstractPistonPlateBlockRenderer extends KineticBlockEntityRenderer<AbstractPistonPlateBlockEntity> {

    public AbstractPistonPlateBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }
}
