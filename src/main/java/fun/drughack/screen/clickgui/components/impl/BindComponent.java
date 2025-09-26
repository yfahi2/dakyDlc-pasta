package fun.drughack.screen.clickgui.components.impl;

import java.awt.Color;

import org.lwjgl.glfw.GLFW;

import fun.drughack.modules.settings.api.Bind;
import fun.drughack.modules.settings.impl.BindSetting;
import fun.drughack.screen.clickgui.components.Component;
import fun.drughack.utils.animations.Animation;
import fun.drughack.utils.animations.Easing;
import fun.drughack.utils.animations.infinity.InfinityAnimation;
import fun.drughack.utils.math.MathUtils;
import fun.drughack.utils.render.ColorUtils;
import fun.drughack.utils.render.Render2D;
import fun.drughack.utils.render.fonts.Fonts;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.resource.language.I18n;

public class BindComponent extends Component {

	private final BindSetting setting;
	private final InfinityAnimation animation = new InfinityAnimation(Easing.LINEAR);
	private final Animation bindingAnimation = new Animation(300, 1f, false, Easing.BOTH_SINE);
	private boolean binding;

	public BindComponent(BindSetting setting) {
		super(setting.getName());
		this.setting = setting;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		bindingAnimation.update(binding);
		String text = binding ? "..." : setting.getValue().toString().replace("_", " ");
		float textWidth = Fonts.REGULAR.getWidth(text, 6.5f);
		float finalWidth = animation.animate(textWidth + 4f, 200);
		Render2D.drawFont(context.getMatrices(), Fonts.REGULAR.getFont(7.5f), I18n.translate(setting.getName()), x + 4f, y + 3f, Color.WHITE);
		Render2D.startScissor(context, x + width - finalWidth - 4f, y + 2f, finalWidth, height - 4f);
		Render2D.drawRoundedRect(context.getMatrices(), x + width - finalWidth - 4f, y + 2f, finalWidth, height - 4f, 1.5f, ColorUtils.getGlobalColor());
		Render2D.drawFont(context.getMatrices(), Fonts.REGULAR.getFont(6.5f), setting.getValue().toString().replace("_", " "), x + width - textWidth - 6f, y + 3f, new Color(255, 255, 255, (int) (255 * bindingAnimation.getReversedValue())));
		Render2D.drawFont(context.getMatrices(), Fonts.REGULAR.getFont(6.5f), "...", x + width - textWidth - 6f, y + 3f, new Color(255, 255, 255, (int) (255 * bindingAnimation.getValue())));
		Render2D.stopScissor(context);
	}

	@Override
	public void mouseClicked(double mouseX, double mouseY, int button) {
		String text = binding ? "..." : setting.getValue().toString().replace("_", " ");
		float textWidth = Fonts.REGULAR.getWidth(text, 6.5f);
		if (MathUtils.isHovered(x + width - 8f - textWidth, y + 2f, textWidth + 4f, height - 4f, (float) mouseX, (float) mouseY) && !binding && button == 0) {
			binding = true;
			return;
		}

		if (binding) {
			setting.setValue(new Bind(button, true));
			binding = false;
			return;
		}
	}

	@Override
	public void mouseReleased(double mouseX, double mouseY, int button) {

	}

	@Override
	public void keyPressed(int keyCode, int scanCode, int modifiers) {
		if (binding) {
			if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_DELETE) setting.setValue(new Bind(-1, false));
			else setting.setValue(new Bind(keyCode, false));
			binding = false;
		}
	}

	@Override
	public void keyReleased(int keyCode, int scanCode, int modifiers) {

	}

	@Override
	public void charTyped(char chr, int modifiers) {

	}//
}