package fun.drughack.modules.impl.movement;

import fun.drughack.DrugHack;
import fun.drughack.api.events.impl.EventPacket;
import fun.drughack.api.events.impl.EventPlayerTick;
import fun.drughack.modules.api.Category;
import fun.drughack.modules.api.Module;
import fun.drughack.modules.impl.combat.Aura;
import fun.drughack.modules.settings.impl.BooleanSetting;
import fun.drughack.modules.settings.impl.EnumSetting;
import fun.drughack.modules.settings.impl.NumberSetting;
import fun.drughack.utils.math.MathUtils;
import fun.drughack.utils.math.TimerUtils;
import fun.drughack.utils.movement.MoveUtils;
import fun.drughack.utils.network.NetworkUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.MathHelper;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class FakeLagDynamic extends Module {

    private enum Mode { Constant, Dynamic }

    private final EnumSetting<Mode> mode =
            new EnumSetting<>("Mode", Mode.Dynamic, () -> true, new Mode[]{Mode.Constant, Mode.Dynamic});

    private final NumberSetting rangeMin   = new NumberSetting("MinRange",        2.0f,   0.0f,  10.0f, 0.1f);
    private final NumberSetting rangeMax   = new NumberSetting("MaxRange",        5.0f,   0.0f,  10.0f, 0.1f);
    private final NumberSetting delayMin   = new NumberSetting("MinDelay(ms)",  300.0f,   0.0f, 1000.0f, 1.0f);
    private final NumberSetting delayMax   = new NumberSetting("MaxDelay(ms)",  600.0f,   0.0f, 1000.0f, 1.0f);
    private final NumberSetting recoilTime = new NumberSetting("RecoilTime(ms)", 250.0f,   0.0f, 1000.0f, 1.0f);

    private final BooleanSetting flushOnEntityInteract = new BooleanSetting("Flush:EntityInteract", true);
    private final BooleanSetting flushOnBlockInteract  = new BooleanSetting("Flush:BlockInteract",  true);
    private final BooleanSetting flushOnAction         = new BooleanSetting("Flush:Action",         true);
    private final BooleanSetting flushOnVelocity       = new BooleanSetting("Flush:Velocity",       true);
    private final BooleanSetting flushOnExplosion      = new BooleanSetting("Flush:Explosion",      true);
    private final BooleanSetting flushOnHealth         = new BooleanSetting("Flush:Health",         true);
    private final BooleanSetting flushOnServerPos      = new BooleanSetting("Flush:ServerPos",      true);
    private final BooleanSetting flushOnResourceStatus = new BooleanSetting("Flush:ResourceStatus", true);

    private final BooleanSetting onlyMoving = new BooleanSetting("OnlyMoving",     false);
    private final BooleanSetting onlyGround = new BooleanSetting("OnlyGround",     false);
    private final BooleanSetting onlyTarget = new BooleanSetting("OnlyTargetAura", false);

    public FakeLagDynamic() {
        super("FakeLag", Category.Movement);
        getSettings().addAll(Arrays.asList(
                mode,
                rangeMin, rangeMax,
                delayMin, delayMax, recoilTime,
                flushOnEntityInteract, flushOnBlockInteract, flushOnAction,
                flushOnVelocity, flushOnExplosion, flushOnHealth, flushOnServerPos, flushOnResourceStatus,
                onlyMoving, onlyGround, onlyTarget
        ));
    }

    private final List<Packet<?>> queue = new CopyOnWriteArrayList<>();
    private final TimerUtils sinceLastFlush = new TimerUtils();
    private final TimerUtils sinceEnqueue   = new TimerUtils();
    private long nextDelayMs = 500L;
    private boolean enemyNearby = false;

    @Override
    public void onEnable() {
        super.onEnable();
        queue.clear();
        sinceLastFlush.reset();
        sinceEnqueue.reset();
        nextDelayMs = randomDelay();
        enemyNearby = false;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        flushAll();
        enemyNearby = false;
    }

    @EventHandler
    public void onOutgoing(EventPacket.Send e) {
        if (fullNullCheck()) return;
        if (!shouldWork()) { flushAll(); return; }

        if (shouldFlushOnOutgoing(e.getPacket())) { flushAll(); return; }

        if (!shouldLagNow()) return;

        if (sinceEnqueue.passed(nextDelayMs)) {
            flushAll();
            nextDelayMs = randomDelay();
            return;
        }

        if (e.getPacket() instanceof PlayerMoveC2SPacket) {
            queue.add(e.getPacket());
            e.cancel();
        }
    }

    @EventHandler
    public void onIncoming(EventPacket.Receive e) {
        if (fullNullCheck()) return;
        if (!shouldWork()) { flushAll(); return; }

        if (shouldFlushOnIncoming(e.getPacket())) {
            flushAll();
        }
    }

    @EventHandler
    public void onTick(EventPlayerTick e) {
        if (fullNullCheck()) return;

        enemyNearby = findEnemyInRange(randomRange());

        long recoilMs = ((Number) recoilTime.getValue()).longValue();
        if (!sinceLastFlush.passed(recoilMs)) return;

        if (sinceEnqueue.passed(nextDelayMs)) {
            flushAll();
            nextDelayMs = randomDelay();
        }
    }

    private boolean shouldWork() {
        if (mc.player == null || mc.world == null) return false;
        if (onlyTarget.getValue()
                && DrugHack.getInstance().getModuleManager().getModule(Aura.class).getTarget() == null) return false;
        if (onlyMoving.getValue() && !MoveUtils.isMoving()) return false;
        if (onlyGround.getValue() && !mc.player.isOnGround()) return false;
        if (mc.player.isUsingItem()) return false;
        if (mc.player.horizontalCollision) return false;
        return true;
    }

    private boolean shouldLagNow() {
        Mode m = mode.getValue();
        if (m == Mode.Constant) return true;
        if (m == Mode.Dynamic)  return enemyNearby;
        return false;
    }

    private long randomDelay() {
        double min = ((Number) delayMin.getValue()).doubleValue();
        double max = ((Number) delayMax.getValue()).doubleValue();
        if (min > max) { double t = min; min = max; max = t; }
        float val = MathUtils.randomFloat((float) min, (float) max);
        return (long) val;
    }

    private float randomRange() {
        double min = ((Number) rangeMin.getValue()).doubleValue();
        double max = ((Number) rangeMax.getValue()).doubleValue();
        if (min > max) { double t = min; min = max; max = t; }
        return MathUtils.randomFloat((float) min, (float) max);
    }

    private boolean findEnemyInRange(float range) {
        double r2 = MathHelper.square(range);
        for (Entity ent : mc.world.getEntities()) {
            if (!(ent instanceof LivingEntity) || ent == mc.player) continue;
            if (ent.isSpectator() || !ent.isAlive()) continue;
            if (mc.player.squaredDistanceTo(ent) <= r2) return true;
        }
        return false;
    }

    private boolean shouldFlushOnOutgoing(Packet<?> p) {
        if (p == null) return false;
        if (flushOnResourceStatus.getValue() && p instanceof ResourcePackStatusC2SPacket) return true;
        if (flushOnAction.getValue()         && p instanceof PlayerActionC2SPacket)     return true;
        if (flushOnEntityInteract.getValue() && (p instanceof PlayerInteractEntityC2SPacket || p instanceof HandSwingC2SPacket)) return true;
        if (flushOnBlockInteract.getValue()  && p instanceof PlayerInteractBlockC2SPacket) return true;
        return false;
    }

    private boolean shouldFlushOnIncoming(Packet<?> p) {
        if (p == null) return false;
        if (flushOnServerPos.getValue() && p instanceof PlayerPositionLookS2CPacket) return true;

        if (flushOnVelocity.getValue() && p instanceof EntityVelocityUpdateS2CPacket v) {
            if (mc.player != null && v.getEntityId() == mc.player.getId()) {
                double vx = Math.abs(v.getVelocityX() / 8000.0);
                double vy = Math.abs(v.getVelocityY() / 8000.0);
                double vz = Math.abs(v.getVelocityZ() / 8000.0);
                if (vx > 0.0 || vy > 0.0 || vz > 0.0) return true;
            }
        }

        if (flushOnExplosion.getValue() && p instanceof ExplosionS2CPacket ex) {
            if (ex.playerKnockback().isPresent()) {
                var kb = ex.playerKnockback().get();
                if (Math.abs(kb.getX()) > 0.0 || Math.abs(kb.getY()) > 0.0 || Math.abs(kb.getZ()) > 0.0) return true;
            }
        }

        if (flushOnHealth.getValue() && p instanceof HealthUpdateS2CPacket) return true;
        return false;
    }

    private void flushAll() {
        if (queue.isEmpty()) return;
        for (Packet<?> packet : queue) {
            NetworkUtils.sendSilentPacket(packet);
        }
        queue.clear();
        sinceLastFlush.reset();
        sinceEnqueue.reset();
    }
}