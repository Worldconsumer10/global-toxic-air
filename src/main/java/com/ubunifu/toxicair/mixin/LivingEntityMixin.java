package com.ubunifu.toxicair.mixin;

import com.ubunifu.toxicair.AirHandler;
import com.ubunifu.toxicair.ModDamageTypes;
import com.ubunifu.toxicair.effect.ModEffects;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.*;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Unique
    private static final int MAX_HURT_TICK = 60;
    @Unique
    float HurtTick = MAX_HURT_TICK;
    @Unique
    float currentHurtTimer = HurtTick;

    @Inject(method="drop",at=@At("HEAD"),cancellable = true)
    public void dropInjection(DamageSource source, CallbackInfo ci){
        if (source.isOf(ModDamageTypes.TOXIC_SUFFORCATION))
            ci.cancel();
    }

    @Inject(method = "tick", at=@At("HEAD"))
    public void tickProcessor(CallbackInfo ci){
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.getGroup() == EntityGroup.UNDEAD) return;
        float toxicity = GetAirToxicity();
        if (toxicity <= 0){
            HurtTick = Math.min(HurtTick+.1f,MAX_HURT_TICK);
            currentHurtTimer = HurtTick;
        } else {
            StatusEffectInstance statusEffectInstance = entity.getStatusEffect(ModEffects.RESPIRATORY_PROTECTION.value());
            float hurtTimerDecrease = 1f;
            if (statusEffectInstance != null)
                hurtTimerDecrease = Math.min(statusEffectInstance.getAmplifier() / 5,1);
            currentHurtTimer -= hurtTimerDecrease;
            if (currentHurtTimer <= 0f) {
                if (statusEffectInstance != null && statusEffectInstance.getAmplifier() > 2)
                    entity.damage(ModDamageTypes.of(entity.getWorld(),ModDamageTypes.TOXIC_SUFFORCATION), 1.0f);
                HurtTick = Math.max(HurtTick - 1, 5f); // Limit how fast it can go
                currentHurtTimer = HurtTick;
            }
        }
    }
    @Unique
    float GetAirToxicity(){
        LivingEntity entity = (LivingEntity) (Object) this;

        World playerWorld = entity.getWorld();
        if (playerWorld.isClient) return 0f;
        BlockPos eyeBlockPos = BlockPos.ofFloored(entity.getEyePos());
        return AirHandler.getOrComputeToxicity(playerWorld, eyeBlockPos);
    }
}
