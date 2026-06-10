package com.ecrea.elementmagicboss.entity.projectile;

import com.ecrea.elementmagicboss.entity.ModEntities;
import io.redspace.ironsspellbooks.entity.spells.magma_ball.FireBomb;
import io.redspace.ironsspellbooks.entity.spells.magma_ball.FireField;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SeaOfFireBomb extends FireBomb {

    /** FireField の持続時間: MagmaBomb(200tick) × 3 */
    private static final int FIRE_FIELD_DURATION = 600;

    public SeaOfFireBomb(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    public SeaOfFireBomb(Level level, LivingEntity shooter) {
        this(ModEntities.SEA_OF_FIRE_BOMB.get(), level);
        setOwner(shooter);
    }

    /**
     * MagmaBomb の createFireField をオーバーライドして
     * 3倍の持続時間・3倍のサイズで FireField を生成する。
     * radius はスペル側で (基本値 × 3) を setExplosionRadius() で渡す。
     */
    @Override
    public void createFireField(Vec3 location) {
        if (!level().isClientSide()) {
            FireField fire = new FireField(level());
            fire.setOwner(getOwner());
            fire.setDuration(FIRE_FIELD_DURATION);   // 200 → 600 tick (10秒)
            fire.setDamage(getAoeDamage());
            fire.setRadius(getExplosionRadius());     // スペル側で3倍にして渡す
            fire.setCircular();
            fire.moveTo(location);
            level().addFreshEntity(fire);
        }
    }
}
