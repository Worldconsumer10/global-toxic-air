package com.ubunifu.toxicair.mixin;

import com.ubunifu.toxicair.ToxicAir;
import com.ubunifu.toxicair.damagetypes.ModDamageTypes;
import com.ubunifu.toxicair.effect.ModEffects;
import com.ubunifu.toxicair.toxins.AStarAirAlgorithm;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.*;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
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
        if (entity.getWorld().isClient) return;
        if (entity.getGroup() == EntityGroup.UNDEAD) return;
        if (entity instanceof PlayerEntity)
            AStarAirAlgorithm.EntityTick(entity);
        if (entity.isInvulnerable()) return;

        StatusEffectInstance protection = entity.getStatusEffect(ModEffects.RESPIRATORY_PROTECTION.value());
        int protectionLevel = protection != null ? protection.getAmplifier() + 1 : 0; // +1 because amplifier starts at 0
        if (!AStarAirAlgorithm.isToxicAir(entity.getWorld(),entity.getBlockPos().toImmutable())) {
            HurtTick = Math.min(HurtTick + 0.1f, MAX_HURT_TICK);
            currentHurtTimer = HurtTick;
            return;
        }

        float resistance = protectionLevel * 0.1f;
        float toxicityEffect = (1f - resistance);

        //if (entity instanceof PlayerEntity) ToxicAir.LOGGER.info("Player is in toxic air.");
        currentHurtTimer -= toxicityEffect;
        if (currentHurtTimer <= 0f) {
            float damage = MathHelper.clamp(toxicityEffect * 2f, 0.5f, 6f); // Damage scales with severity
            entity.damage(ModDamageTypes.of(entity.getWorld(), ModDamageTypes.TOXIC_SUFFORCATION), damage);

            HurtTick = Math.max(HurtTick - 1f, 5f);
            currentHurtTimer = HurtTick;
        }
    }
}
