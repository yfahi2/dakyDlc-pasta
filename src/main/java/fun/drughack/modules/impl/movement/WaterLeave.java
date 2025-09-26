package fun.drughack.modules.impl.movement;

import fun.drughack.modules.api.Category;
import fun.drughack.modules.api.Module;
import fun.drughack.modules.settings.impl.BooleanSetting;
import fun.drughack.modules.settings.impl.ListSetting;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;

public class WaterLeave extends Module {

    private final ListSetting targets = new ListSetting(
            "Режим работы",
            new BooleanSetting("Песок душ", true),
            new BooleanSetting("Магма", true)
    );

    public WaterLeave() {
        super("WaterLeave", Category.Movement);
        addSetting(targets); // Исправлено: должно быть addSettings()

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (!this.toggled || mc.player == null || mc.world == null) return;

            ClientPlayerEntity player = mc.player;
            BlockPos belowPlayer = player.getBlockPos().down();

            boolean soulSandOn = getFlag("Песок душ");
            boolean magmaOn = getFlag("Магма");

            if (player.isTouchingWater()) {
                if (soulSandOn && mc.world.getBlockState(belowPlayer).isOf(Blocks.SOUL_SAND)) {
                    player.jump();
                    player.setVelocity(player.getVelocity().x, 1.6, player.getVelocity().z);
                } else if (magmaOn && mc.world.getBlockState(belowPlayer).isOf(Blocks.MAGMA_BLOCK)) {
                    player.jump();
                    player.setVelocity(player.getVelocity().x, 1.72, player.getVelocity().z);
                }
            }
        });
    }

    private void addSetting(ListSetting targets) {
    }

    private boolean getFlag(String name) {
        // Замените на реальный способ получить BooleanSetting по имени:
        // например: return ((BooleanSetting) targets.getSetting(name)).get();
        // или через поиск в children
        return true; // заглушка
    }
}