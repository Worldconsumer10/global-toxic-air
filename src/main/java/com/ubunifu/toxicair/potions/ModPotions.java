package com.ubunifu.toxicair.potions;

import com.ubunifu.toxicair.ToxicAir;
import com.ubunifu.toxicair.effect.ModEffects;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class ModPotions {
    public static final RegistryEntry<Potion> RESPIRATORY_PROTECTION_1 = register("respiratory_prot_potion",
            new Potion(new StatusEffectInstance(ModEffects.RESPIRATORY_PROTECTION.value(),1200,0)));
    public static final RegistryEntry<Potion> RESPIRATORY_PROTECTION_2 = register("respiratory_prot_potion_1",
            new Potion(new StatusEffectInstance(ModEffects.RESPIRATORY_PROTECTION.value(),2400,1)));
    public static final RegistryEntry<Potion> RESPIRATORY_PROTECTION_3 = register("respiratory_prot_potion_2",
            new Potion(new StatusEffectInstance(ModEffects.RESPIRATORY_PROTECTION.value(),3600,2)));
    public static final RegistryEntry<Potion> RESPIRATORY_PROTECTION_4 = register("respiratory_prot_potion_3",
            new Potion(new StatusEffectInstance(ModEffects.RESPIRATORY_PROTECTION.value(),4800,3)));
    public static final RegistryEntry<Potion> RESPIRATORY_PROTECTION_5 = register("respiratory_prot_potion_4",
            new Potion(new StatusEffectInstance(ModEffects.RESPIRATORY_PROTECTION.value(),6000,4)));

    private static RegistryEntry<Potion> register(String name, Potion potion){
        return Registry.registerReference(Registries.POTION, Identifier.of(ToxicAir.MOD_ID,name),potion);
    }
    public static void Register(){}
}
