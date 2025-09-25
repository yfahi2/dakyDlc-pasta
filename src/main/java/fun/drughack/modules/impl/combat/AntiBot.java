package fun.drughack.modules.impl.combat;

import fun.drughack.api.events.impl.EventTick;
import fun.drughack.modules.api.Category;
import fun.drughack.modules.api.Module;
import fun.drughack.utils.math.TimerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;

public class AntiBot extends Module {

    public AntiBot() {
        super("AntiBot", Category.Combat);
    }

    public final List<PlayerEntity> bots = new ArrayList<>();
    private final TimerUtils timer = new TimerUtils();

    @EventHandler
    public void onTick(EventTick e) {
        if (fullNullCheck()) return;

        if (timer.passed(10000) && !bots.isEmpty()) {
        	bots.clear();
            timer.reset();
        }

        //bots.forEach(bot -> {
        	//mc.world.removeEntity(bot.getId(), Entity.RemovalReason.KILLED);
        //});
        
        //if (mc.crosshairTarget instanceof EntityHitResult result) {
        	//if (!result.getEntity().getUuid().equals(UUID.nameUUIDFromBytes(("OfflinePlayer:" + result.getEntity().getName().getString()).getBytes(StandardCharsets.UTF_8))))
        		//ChatUtils.sendMessage("Bot: " + result.getEntity().getName().getString());
        //}
        
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == null) continue;
            if (player == mc.player) continue;
            //rw bypasses
            if (armorCheck(player) && !bots.contains(player)) bots.add(player);
        }
    }

    private boolean armorCheck(PlayerEntity entity) {
        return (getArmor(entity, 3).getItem() == Items.LEATHER_HELMET && isNotColored(entity, 3) && !getArmor(entity, 3).hasEnchantments()
                || getArmor(entity, 2).getItem() == Items.LEATHER_CHESTPLATE && isNotColored(entity, 2) && !getArmor(entity, 2).hasEnchantments()
                || getArmor(entity, 1).getItem() == Items.LEATHER_LEGGINGS && isNotColored(entity, 1) && !getArmor(entity, 1).hasEnchantments()
                || getArmor(entity, 0).getItem() == Items.LEATHER_BOOTS && isNotColored(entity, 0) && !getArmor(entity, 0).hasEnchantments()
                || getArmor(entity, 2).getItem() == Items.IRON_CHESTPLATE && !getArmor(entity, 2).hasEnchantments()
                || getArmor(entity, 1).getItem() == Items.IRON_LEGGINGS && !getArmor(entity, 1).hasEnchantments());
    }
   
    private ItemStack getArmor(PlayerEntity entity, int slot) {
        return entity.getInventory().getArmorStack(slot);
    }
    
    private boolean isNotColored(PlayerEntity entity, int slot) {
        return !getArmor(entity, slot).contains(DataComponentTypes.DYED_COLOR);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (!bots.isEmpty()) bots.clear();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (!bots.isEmpty()) bots.clear();
    }
}