package updater.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import updater.mappings.ScreenMapper;
import updater.mappings.Text;
import updater.mappings.UtilitiesClient;

public abstract class AddFromLinkScreen extends ScreenMapper implements IGui {

	private net.minecraft.text.Text message = null;

	private final ConfigScreen configScreen;
	private final TextFieldWidget textField;
	private final ButtonWidget buttonAdd;
	private final net.minecraft.text.Text mainText;
	private final String[] extraText;

	public AddFromLinkScreen(ConfigScreen configScreen, boolean clearTextBoxAfterSearching, net.minecraft.text.Text mainText, net.minecraft.text.Text buttonText, String... extraText) {
		super(Text.literal(""));
		this.configScreen = configScreen;
		this.mainText = mainText;
		this.extraText = extraText;

		textField = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 0, 0, 0, SQUARE_SIZE, Text.literal(""));
		buttonAdd = new ButtonWidget(0, 0, 0, SQUARE_SIZE, buttonText, button -> {
			final String text = textField.getText();
			if (!text.isEmpty()) {
				if (clearTextBoxAfterSearching) {
					textField.setText("");
				}
				onClickBeforeThread();
				button.active = false;
				textField.active = false;
				message = Text.translatable("gui.updater.please_wait");
				new Thread(() -> onClick(text)).start();
			}
		});
	}

	@Override
	protected void init() {
		super.init();

		final int yStart = getYOffset() - TEXT_PADDING - SQUARE_SIZE - TEXT_FIELD_PADDING / 2;
		IGui.setPositionAndWidth(textField, SQUARE_SIZE + TEXT_FIELD_PADDING / 2, yStart, width - SQUARE_SIZE * 5 - TEXT_FIELD_PADDING);
		IGui.setPositionAndWidth(buttonAdd, width - SQUARE_SIZE * 4, yStart, SQUARE_SIZE * 3);

		textField.setChangedListener(text -> setAddButtonActive());
		textField.setMaxLength(2048);
		setAddButtonActive();

		addDrawableChild(textField);
		addDrawableChild(buttonAdd);
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		try {
			renderBackground(matrices);
			InGameHud.drawCenteredTextWithShadow(matrices, textRenderer, mainText, width / 2, SQUARE_SIZE, ARGB_WHITE);
			if (message != null) {
				textRenderer.drawShadow(matrices, message, SQUARE_SIZE, getYOffset(), ARGB_WHITE);
			}
			for (int i = 0; i < extraText.length; i++) {
				textRenderer.drawShadow(matrices, extraText[i], SQUARE_SIZE, SQUARE_SIZE + (TEXT_HEIGHT + TEXT_PADDING) * (1 + i), ARGB_WHITE);
			}
			renderAdditional(matrices);
			super.render(matrices, mouseX, mouseY, delta);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void tick() {
		textField.tick();
	}

	@Override
	public void close() {
		super.close();
		if (client != null) {
			UtilitiesClient.setScreen(client, configScreen);
		}
	}

	protected abstract void onClick(String text);

	protected void onClickBeforeThread() {
	}

	protected void setMessage(net.minecraft.text.Text message) {
		setAddButtonActive();
		this.message = message;
	}

	protected void renderAdditional(MatrixStack matrices) {
	}

	protected int getYOffset() {
		return SQUARE_SIZE * 2 + (TEXT_HEIGHT + TEXT_PADDING) * (1 + extraText.length) + TEXT_PADDING + TEXT_FIELD_PADDING;
	}

	private void setAddButtonActive() {
		buttonAdd.active = !textField.getText().isEmpty();
		if (buttonAdd.active) {
			message = null;
		}
		textField.active = true;
	}
}
