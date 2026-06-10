package com.ecrea.elementmagicboss.entity.ai;

import com.ecrea.elementmagicboss.entity.AngelRazielEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Angel Razielの第1フェーズ（飛行）用移動AI
 */
public class AngelRazielFlyGoal extends Goal {
    private final AngelRazielEntity entity;

    public AngelRazielFlyGoal(AngelRazielEntity entity) {
        this.entity = entity;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return entity.getPhase() == 1 && entity.getTarget() != null;
    }

    @Override
    public void tick() {
        LivingEntity target = entity.getTarget();
        if (target == null) return;

        double distSq = entity.distanceToSqr(target);
        
        // ターゲットの上空 10ブロック前後を維持
        double targetY = target.getY() + 10.0D;
        double speed = 0.8D;

        // 距離が近すぎる場合は「後ずさり」する（10ブロック以内）
        if (distSq < 100.0D) { 
             // Navigationを使うと、目標地点への最短経路をとろうとして後ろを向いてしまう。
             // これを避けるため、Navigationを停止し、直接ベクトル加算で後退させる。
             entity.getNavigation().stop();
             
             Vec3 currentPos = entity.position();
             Vec3 targetPos = target.position();
             // プレイヤーから離れる水平ベクトル
             Vec3 backDir = new Vec3(currentPos.x - targetPos.x, 0, currentPos.z - targetPos.z).normalize();
             
             // 高度を維持しつつ後退するベクトル
             double yAdjust = (targetY - entity.getY()) * 0.1;
             // 加速度を抑えて滑らかに移動
             Vec3 moveVec = backDir.scale(0.04).add(0, yAdjust * 0.5, 0);
             entity.setDeltaMovement(entity.getDeltaMovement().add(moveVec));
        } else if (distSq > 225.0D) { // 15ブロック以上離れているなら近づく
             entity.getNavigation().moveTo(target.getX(), targetY, target.getZ(), speed);
        } else {
            // 適切な距離(10-15)なら周囲を旋回
            if (entity.tickCount % 40 == 0 || entity.getNavigation().isDone()) {
                double angle = entity.getRandom().nextDouble() * Math.PI * 2;
                double offsetX = Math.cos(angle) * 10.0D;
                double offsetZ = Math.sin(angle) * 10.0D;
                entity.getNavigation().moveTo(target.getX() + offsetX, targetY, target.getZ() + offsetZ, speed);
            }
        }
        
        // 回転ロックは解除し、LookControlによる自然な追従に任せる
        entity.getLookControl().setLookAt(target, 30.0F, 30.0F);
    }
}
