package redpannde.hydraulics_simulated.registry;

import com.simibubi.create.foundation.data.CreateRegistrate;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

public class HydraulicsSimRegistrate extends CreateRegistrate {

    public static final Set<String> MODS = new HashSet<>();
    private ResourceLocation currentSection;
    public HydraulicsSimRegistrate(final ResourceLocation initialSection, final String modId) {
        super(modId);
        this.currentSection = initialSection;
        MODS.add(modId);
    }
}
