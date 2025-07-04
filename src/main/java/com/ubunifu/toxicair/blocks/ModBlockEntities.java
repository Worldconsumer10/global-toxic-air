package com.ubunifu.toxicair.blocks;

import com.ubunifu.toxicair.ToxicAir;
import com.ubunifu.toxicair.blocks.AirPurifier.AirPurifierBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityType;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {
    public static final BlockEntityType<AirPurifierBlockEntity> AIR_PURIFIER_BLOCK_ENTITY =
            Registry.register(
                    Registries.BLOCK_ENTITY_TYPE,
                    new Identifier(ToxicAir.MOD_ID, "air_purifier_be"),
                    BlockEntityType.Builder.create(AirPurifierBlockEntity::new, ModBlocks.AIR_PURIFIER).build(null)
            );

    public static void Register() {
        ToxicAir.LOGGER.info("Registering Block Entities for " + ToxicAir.MOD_ID);
    }
}
