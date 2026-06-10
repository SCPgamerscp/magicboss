package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.projectile.ClownpieceFieldEntity;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

/**
 * クラウンピース (Clownpiece)
 * YH の ClownItemSpell と同じ弾幕パターンを YH 依存なしで再現。
 * - Phase0: 3対のスイープ弾 + 扇形弾幕
 * - Phase1: 1対の広角スイープ弾 + 扇形弾幕
 * School: Fire / CastType: LONG / Cooldown: 10s
 */
public class ClownpieceSpell extends AbstractSpell {

    /** このタグが付いた SmallMagicArrow は無敵時間を無視する */
    public static final String CLOWNPIECE_ARROW_TAG = "elementmagicboss_clownpiece_arrow";

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "clownpiece");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.FIRE_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(10)
            .build();

    public ClownpieceSpell() {
        this.manaCostPerLevel   = 3;
        this.baseSpellPower     = 6;
        this.spellPowerPerLevel = 1;
        this.castTime           = 20;  // 1秒詠唱でターゲット取得
        this.baseManaCost       = 40;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage",
                        Utils.stringTruncation(getDamage(spellLevel, caster), 1)),
                Component.translatable("ui.elementmagicboss.clownpiece_duration",
                        Utils.timeFromTicks(120, 1))
        );
    }

    @Override public CastType getCastType()              { return CastType.LONG; }
    @Override public DefaultConfig getDefaultConfig()    { return defaultConfig; }
    @Override public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.CHARGE_RAISED_HAND;
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.FIRECHARGE_USE);
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity,
                                           MagicData playerMagicData) {
        // YH SpellItem と同じ範囲でターゲット取得
        return Utils.preCastTargetHelper(level, entity, playerMagicData, this, 32, 0.35f);
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity,
                       CastSource castSource, MagicData playerMagicData) {
        if (!world.isClientSide() && world instanceof ServerLevel sl) {
            Vec3 dir = entity.getLookAngle();
            Vec3 targetPos = null;

            if (playerMagicData.getAdditionalCastData() instanceof TargetEntityCastData td) {
                LivingEntity target = td.getTarget(sl);
                if (target != null) {
                    targetPos = target.position().add(0, target.getBbHeight() * 0.5, 0);
                    // targetに向かう方向に dir を更新
                    Vec3 diff = targetPos.subtract(entity.getEyePosition());
                    if (diff.lengthSqr() > 1e-4) dir = diff.normalize();
                }
            }

            ClownpieceFieldEntity field = new ClownpieceFieldEntity(
                    world, entity, getDamage(spellLevel, entity), dir, targetPos);
            field.setPos(entity.getX(), entity.getY() + entity.getEyeHeight() * 0.5, entity.getZ());
            world.addFreshEntity(field);
        }
        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    private float getDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster) * 2.0f;
    }
}
