package fun.drughack.modules.impl.combat;

import fun.drughack.DrugHack;
import fun.drughack.api.events.impl.EventAttackEntity;
import fun.drughack.api.events.impl.EventPlayerTick;
import fun.drughack.api.events.impl.EventTick;
import fun.drughack.modules.api.Category;
import fun.drughack.modules.api.Module;
import fun.drughack.modules.settings.api.Nameable;
import fun.drughack.modules.settings.impl.BooleanSetting;
import fun.drughack.modules.settings.impl.EnumSetting;
import fun.drughack.modules.settings.impl.ListSetting;
import fun.drughack.modules.settings.impl.NumberSetting;
import fun.drughack.screen.clickgui.components.impl.ListComponent;
import fun.drughack.utils.network.ChatUtils;
import fun.drughack.utils.network.NetworkUtils;
import fun.drughack.utils.rotations.RotationChanger;
import fun.drughack.utils.rotations.RotationUtils;
import fun.drughack.utils.world.InventoryUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;

public class SwapMace extends Module {

    private final EnumSetting<InventoryUtils.Switch> autoSwitch = new EnumSetting<>("settings.switch", InventoryUtils.Switch.Silent);
    private final EnumSetting<InventoryUtils.Swing> swing = new EnumSetting<>("settings.swing", InventoryUtils.Swing.MainHand);
    private final NumberSetting switchDelay = new NumberSetting("задержка", 1, 0, 5, 0.1f);

    private int previousSlot = -1;
    private int maceSlot = -1;
    private long switchTime = 0;
    private boolean shouldSwitchBack = false;
    private boolean isSwapping = false;

    public SwapMace() {
        super("Swapper", Category.Movement);
    }

    @EventHandler
    public void onTick(EventTick e) {
        if (fullNullCheck()) return;

        if (shouldSwitchBack && System.currentTimeMillis() - switchTime >= switchDelay.getValue() * 1000) {
            if (previousSlot != -1 && maceSlot != -1) {
                InventoryUtils.switchBack(autoSwitch.getValue(), maceSlot, previousSlot);
                InventoryUtils.switchBack(autoSwitch.getValue(), previousSlot, previousSlot);
            }
            shouldSwitchBack = false;
            isSwapping = false;
        }
    }

    @EventHandler
    public void onAttack(EventAttackEntity e) {
        if (fullNullCheck()) return;
        if (isSwapping) return;
        if (mc.player.getMainHandStack().getItem() == Items.MACE) return;
        int slot = InventoryUtils.findHotbar(Items.MACE);
        if (slot == -1) return;

        isSwapping = true;
        previousSlot = mc.player.getInventory().selectedSlot;
        maceSlot = slot;

        InventoryUtils.switchSlot(autoSwitch.getValue(), slot, previousSlot);




        switchTime = System.currentTimeMillis();
        shouldSwitchBack = true;
    }
}