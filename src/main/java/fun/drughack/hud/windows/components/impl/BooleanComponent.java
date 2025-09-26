package fun.drughack.hud.windows.components.impl;

import java.awt.Color;

import fun.drughack.hud.windows.components.WindowComponent;
import fun.drughack.modules.settings.impl.BooleanSetting;
import fun.drughack.utils.animations.Animation;
import fun.drughack.utils.animations.Easing;
import fun.drughack.utils.math.MathUtils;
import fun.drughack.utils.render.ColorUtils;
import fun.drughack.utils.render.Render2D;
import fun.drughack.utils.render.fonts.Fonts;
import net.minecraft.client.gui.DrawContext;

public class BooleanComponent extends WindowComponent {

	private final BooleanSetting setting;
	private final Animation toggleAnimation = new Animation(300, 1, false, Easing.LINEAR);

	public BooleanComponent(String name, BooleanSetting setting) {
		super(name);
		this.setting = setting;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		toggleAnimation.update(setting.getValue());


		float textWidth = Fonts.BOLD.getFont(8f).getWidth(getName());


		float toggleX = x + textWidth + 10f;
		float toggleY = y + 4.5f;


		Render2D.drawFont(context.getMatrices(), Fonts.BOLD.getFont(8f), getName(), x + 5f, y + 4f, new Color(255, 255, 255, (int) (255 * animation.getValue())));

		Render2D.drawRoundedRect(context.getMatrices(), toggleX, toggleY, 0f * toggleAnimation.getValue() , 8f, 2.5f, new Color(23, 23, 23, 100));
		Render2D.drawRoundedRect(context.getMatrices(), toggleX, toggleY, 16f * toggleAnimation.getValue(), 8f, 2.5f, ColorUtils.getGlobalColor((int) (255 * toggleAnimation.getLinear())));
		//	Render2D.drawRoundedRect(context.getMatrices(), toggleX + 16f - (16f * toggleAnimation.getValue()), toggleY, 16f * toggleAnimation.getReversedValue(), 8f, 2.5f, new Color(23, 23, 23, 100));

		Render2D.drawRoundedRect(context.getMatrices(), toggleX + 0.5f + (8f * toggleAnimation.getValue()), toggleY + 0.5f, 7f, 7f, 2.5f, Color.WHITE);

	}

	@Override
	public void mouseClicked(double mouseX, double mouseY, int button) {
		// Получаем ширину текста для позиционирования области клика
		float textWidth = Fonts.BOLD.getFont(8f).getWidth(getName());
		float toggleX = x + textWidth + 10f;
		float toggleY = y + 4.5f;
		if (MathUtils.isHovered(toggleX, toggleY, 16f, 8f, (float) mouseX, (float) mouseY) && button == 0) {
			setting.setValue(!setting.getValue());
		}
	}

	@Override
	public void mouseReleased(double mouseX, double mouseY, int button) {

	}

	@Override
	public void keyPressed(int keyCode, int scanCode, int modifiers) {

	}

	@Override
	public void keyReleased(int keyCode, int scanCode, int modifiers) {

	}

	@Override
	public void charTyped(char chr, int modifiers) {

	}
}