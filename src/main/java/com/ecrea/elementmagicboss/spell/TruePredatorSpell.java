package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.devour_jaw.DevourJaw;
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
 * 真の捕食者 (True Predator)
 * Devourの強化版。半径20ブロック内の全エンティティにDevourJawを召喚する。
 * ダメージはDevourの2倍。
 * School: Blood / CastType: INSTANT / Cooldown: 10s
 */
public class TruePredatorSpell extends AbstractSpell {

    private static final float RADIUS = 20f;

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "true_predator");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.BLOOD_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(10)
            .build();

    public TruePredatorSpell() {
        this.manaCostPerLevel   = 5;
        this.baseSpellPower     = 6;
        this.spellPowerPerLevel = 1;
        this.castTime           = 0;   // INSTANT
        this.baseManaCost       = 40;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage",
                        Utils.stringTruncation(getDamage(spellLevel, caster), 2)),
                Component.translatable("ui.elementmagicboss.true_root_radius", (int) RADIUS)
        );
    }

    @Override public CastType getCastType()              { return CastType.INSTANT; }
    @Override public DefaultConfig getDefaultConfig()    { return defaultConfig; }
    @Override public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.empty();
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.RAVAGER_ROAR);
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity,
                       CastSource castSource, MagicData playerMagicData) {

        float damage = getDamage(spellLevel, entity);
        int vigor = getHpBonus(spellLevel, entity);
        AABB area = AABB.ofSize(entity.position(), RADIUS * 2, RADIUS * 2, RADIUS * 2);

        // 半径20ブロック内の自分以外の全LivingEntityにDevourJawを召喚
        world.getEntitiesOfClass(LivingEntity.class, area, target ->
                target != entity
                && !DamageSources.isFriendlyFireBetween(entity, target)
                && target.distanceTo(entity) <= RADIUS
        ).forEach(target -> {
            DevourJaw jaw = new DevourJaw(world, entity, target);
            jaw.setPos(target.position());
            jaw.setYRot(entity.getYRot());
            jaw.setDamage(damage);
            jaw.vigorLevel = vigor;
            world.addFreshEntity(jaw);
        });

        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    /** Devourの2倍ダメージ: getSpellPower * 2 */
    public float getDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster) * 2.0f;
    }

    public int getHpBonus(int spellLevel, LivingEntity caster) {
        return 2 * (int) (getSpellPower(spellLevel, caster) * 0.25f);
    }
}
