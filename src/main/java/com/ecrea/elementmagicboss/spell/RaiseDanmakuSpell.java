package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.projectile.RaiseDanmakuProjectile;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class RaiseDanmakuSpell extends AbstractSpell {

    // 10重リング: 半径1〜10ブロック
    private static final float[] RING_RADII = { 1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f, 9.0f, 10.0f };
    // 弾同士の弧間隔 (ブロック単位) → 均等スキマ
    private static final float ARC_SPACING = 0.8f;
    private static final float DAMAGE_MULTIPLIER = 0.65f;

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "raise_danmaku");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.FIRE_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(20)
            .build();

    public RaiseDanmakuSpell() {
        this.manaCostPerLevel   = 5;
        this.baseSpellPower     = 12;
        this.spellPowerPerLevel = 2;
        this.castTime           = 16; // Raise Hellと同じ
        this.baseManaCost       = 60;
    }

    // Raise Hellと同じ: recast回数 = スペルレベル
    @Override
    public int getRecastCount(int spellLevel, @Nullable LivingEntity entity) {
        return spellLevel;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", getDamageText(spellLevel, caster)),
                Component.translatable("ui.irons_spellbooks.projectile_count", countTotalBullets()),
                Component.translatable("ui.irons_spellbooks.recast_count", getRecastCount(spellLevel, caster))
        );
    }

    private int countTotalBullets() {
        int total = 0;
        for (float r : RING_RADII) total += bulletsForRadius(r);
        return total;
    }

    private static int bulletsForRadius(float radius) {
        return Math.max(6, Math.round(Mth.TWO_PI * radius / ARC_SPACING));
    }

    @Override public CastType getCastType()              { return CastType.LONG; }
    @Override public DefaultConfig getDefaultConfig()    { return defaultConfig; }
    @Override public ResourceLocation getSpellResource() { return spellId; }

    // Raise Hellと同じ: キャスト時間をアトリビュートで変化させない
    @Override
    public int getEffectiveCastTime(int spellLevel, @Nullable LivingEntity entity) {
        return getCastTime(spellLevel);
    }

    // Raise Hellと同じ: プレイヤーはキャンセル不可
    @Override
    public boolean canBeInterrupted(Player player) {
        return false;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.RAISE_HELL_PREPARE.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.FIRE_ERUPTION_SLAM.get());
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.OVERHEAD_MELEE_SWING_ANIMATION;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return AnimationHolder.pass();
    }

    // ---------------------------------------------------------------
    // 発動: Raise Hellと同じrecastロジック + 10重リング打ち上げ
    // ---------------------------------------------------------------
    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity,
                       CastSource castSource, MagicData playerMagicData) {

        // Raise Hellと全く同じrecast登録ロジック
        if (!playerMagicData.getPlayerCooldowns().isOnCooldown(this)
                && !playerMagicData.getPlayerRecasts().hasRecastForSpell(getSpellId())) {
            playerMagicData.getPlayerRecasts().addRecast(
                    new RecastInstance(getSpellId(), spellLevel, getRecastCount(spellLevel, entity), 80, castSource, null),
                    playerMagicData
            );
        }

        if (!level.isClientSide) {
            float damage = getDamage(spellLevel, entity);
            double baseX = entity.getX();
            double baseY = entity.getY() + 0.3;
            double baseZ = entity.getZ();

            for (float radius : RING_RADII) {
                int count = bulletsForRadius(radius);
                for (int i = 0; i < count; i++) {
                    float angle = (Mth.TWO_PI / count) * i;
                    double offsetX = Math.sin(angle) * radius;
                    double offsetZ = Math.cos(angle) * radius;

                    RaiseDanmakuProjectile proj = new RaiseDanmakuProjectile(level, entity);
                    proj.setPos(baseX + offsetX, baseY, baseZ + offsetZ);
                    proj.setDamage(damage);
                    proj.setVariant(i % 13);
                    proj.shoot(new Vec3(0, 1, 0));
                    level.addFreshEntity(proj);
                }
            }
        }

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public SpellDamageSource getDamageSource(Entity projectile, Entity attacker) {
        return super.getDamageSource(projectile, attacker).setFireTicks(40).setIFrames(0);
    }

    private float getDamage(int spellLevel, LivingEntity caster) {
        float weaponDamage = Utils.getWeaponDamage(caster, MobType.UNDEFINED);
        return (getSpellPower(spellLevel, caster) + weaponDamage) * DAMAGE_MULTIPLIER;
    }

    private String getDamageText(int spellLevel, LivingEntity caster) {
        if (caster != null) {
            float weaponDamage = Utils.getWeaponDamage(caster, MobType.UNDEFINED);
            String plus = weaponDamage > 0
                    ? String.format(" (+%s)", Utils.stringTruncation(weaponDamage, 1))
                    : "";
            return Utils.stringTruncation(getDamage(spellLevel, caster), 1) + plus;
        }
        return String.valueOf(getSpellPower(spellLevel, caster));
    }
}
