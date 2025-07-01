package com.ubunifu.toxicair.potions;

import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.Ingredient;

public class ModPotionRecipe {
    public static void Register(){
        FabricBrewingRecipeRegistry.registerPotionRecipe(Potions.THICK,
                Ingredient.ofStacks(new ItemStack(Items.CHARCOAL)),
                ModPotions.RESPIRATORY_PROTECTION_1.value()
        );
        FabricBrewingRecipeRegistry.registerPotionRecipe(ModPotions.RESPIRATORY_PROTECTION_1.value(),
                Ingredient.ofStacks(new ItemStack(Items.GLOWSTONE_DUST)),
                ModPotions.RESPIRATORY_PROTECTION_2.value()
        );
        FabricBrewingRecipeRegistry.registerPotionRecipe(ModPotions.RESPIRATORY_PROTECTION_2.value(),
                Ingredient.ofStacks(new ItemStack(Items.GLOWSTONE_DUST)),
                ModPotions.RESPIRATORY_PROTECTION_3.value()
        );
        FabricBrewingRecipeRegistry.registerPotionRecipe(ModPotions.RESPIRATORY_PROTECTION_3.value(),
                Ingredient.ofStacks(new ItemStack(Items.GLOWSTONE_DUST)),
                ModPotions.RESPIRATORY_PROTECTION_4.value()
        );
        FabricBrewingRecipeRegistry.registerPotionRecipe(ModPotions.RESPIRATORY_PROTECTION_4.value(),
                Ingredient.ofStacks(new ItemStack(Items.GLOWSTONE_DUST)),
                ModPotions.RESPIRATORY_PROTECTION_5.value()
        );
    }
}
