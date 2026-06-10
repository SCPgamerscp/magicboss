package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.ModEntities;
import com.ecrea.elementmagicboss.entity.projectile.AbyssBlastEntity;
import com.ecrea.elementmagicboss.sound.ModSounds;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

/**
 * アビスブラスト（深淵砲）
 * Cataclysmのアビスブラストを魔法として詠唱可能にしたもの。
 * School: Ender / Cast Type: Long / Cooldown: 20s
 * チャージ後、視線方向に追随する紫のビームを10秒間射出する。
 */
public class AbyssBlastSpell extends AbstractSpell {

    /** ビーム持続tick数（詠唱時間。20tick = 1秒 × 10秒 = 200tick） */
    public static final int BEAM_DURATION_TICKS = 200;

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "abyss_blast");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(SchoolRegistry.ENDER_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(20)
            .build();

    public AbyssBlastSpell() {
        this.manaCostPerLevel   = 3;
        this.baseSpellPower     = 10;
        this.spellPowerPerLevel = 2;
        this.castTime           = 100;  // 5秒チャージ (チャージ音に合わせる)
        this.baseManaCost       = 50;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage",
                        Utils.stringTruncation(getDamage(spellLevel, caster), 1)),
                Component.translatable("ui.elementmagicboss.abyss_blast_duration",
                        String.format("%.0f", BEAM_DURATION_TICKS / 20.0f))
        );
    }

    @Override public CastType getCastType()              { return CastType.LONG; }
    @Override public DefaultConfig getDefaultConfig()    { return defaultConfig; }
    @Override public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(ModSounds.ABYSS_BLAST_CHARGE.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ANIMATION_LONG_CAST;
    }

    /**
     * チャージ完了後にビームエンティティを1体スポーンさせる。
     */
    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity,
                       CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide) {
            float damage = getDamage(spellLevel, entity);
            float yaw = (float) ((entity.yHeadRot + 90) * Math.PI / 180.0d);
            float pitch = (float) (-entity.getXRot() * Math.PI / 180.0d);

            AbyssBlastEntity beam = new AbyssBlastEntity(
                    ModEntities.ABYSS_BLAST.get(), level, entity,
                    entity.getX(), entity.getEyeY(), entity.getZ(),
                    yaw, pitch, BEAM_DURATION_TICKS, damage
            );
            level.addFreshEntity(beam);

            // 発射音
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    ModSounds.ABYSS_BLAST_SHOOT.get(), SoundSource.PLAYERS, 3.0f, 1.0f);
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private float getDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster) * 1.5f;
    }

    @Override
    public SpellDamageSource getDamageSource(Entity projectile, Entity attacker) {
        return super.getDamageSource(projectile, attacker).setIFrames(0);
    }
}
