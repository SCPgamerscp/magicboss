package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.root.RootEntity;
import io.redspace.ironsspellbooks.util.ModTags;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Optional;

/**
 * 真の根縛り (True Root Binding)
 * Rootの強化版。半径20ブロック以内の自分以外のエンティティ全員を根縛りする。
 * 効果時間はRootの3倍 (getSpellPower * 20 * 3 ticks)
 * School: Nature / CastType: LONG / Cooldown: 20s / CastTime: 1s
 */
public class TrueRootBindingSpell extends AbstractSpell {

    private static final float RADIUS = 20f;

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "true_root_binding");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.NATURE_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(20)
            .build();

    public TrueRootBindingSpell() {
        this.manaCostPerLevel   = 5;
        this.baseSpellPower     = 5;
        this.spellPowerPerLevel = 1;
        this.castTime           = 20;   // 1 second
        this.baseManaCost       = 60;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.effect_length",
                        Utils.timeFromTicks(getDuration(spellLevel, caster), 1)),
                Component.translatable("ui.elementmagicboss.true_root_radius", (int) RADIUS)
        );
    }

    @Override public CastType getCastType()              { return CastType.LONG; }
    @Override public DefaultConfig getDefaultConfig()    { return defaultConfig; }
    @Override public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundEvents.EVOKER_PREPARE_ATTACK);
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.GRASS_BREAK);
    }

    // エリア系なのでターゲット選択不要 → 常に true を返す
    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel,
                                          LivingEntity entity, MagicData playerMagicData) {
        return true;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity,
                       CastSource castSource, MagicData playerMagicData) {

        int duration = getDuration(spellLevel, entity);
        AABB area = AABB.ofSize(entity.position(), RADIUS * 2, RADIUS * 2, RADIUS * 2);

        // 半径20ブロック内の自分以外の全LivingEntityを根縛り
        level.getEntitiesOfClass(LivingEntity.class, area, target ->
                target != entity
                && !target.getType().is(ModTags.CANT_ROOT)
                && target.distanceTo(entity) <= RADIUS
        ).forEach(target -> {
            // すでに根縛り中（RootEntityに乗っている）の場合はスキップ
            if (target.getVehicle() instanceof RootEntity) return;

            RootEntity rootEntity = new RootEntity(level, entity);
            rootEntity.setDuration(duration);
            rootEntity.setTarget(target);
            rootEntity.moveTo(target.position());
            level.addFreshEntity(rootEntity);
            target.stopRiding();
            target.startRiding(rootEntity, true);
        });

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    /** Rootの3倍の効果時間 */
    public int getDuration(int spellLevel, LivingEntity caster) {
        return ModSpellDurations.scalingDurationTicks(spellLevel, getSpellPower(spellLevel, caster));
    }
}
