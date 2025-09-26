package fun.drughack.modules.impl.combat;

import fun.drughack.DrugHack;
import fun.drughack.api.events.impl.DrawEvent;
import fun.drughack.api.events.impl.EventPlayerTick;
import fun.drughack.api.events.impl.EventRender3D;
import fun.drughack.modules.api.Category;
import fun.drughack.modules.api.Module;
import fun.drughack.modules.impl.movement.ElytraForward;
import fun.drughack.modules.impl.render.TargetEsp;
import fun.drughack.modules.settings.api.Nameable;
import fun.drughack.modules.settings.impl.*;
import fun.drughack.screen.clickgui.components.impl.ColorSettings;
import fun.drughack.screen.clickgui.components.impl.ColorSettingsC;
import fun.drughack.utils.animations.Animation;
import fun.drughack.utils.animations.Easing;
import fun.drughack.utils.animations.infinity.InfinityAnimation;
import fun.drughack.utils.animations.infinity.RotationAnimation;
import fun.drughack.utils.combat.IdealHitUtils;
import fun.drughack.utils.combat.PredictUtils;
import fun.drughack.utils.math.LegitUtils;
import fun.drughack.utils.math.MathUtils;
import fun.drughack.utils.math.TimerUtils;
import fun.drughack.utils.network.NetworkUtils;
import fun.drughack.utils.network.Server;
import fun.drughack.utils.render.Render3D;
import fun.drughack.utils.rotations.RotationChanger;
import fun.drughack.utils.rotations.RotationUtils;
import fun.drughack.utils.world.InventoryUtils;
import fun.drughack.utils.world.MultipointUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.NonFinal;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.SimplexNoise;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static net.minecraft.util.math.MathHelper.lerp;

public class Aura extends Module {

    private final EnumSetting<Rotate> rotate = new EnumSetting<>("settings.rotate", Rotate.Normal);

    public final NumberSetting range = new NumberSetting("settings.aura.range", 3f, 0f, 6f, 0.05f);
    public final ColorSettings all = new ColorSettings("Target Color", "Цвет ауры");
    public final ColorSettingsC a = new ColorSettingsC("s","s");
    private final NumberSetting aimRange = new NumberSetting("settings.aura.aimrange", 3f, 0f, 6f, 0.05f, () -> rotate.getValue() != Rotate.FuntimeSnap);
    private final NumberSetting elytraAimRange = new NumberSetting("settings.aura.elytraaimrange", 50f, 10f, 100f, 0.05f);
    private final EnumSetting<InventoryUtils.Swing> swing = new EnumSetting<>("settings.swing", InventoryUtils.Swing.MainHand);
    public final EnumSetting<Sprint> sprint = new EnumSetting<>("settings.aura.sprintreset", Sprint.Legit);
    private final BooleanSetting throughWalls = new BooleanSetting("settings.aura.throughwalls", true);
    private final BooleanSetting unpressShield = new BooleanSetting("settings.aura.unpressshield", false);
    private final BooleanSetting breakShield = new BooleanSetting("settings.aura.breakshield", true);
    private final EnumSetting<Priority> priority = new EnumSetting<>("settings.aura.priority", Priority.Distance);
    private final BooleanSetting renderBox = new BooleanSetting("Render Box", true);

    public Aura() {
        super("Aura", Category.Combat);
    }
    @NonFinal
    @Getter
    public static LivingEntity target = null;
    @NonFinal
    private Vec3d currentAimPoint = null;
    private Vec3d multipoint = Vec3d.ZERO;
    @Getter private float[] currentRotations = new float[2], targetRotations = new float[2];
    private final TimerUtils backTimer = new TimerUtils(), attackTimer = new TimerUtils();
    private final RotationAnimation shakingAnimation = new RotationAnimation(Easing.LINEAR, Easing.LINEAR);
    private final InfinityAnimation pitchAnimationInf = new InfinityAnimation(Easing.LINEAR);
    private final Animation yawAnimation = new Animation(300, 1, false, Easing.LINEAR);
    private final Animation pitchAnimation = new Animation(300, 1, false, Easing.LINEAR);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final RotationChanger changer = new RotationChanger(
            10000,
            () -> new Float[]{currentRotations[0], currentRotations[1]},
            () -> fullNullCheck() || target == null
    );


    private Box box;

    @EventHandler
    public void onPlayerTick(EventPlayerTick e) {
        if (fullNullCheck()) return;


        executor.submit(() -> {
            target = findTarget();
            multipoint = target == null ? Vec3d.ZERO : getMultipoint(target);
        });


        if (target != null || !backTimer.passed(250)) {
            handleTargetRotation();

            if (target != null) {
                applyRotationStrategy();
                if (shouldAttack()) attack();
            }
        } else if (target == null) {
            backTimer.reset();
        }
    }
    @EventHandler
    public void onRender(DrawEvent e) {
        if (currentAimPoint != null) {
            //  renderAimPoint(e.getMatrixStack());
        }
    }

    private void renderAimPoint(MatrixStack matrixStack) {
        if (currentAimPoint == null) return;

        double boxSize = 0.1;
        Box box = new Box(
                currentAimPoint.x - boxSize / 2,
                currentAimPoint.y - boxSize / 2,
                currentAimPoint.z - boxSize / 2,
                currentAimPoint.x + boxSize / 2,
                currentAimPoint.y + boxSize / 2,
                currentAimPoint.z + boxSize / 2
        );

        //   Render3D.renderBoxCC(matrixStack, box, new Color(255, 0, 0, 255));
    }
    private void handleTargetRotation() {
        float[] elytraRotations = new float[2];

        if (target != null) {
            // Получаем предсказанные позиции для элитр и обычного режима
            Vec3d targetPos = target.getPos().add(0, target.getHeight() / 2, 0);
            elytraRotations = RotationUtils.getRotations(PredictUtils.predict(target, targetPos, 0));

            if (DrugHack.getInstance().getModuleManager().getModule(ElytraForward.class).isToggled()) {
                int predict = DrugHack.getInstance().getModuleManager().getModule(ElytraForward.class).forward.getValue().intValue();
                elytraRotations = RotationUtils.getRotations(PredictUtils.predict(target, targetPos, predict));
            }

            targetRotations = RotationUtils.getRotations(targetPos);
        }


        currentRotations = mc.player.isGliding() ? elytraRotations : calculateCurrentRotations();
    }

    private float[] calculateCurrentRotations() {
        switch (rotate.getValue()) {
            case FuntimeSnap:
                return handleFuntimeSnap();
            case Smoothness:
                return handleSmoothness();
            case SnapHW:
                return handleSnapHW();
            default:
                return handleSmoothness();
        }
    }

    private float[] handleFuntimeSnap() {
        yawAnimation.setDuration(700);
        pitchAnimation.setDuration(500);
        yawAnimation.update();
        pitchAnimation.update();

        float[] rotations = target == null ? new float[2]
                : RotationUtils.getRotations(target.getPos().add(0, target.getHeight() / 2, 0));

        rotations[0] = (((((rotations[0] - mc.player.getYaw()) % 360) + 540) % 360) - 180);

        if (target != null) {
            boolean shouldRandomize = !(IdealHitUtils.findFall(0) || IdealHitUtils.canCritical(target));

            if (shouldRandomize) {
                rotations[0] = yawAnimation.getValue() * 40 - 20;
                rotations[1] += pitchAnimation.getValue() * 15 - 7;
            } else {
                rotations[0] += MathUtils.randomInt(5, 10);
                rotations[1] += MathUtils.randomInt(5, 30);
            }
        }


        float headYaw = mc.player.getYaw() +
                MathUtils.getStep(currentRotations[0] - mc.player.getYaw(), rotations[0], MathUtils.randomInt(120, 140));

        float headPitch = pitchAnimationInf.animate(rotations[1], 160);
        headPitch = MathHelper.clamp(headPitch, -90, 90);


        float bodyYaw = mc.player.getYaw() +
                MathUtils.getStep(currentRotations[0] - mc.player.getYaw(), rotations[0], MathUtils.randomInt(80, 85));




        currentRotations[0] = headYaw;
        currentRotations[1] = headPitch;

        return RotationUtils.correctRotation(currentRotations);
    }

    private float[] handleSmoothness() {
        yawAnimation.setDuration(700);
        pitchAnimation.setDuration(500);
        yawAnimation.update();
        pitchAnimation.update();

        if (target == null) return currentRotations;

        float[] rotations = RotationUtils.getRotations(target.getPos().add(0, target.getHeight() / 2, 0));
        rotations[0] = MathHelper.wrapDegrees(rotations[0] - mc.player.getYaw());
        rotations[1] = MathHelper.clamp(rotations[1], -89.9f, 89.9f);

        // Yaw
        float yawDiff = rotations[0];
        float t = Math.abs(yawDiff) / 180f;
        float eased = (float) (1 - Math.pow(1 - t, 3));
        int yawSpeed = (int) (80 + eased * 40);
        currentRotations[0] = mc.player.getYaw() + MathUtils.getStep(currentRotations[0] - mc.player.getYaw(), yawDiff, yawSpeed);

        // Pitch
        int pitchSpeed = MathUtils.randomInt(80, 140);
        currentRotations[1] = pitchAnimationInf.animate(rotations[1], pitchSpeed);

        // Добавим небольшой шум
        currentRotations[0] += (float) (SimplexNoise.noise((float) (System.nanoTime() * 1e-9), (float) 0) * 0.5);
        currentRotations[1] += (float) (SimplexNoise.noise(0, (float) (System.nanoTime() * 1e-9)) * 0.3);

        return RotationUtils.correctRotation(currentRotations);
    }

    private float[] handleSnapHW() {
        yawAnimation.setDuration(700);
        pitchAnimation.setDuration(500);
        yawAnimation.update();
        pitchAnimation.update();

        float[] rotations = target == null ? new float[2] : RotationUtils.getRotations(target.getPos().add(0, target.getHeight() / 2, 0));
        rotations[0] = (((((rotations[0] - mc.player.getYaw()) % 360) + 540) % 360) - 180);

        if (target != null) {
            boolean isCriticalJump = isJumpingForCritical();
            boolean shouldTurnBack =  !attackTimer.passed(250) && !isCriticalJump;


            if (shouldTurnBack) {
                rotations[0] = yawAnimation.getValue() ;
                rotations[1] += pitchAnimation.getValue() ;
            } else {
                int yawRandom = isCriticalJump ? MathUtils.randomInt(2, 5) : MathUtils.randomInt(5, 10);
                int pitchRandom = isCriticalJump ? MathUtils.randomInt(2, 10) : MathUtils.randomInt(5, 30);

                rotations[0] += yawRandom % RotationUtils.getGCDValue();
                rotations[1] += pitchRandom % RotationUtils.getGCDValue();
            }
        }

        // Настраиваем скорость поворота в зависимости от прыжка для крита
        int yawStep = isJumpingForCritical() ? MathUtils.randomInt(100, 120) : MathUtils.randomInt(80, 85);
        currentRotations[0] = mc.player.getYaw() + MathUtils.getStep(currentRotations[0] - mc.player.getYaw(), rotations[0], yawStep);

        int pitchSpeed = isJumpingForCritical() ? MathUtils.randomInt(50, 80) : MathUtils.randomInt(100, 150);
        currentRotations[1] = pitchAnimationInf.animate(rotations[1], (long) pitchSpeed);
        currentRotations[1] = MathHelper.clamp(currentRotations[1], -90, 90);

        return RotationUtils.correctRotation(currentRotations);
    }

    private void applyRotationStrategy() {
        if (rotate.getValue() == Rotate.Packet) {
            DrugHack.getInstance().getRotationManager().addPacketRotation(currentRotations);
        } else {
            DrugHack.getInstance().getRotationManager().addRotation(changer);
        }
    }

    public static float[] getShortestRotation(float currentYaw, float currentPitch,
                                              float targetYaw, float targetPitch) {
        // Минимальный yaw
        float yawDiff = targetYaw - currentYaw;
        yawDiff = yawDiff - Math.round(yawDiff / 360) * 360;

        // Минимальный pitch (уже ограничен -90..90)
        float pitchDiff = targetPitch - currentPitch;

        return new float[]{yawDiff, pitchDiff};
    }

    private float normalizePitch(float pitch) {
        pitch = Math.max(-90, Math.min(90, pitch));
        return pitch;
    }
    @EventHandler
    public void onGameRender3D(EventRender3D.Game e) {
        if (fullNullCheck() || target == null) return;

        if (renderBox.getValue()) {

            Render3D.renderCube(
                    e.getMatrixStack(),
                    target.getBoundingBox(),
                    true,
                    new Color(255, 0, 0, 0),
                    true,
                    Color.RED
            );

        }

        if (DrugHack.getInstance().getModuleManager().getModule(ElytraForward.class).isToggled()
                && target != null) {
            int predict = DrugHack.getInstance().getModuleManager().getModule(ElytraForward.class).forward.getValue().intValue();
            Vec3d predictedPos = PredictUtils.predict(target, target.getPos().add(0, target.getHeight() / 2, 0), predict);
            Box predictedBox = target.getBoundingBox().offset(predictedPos.subtract(target.getPos()));
            if (renderBox.getValue()) {
                Render3D.renderBoxС(
                        e.getMatrixStack(),
                        predictedBox,
                        all.getAwtColor().getRGB()
                );
            }

        }
    }

    private LivingEntity findTarget() {
        List<LivingEntity> targets = new ArrayList<>();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (!isValidTarget(living)) continue;
            targets.add(living);
        }

        if (targets.isEmpty() || !isToggled()) return null;

        switch (priority.getValue()) {
            case Distance -> targets.sort(Comparator.comparingDouble(entity ->
                    MultipointUtils.getClosestPoint(entity, 25, 25, getMaxAimRange())
                            .squaredDistanceTo(mc.player.getEyePos())
            ));

            case Health -> targets.sort(Comparator.comparingDouble(LivingEntity::getHealth));

            case Angle -> targets.sort(Comparator.comparingDouble(entity -> {
                Vec3d targetVec = entity.getPos().add(0, entity.getHeight() / 2, 0);
                float[] rotations = RotationUtils.getRotations(targetVec);
                float yawDiff = Math.abs(MathHelper.wrapDegrees(rotations[0] - mc.player.getYaw()));
                float pitchDiff = Math.abs(MathHelper.wrapDegrees(rotations[1] - mc.player.getPitch()));
                return yawDiff + pitchDiff;
            }));
        }

        return targets.getFirst();
    }

    private Vec3d getMultipoint(LivingEntity entity) {
        return MultipointUtils.getClosestPoint(entity, 25, 25, rotate.getValue() != Rotate.FuntimeSnap ? getMaxAimRange() : range.getValue());
    }

    private void attack() {
        if (unpressShield.getValue() && mc.player.isBlocking()) mc.interactionManager.stopUsingItem(mc.player);
        if (sprint.getValue() == Sprint.Hvh
                && DrugHack.getInstance().getServerManager().isServerSprinting()
        ) NetworkUtils.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
        if (target instanceof PlayerEntity player && player.isBlocking() && breakShield.getValue()) shieldBreak(player);
        else mc.interactionManager.attackEntity(mc.player, target);
        InventoryUtils.swing(swing.getValue());
        if (sprint.getValue() == Sprint.Hvh && !DrugHack.getInstance().getServerManager().isServerSprinting()) NetworkUtils.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
        if (sprint.getValue() == Sprint.FURRY && attackTimer.passed(200)

        ) mc.player.setSprinting(true);

        attackTimer.reset();
    }

    private void shieldBreak(PlayerEntity entity) {
        int slot = InventoryUtils.findBestAxe(0, 8);
        int previousSlot = mc.player.getInventory().selectedSlot;
        if (slot == -1) return;
        InventoryUtils.switchSlot(InventoryUtils.Switch.Silent, slot, previousSlot);
        mc.interactionManager.attackEntity(mc.player, entity);
        InventoryUtils.swing(swing.getValue());
        InventoryUtils.switchBack(InventoryUtils.Switch.Silent, previousSlot, previousSlot);
    }

    private boolean isWeapon() {
        return 	mc.player.getMainHandStack().getItem() != Items.AIR
                && (mc.player.getMainHandStack().getItem() instanceof SwordItem
                || mc.player.getMainHandStack().getItem() instanceof PickaxeItem
                || mc.player.getMainHandStack().getItem() instanceof AxeItem
                || mc.player.getMainHandStack().getItem() instanceof HoeItem
                || mc.player.getMainHandStack().getItem() instanceof ShovelItem
                || mc.player.getMainHandStack().getItem() == Items.MACE);
    }

    private boolean isValidTarget(LivingEntity entity) {
        if (entity == null) return false;
        if (rotate.getValue() != Rotate.FuntimeSnap && getMultipoint(entity).squaredDistanceTo(mc.player.getEyePos()) > MathHelper.square(getMaxAimRange())) return false;
        if (rotate.getValue() == Rotate.FuntimeSnap && getMultipoint(entity).squaredDistanceTo(mc.player.getEyePos()) > MathHelper.square(range.getValue())) return false;
        if (!throughWalls.getValue() && !mc.player.canSee(entity)) return false;

        return Server.isValid(entity);
    }
    private boolean isJumpingForCritical() {
        if (mc.player.isOnGround()) return false;
        if (mc.player.getVelocity().y <= 0) return false;


        double verticalDelta = mc.player.getY() - Math.floor(mc.player.getY());
        return verticalDelta < 0.4;
    }
    private boolean shouldAttack() {
        if (!attackTimer.passed(70)) return false;
        attackTimer.reset();
        if (mc.player.getAttackCooldownProgress(0f) < IdealHitUtils.getAICooldown()) return false;
        if ((rotate.getValue() == Rotate.Normal || rotate.getValue() == Rotate.Smoothness)
                && !mc.player.isGliding()
                && !MathUtils.inFov(target.getPos(), 30, currentRotations[0])) return false;
        if (multipoint.squaredDistanceTo(mc.player.getEyePos() ) >= MathHelper.square(range.getValue())) return false;



        if (DrugHack.getInstance().getModuleManager().getModule(Criticals.class).isToggled()) {
            return isJumpingForCritical();
        } else {
            return IdealHitUtils.canCritical(target);
        }
    }

    private float getMaxAimRange() {
        return mc.player.isGliding() && rotate.getValue() != Rotate.FuntimeSnap	? elytraAimRange.getValue()
                : rotate.getValue() != Rotate.FuntimeSnap ? range.getValue() + aimRange.getValue()
                : 0;
    }

    @AllArgsConstructor
    public enum Priority implements Nameable {
        Distance("settings.aura.priority.distance"),
        Health("settings.aura.priority.health"),
        Angle("settings.aura.priority.angle"),
        None("settings.none");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }

    @AllArgsConstructor
    public enum Rotate implements Nameable {
        Normal("settings.normal"),
        Smoothness("settings.aura.rotate.smoothness"),
        FuntimeSnap("settings.aura.rotate.funtimesnap"),
        Packet("settings.packet"),
        SnapHW("SnapHW"),
        None("settings.none");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }

    @AllArgsConstructor
    public enum Esp implements Nameable {
        Gost("Gost"),
        Nurik("Nurik");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }

    @AllArgsConstructor
    public enum Sprint implements Nameable {
        Hvh("settings.aura.sprintreset.hvh"),
        FURRY("FURRY"),
        Legit("settings.aura.sprintreset.legit");


        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (target != null) backTimer.reset();
        target = null;
    }
}