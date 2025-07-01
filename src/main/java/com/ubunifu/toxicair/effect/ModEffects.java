package com.ubunifu.toxicair.effect;

import com.ubunifu.toxicair.ToxicAir;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class ModEffects {
    public static final RegistryEntry<StatusEffect> RESPIRATORY_PROTECTION
            = register("respiratory_protection",
            new RespiratoryProtectionEffect(StatusEffectCategory.BENEFICIAL,0xd014fa));

    private static RegistryEntry<StatusEffect> register(String name, StatusEffect statusEffect){
        return Registry.registerReference(Registries.STATUS_EFFECT, Identifier.of(ToxicAir.MOD_ID,name),statusEffect);
    }
    public static void Register(){}
}
