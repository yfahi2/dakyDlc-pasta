package fun.drughack.hud.impl;

import fun.drughack.DrugHack;
import fun.drughack.api.events.impl.EventRender2D;
import fun.drughack.hud.HudElement;
import fun.drughack.modules.impl.combat.Aura;
import fun.drughack.utils.animations.Easing;
import fun.drughack.utils.animations.infinity.InfinityAnimation;
import fun.drughack.utils.math.MathUtils;
import fun.drughack.utils.network.Server;
import fun.drughack.utils.render.ColorUtils;
import fun.drughack.utils.render.fonts.Fonts;
import fun.drughack.utils.render.Render2D;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.awt.Color;
import java.util.List;

public class TargetHud extends HudElement {
	
    public TargetHud() {
        super("TargetHud");
    }

    private final InfinityAnimation healthAnimation = new InfinityAnimation(Easing.LINEAR);
    private final InfinityAnimation gappleAnimation = new InfinityAnimation(Easing.LINEAR);

    @Override
    public void onRender2D(EventRender2D e) {
        if (fullNullCheck() || closed()) return;

        Aura aura = DrugHack.getInstance().getModuleManager().getModule(Aura.class);
        LivingEntity target = mc.currentScreen instanceof ChatScreen ? mc.player : aura.getTarget();
        if (target == null) return;
		float posX = getX();
		float posY = getY();

		// New layout constants (adapted from requested design)
		float width = 192f;
		float height = 70f;
		float padding = 6f;
		float headSize = 58f;
		float cornerRadius = 12f;
		float nameTextSize = 16f;
		float itemSize = 16f;
		float itemSpacing = 4f;

		float hp = MathUtils.round(Server.getHealth(target, false));
		float maxHp = MathUtils.round(target.getMaxHealth());
		float gappleHp = MathUtils.round(target.getAbsorptionAmount());
		float healthPercent = Math.max(0f, Math.min(1f, hp / Math.max(1f, maxHp)));
		float gapplePercent = Math.max(0f, gappleHp / Math.max(1f, maxHp));

		// Animate widths for smooth transitions
		float hpBarMaxWidth = 116f;
		float hpBarHeight = 18f;
		float animatedHealthWidth = healthAnimation.animate(hpBarMaxWidth * healthPercent, 200);
		float animatedAbsorbWidth = gappleAnimation.animate(hpBarMaxWidth * gapplePercent, 200);
        e.getContext().getMatrices().push();
        e.getContext().getMatrices().translate(posX + width / 2, posY + height / 2, 0f);
        e.getContext().getMatrices().scale(toggledAnimation.getValue(), toggledAnimation.getValue(), 0);
        e.getContext().getMatrices().translate(-(posX + width / 2), -(posY + height / 2), 0f);
		// Equipment will be rendered later in a fixed row; skip early armor pass

		Render2D.startScissor(e.getContext(), posX, posY, width, height);
		// Background with new corner radius and opacity
		Render2D.drawStyledRect(
				e.getContext().getMatrices(),
				posX,
				posY,
				width,
				height,
				cornerRadius,
				new Color(0, 0, 0, 200),
				255
		);

		float headX = posX + padding;
		float headY = posY + padding;

        if (target instanceof PlayerEntity)
            Render2D.drawTexture(
                    e.getContext().getMatrices(),
                    headX,
                    headY,
                    headSize,
                    headSize,
                    2f,
                    0.125f,
                    0.125f,
                    0.125f,
                    0.125f,
                    ((AbstractClientPlayerEntity) target).getSkinTextures().texture(),
                    Color.WHITE
            );
        else {
            Render2D.drawRoundedRect(
                    e.getContext().getMatrices(),
                    headX,
                    headY,
                    headSize,
                    headSize,
                    2f,
                    new Color(20, 20, 20)
            );

            Render2D.drawFont(
                    e.getContext().getMatrices(),
                    Fonts.BOLD.getFont(9f),
                    "?",
                    headX + (headSize / 2f) - Fonts.BOLD.getWidth("?", 9f) / 2f,
                    headY + (headSize / 2f) - Fonts.BOLD.getHeight(9f) / 2f,
                    Color.RED
            );
        }

		// Name (truncated to available width)
		String fullName = target.getName().getString();
		float baseX = headX + headSize + padding;
		float maxNameWidth = width - padding - headSize - padding * 2 - 10f;
		if (!fullName.isEmpty()) {
			String displayName = truncateToWidth(fullName, maxNameWidth, nameTextSize);
			Render2D.drawFont(
					e.getContext().getMatrices(),
					Fonts.MEDIUM.getFont(nameTextSize),
					displayName,
					baseX,
					posY + padding,
					Color.WHITE
			);
		}

		// Health bar area (aligned to bottom with padding)
		float hpBarX = baseX;
		float hpBarY = posY + height - padding - hpBarHeight;

		// Bar background
		Render2D.drawRoundedRect(
				e.getContext().getMatrices(),
				hpBarX,
				hpBarY,
				hpBarMaxWidth,
				hpBarHeight,
				10f / 2f,
				new Color(97, 97, 97, 128)
		);

		// Filled health
		if (animatedHealthWidth > 0.5f) {
			Render2D.drawRoundedRect(
					e.getContext().getMatrices(),
					hpBarX,
					hpBarY,
					animatedHealthWidth,
					hpBarHeight,
					10f / 2f,
					new Color(150, 150, 150, 255)
			);
		}

		// Absorption overlay
		if (gappleHp > 0f && animatedAbsorbWidth > 0.5f) {
			Render2D.drawRoundedRect(
					e.getContext().getMatrices(),
					hpBarX,
					hpBarY,
					Math.min(animatedAbsorbWidth, hpBarMaxWidth),
					hpBarHeight,
					cornerRadius / 2f,
					new Color(222, 189, 0, 255)
			);
		}

		// Percent text centered in the bar
		String percentText = Math.round(healthPercent * 100f) + "%";
		float percentWidth = Fonts.MEDIUM.getWidth(percentText, 12f);
		float percentX = hpBarX + (hpBarMaxWidth - percentWidth) / 2f;
		float percentY = hpBarY + (hpBarHeight - Fonts.MEDIUM.getHeight(12f)) / 2f - 1f;
		Render2D.drawFont(
				e.getContext().getMatrices(),
				Fonts.MEDIUM.getFont(12f),
				percentText,
				percentX,
				percentY,
				Color.WHITE
		);

		// Equipment row (6 slots): HEAD, CHEST, LEGS, FEET, MAINHAND, OFFHAND
		if (target instanceof PlayerEntity player) {
			List<ItemStack> armor = player.getInventory().armor;
			ItemStack head = armor.get(3);
			ItemStack chest = armor.get(2);
			ItemStack legs = armor.get(1);
			ItemStack feet = armor.get(0);
			ItemStack mainHand = player.getMainHandStack();
			ItemStack offHand = player.getOffHandStack();

			float itemsY = hpBarY - 5f - itemSize + 1f;
			float ix = baseX;
			ItemStack[] items = new ItemStack[]{head, chest, legs, feet, mainHand, offHand};
			for (int i = 0; i < items.length; i++) {
				ItemStack stack = items[i];
				if (stack != null && !stack.isEmpty()) {
					e.getContext().drawItem(stack, (int) ix, (int) itemsY);
				}
				ix += itemSize + itemSpacing;
			}
		}

        Render2D.stopScissor(e.getContext());
        e.getContext().getMatrices().pop();
        setBounds(getX(), getY(), width, height);
        super.onRender2D(e);
    }

	private String truncateToWidth(String text, float maxWidth, float textSize) {
		if (Fonts.MEDIUM.getWidth(text, textSize) <= maxWidth) return text;
		String ellipsis = "...";
		float ellipsisWidth = Fonts.MEDIUM.getWidth(ellipsis, textSize);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			sb.append(c);
			if (Fonts.MEDIUM.getWidth(sb.toString(), textSize) + ellipsisWidth > maxWidth) {
				// remove last char which overflowed
				sb.deleteCharAt(sb.length() - 1);
				break;
			}
		}
		return sb.append(ellipsis).toString();
	}
}