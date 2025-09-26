package fun.drughack.modules.impl.render;

import fun.drughack.api.events.impl.DrawEvent;
import fun.drughack.modules.api.Category;
import fun.drughack.modules.api.Module;
import fun.drughack.utils.render.BetterColor;
import fun.drughack.utils.render.Render2D;
import fun.drughack.utils.render.VertexUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class Predict extends Module {

    public Predict(){
        super("Predict", Category.Render);
    }

    @EventHandler
    public void renderEvent(DrawEvent event) {


        MatrixStack matrices = event.getMatrixStack();

        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();

        matrices.push();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        VertexConsumerProvider.Immediate immediate = mc.getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer vertexConsumer = immediate.getBuffer(VertexUtils.LINES);

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof ProjectileEntity)) continue;

            if (entity instanceof FireworkRocketEntity) continue;

            AtomicReference<Vec3d> prev = new AtomicReference<>(entity.getPos());
            List<Vec3d> list = calcTrajectory((ProjectileEntity) entity);
            List<Vec3d> list1 = calcTrajectory((ProjectileEntity) entity);
            list.forEach(e -> {
                int color = ColorHelper.lerp(Math.clamp((float) list.indexOf(e) / list.size(), 0f, 1f), new Color(9, 86, 197).getRGB(), 0xFFFF0000);

                vertexConsumer.vertex(matrices.peek().getPositionMatrix(), (float) prev.get().getX(), (float) prev.get().getY(), (float) prev.get().getZ())
                        .color(color)
                        .normal(0,1,0);

                vertexConsumer.vertex(matrices.peek().getPositionMatrix(), (float) e.getX(), (float) e.getY(), (float) e.getZ())
                        .color(color)
                        .normal(0,1,0);


                prev.set(e);
            });
        }

        immediate.drawCurrentLayer();
        matrices.pop();
    }
    public List<Vec3d> calcTrajectory(ProjectileEntity entity) {
        List<Vec3d> out = new ArrayList<>();

        Vec3d vel = entity.getVelocity();
        Vec3d pos = entity.getPos();

        final double gravity = entity.getFinalGravity();
        final double drag = entity.isTouchingWater() ? .8f : .99f;

        if (entity.getVelocity().equals(Vec3d.ZERO)) return List.of();

        for (int i = 0; i < 150; i++) {
            out.add(pos);

            Vec3d next = pos.add(vel);

            BlockHitResult blockHit = mc.world.raycast(new RaycastContext(
                    pos, next,
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    entity
            ));

            if (blockHit.getType() != HitResult.Type.MISS) {
                out.add(blockHit.getPos());
                break;
            }

            pos = next;
            vel = vel.multiply(drag);
            vel = vel.subtract(0, gravity, 0);
        }

        return out;
    }
}
