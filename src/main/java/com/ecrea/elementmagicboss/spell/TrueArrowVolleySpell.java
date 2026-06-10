package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.ModEntities;
import com.ecrea.elementmagicboss.entity.projectile.TrueArrowVolleyEntity;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class TrueArrowVolleySpell extends AbstractSpell {

    private static final int ARROW_MULTIPLIER = 10;

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "true_arrow_volley");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(6)
            .setCooldownSeconds(15)
            .build();

    public TrueArrowVolleySpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 8;
        this.spellPowerPerLevel = 1;
        this.castTime = 30;
        this.baseManaCost = 40;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage",
                        Utils.stringTruncation(getDamage(spellLevel, caster), 1)),
                Component.translatable("ui.irons_spellbooks.projectile_count", getCount(spellLevel))
        );
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.ARROW_VOLLEY_PREPARE.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.EVOKER_CAST_SPELL);
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        Utils.preCastTargetHelper(level, entity, playerMagicData, this, 48, .25f, false);
        return true;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        Vec3 targetLocation = null;
        if (playerMagicData.getAdditionalCastData() instanceof TargetEntityCastData castTargetingData) {
            targetLocation = castTargetingData.getTargetPosition((ServerLevel) level);
        }
        if (targetLocation == null) {
            targetLocation = Utils.raycastForEntity(level, entity, 100, true).getLocation();
        }

        Vec3 backward = new Vec3(targetLocation.x - entity.getX(), 0, targetLocation.z - entity.getZ()).normalize().scale(-4);
        Vec3 raycastTarget = Utils.moveToRelativeGroundLevel(level, targetLocation.add(0, 2, 0), 4).add(backward).add(0, 6, 0);
        Vec3 spawnLocation = Utils.raycastForBlock(level, targetLocation, raycastTarget, ClipContext.Fluid.NONE).getLocation();
        spawnLocation = spawnLocation.subtract(targetLocation).scale(.9f).add(targetLocation);

        float dx = Mth.sqrt((float) ((spawnLocation.x - targetLocation.x) * (spawnLocation.x - targetLocation.x)
                + (spawnLocation.z - targetLocation.z) * (spawnLocation.z - targetLocation.z)));
        float arrowAngleX = dx == 0 ? 70 : (float) (Mth.atan2(dx, (spawnLocation.y - targetLocation.y)) * Mth.RAD_TO_DEG);
        float arrowAngleY = entity.getX() == targetLocation.x && entity.getZ() == targetLocation.z
                ? (entity.getYRot() - 90) * Mth.DEG_TO_RAD
                : Utils.getAngle(entity.getX(), entity.getZ(), targetLocation.x, targetLocation.z);

        TrueArrowVolleyEntity arrowVolleyEntity = new TrueArrowVolleyEntity(ModEntities.TRUE_ARROW_VOLLEY.get(), level);
        arrowVolleyEntity.moveTo(spawnLocation);
        arrowVolleyEntity.setYRot(arrowAngleY * Mth.RAD_TO_DEG + 90);
        arrowVolleyEntity.setXRot(arrowAngleX + 25);
        arrowVolleyEntity.setDamage(getDamage(spellLevel, entity));
        arrowVolleyEntity.setArrowsPerRow(getArrowsPerRow(spellLevel));
        arrowVolleyEntity.setRows(getRows(spellLevel));
        arrowVolleyEntity.setOwner(entity);
        level.addFreshEntity(arrowVolleyEntity);

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private int getCount(int spellLevel) {
        return getRows(spellLevel) * getArrowsPerRow(spellLevel);
    }

    private int getRows(int spellLevel) {
        return 4 + spellLevel;
    }

    private int getArrowsPerRow(int spellLevel) {
        return (5 + spellLevel / 2) * ARROW_MULTIPLIER;
    }

    private float getDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster) * 1.25f;
    }

    @Override
    public SpellDamageSource getDamageSource(Entity projectile, Entity attacker) {
        return super.getDamageSource(projectile, attacker).setIFrames(0);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.CHARGE_RAISED_HAND;
    }
}
