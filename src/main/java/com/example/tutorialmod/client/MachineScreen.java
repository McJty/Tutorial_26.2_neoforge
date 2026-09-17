package com.example.tutorialmod.client;

import com.example.tutorialmod.TutorialMod;
import com.example.tutorialmod.menu.MachineMenu;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public final class MachineScreen extends AbstractContainerScreen<MachineMenu> {
    private static final Identifier BACKGROUND =
            Identifier.fromNamespaceAndPath(TutorialMod.MOD_ID, "textures/gui/machine.png");
    private static final int ENERGY_COLOR = 0xffd9ad32;
    private static final int TEXT_COLOR = 0xff404040;

    public MachineScreen(MachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, leftPos, topPos,
                0, 0, imageWidth, imageHeight, 256, 256);
        int width = menu.getCapacity() == 0 ? 0 : 80 * menu.getEnergy() / menu.getCapacity();
        graphics.fill(leftPos + 80, topPos + 35, leftPos + 80 + width, topPos + 47, ENERGY_COLOR);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        graphics.text(font, Component.translatable(
                menu.isGenerator() ? "gui.tutorialmod.fuel" : "gui.tutorialmod.food"),
                leftPos + 36, topPos + 23, TEXT_COLOR, false);
        graphics.text(font, Component.translatable("gui.tutorialmod.energy",
                menu.getEnergy(), menu.getCapacity()),
                leftPos + 78, topPos + 52, TEXT_COLOR, false);
    }
}
