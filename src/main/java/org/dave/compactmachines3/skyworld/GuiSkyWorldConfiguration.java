package org.dave.compactmachines3.skyworld;

import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.GuiScrollingList;
import net.minecraftforge.fml.client.config.GuiCheckBox;
import org.dave.compactmachines3.schema.Schema;
import org.dave.compactmachines3.schema.SchemaRegistry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiSkyWorldConfiguration extends GuiScreen {
    private final GuiCreateWorld parent;

    private SkyWorldConfiguration config;

    private GuiCheckBox lockedButton;
    private GuiCheckBox givePsdButton;
    private GuiButton closeButton;
    private SchemaScrollingList guiSchemaList;

    private GuiCheckBox smallButton;
    private GuiCheckBox mediumButton;
    private GuiCheckBox largeButton;

    public GuiSkyWorldConfiguration(GuiCreateWorld parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        super.initGui();

        // Restore previously selected values when reopening this gui
        if(parent.chunkProviderSettingsJson != null && !parent.chunkProviderSettingsJson.trim().isEmpty()) {
            config = new SkyWorldConfiguration(parent.chunkProviderSettingsJson);
        } else {
            config = new SkyWorldConfiguration();
        }

        int yOffset = 8;

        lockedButton = new GuiCheckBox(0, 8, yOffset, I18n.format("gui.compactmachines3.compactsky.configuration.startLocked"), config.startLocked);
        this.buttonList.add(lockedButton);
        yOffset += 14;

        givePsdButton = new GuiCheckBox(1, 8, yOffset, I18n.format("gui.compactmachines3.compactsky.configuration.givePSD"), config.givePSD);
        this.buttonList.add(givePsdButton);
        yOffset += 14;

        smallButton = new GuiCheckBox(2, 8, yOffset, I18n.format("gui.compactmachines3.compactsky.configuration.small"), config.size == EnumSkyWorldSize.SMALL);
        this.buttonList.add(smallButton);

        mediumButton = new GuiCheckBox(3, 100, yOffset, I18n.format("gui.compactmachines3.compactsky.configuration.medium"), config.size == EnumSkyWorldSize.MEDIUM);
        this.buttonList.add(mediumButton);

        largeButton = new GuiCheckBox(4, 192, yOffset, I18n.format("gui.compactmachines3.compactsky.configuration.large"), config.size == EnumSkyWorldSize.LARGE);
        this.buttonList.add(largeButton);

        yOffset += 14;

        int listHeight = this.height - 52 - yOffset;
        guiSchemaList = new SchemaScrollingList(this, 8, yOffset, 200, listHeight, 20);

        closeButton = new GuiButton(100, this.width / 2 - 75, this.height-28, 150, 20, I18n.format("gui.done"));
        this.buttonList.add(closeButton);

    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        if(button.id == 0) {
            this.config.startLocked = !this.config.startLocked;
        }

        if(button.id == 1) {
            this.config.givePSD = !this.config.givePSD;
        }

        if(button.id == 2) {
            this.config.size = EnumSkyWorldSize.SMALL;
            mediumButton.setIsChecked(false);
            largeButton.setIsChecked(false);
        }

        if(button.id == 3) {
            this.config.size = EnumSkyWorldSize.MEDIUM;
            smallButton.setIsChecked(false);
            largeButton.setIsChecked(false);
        }

        if(button.id == 4) {
            this.config.size = EnumSkyWorldSize.LARGE;
            smallButton.setIsChecked(false);
            mediumButton.setIsChecked(false);
        }

        parent.chunkProviderSettingsJson = this.config.getAsJsonString();

        if(button.id == 100) {
            this.mc.displayGuiScreen(parent);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);

        guiSchemaList.drawScreen(mouseX, mouseY, partialTicks);

        Schema selectedSchema = guiSchemaList.getSelected();
        if(selectedSchema != null) {
            drawSchemaInfo(selectedSchema, mouseX, mouseY, partialTicks);
        }
    }

    private void drawSchemaInfo(Schema selectedSchema, int mouseX, int mouseY, float partialTicks) {
        String sizeLabel = I18n.format("gui.compactmachines3.compactsky.configuration.label.MachineSize");
        int dim = selectedSchema.getSize().getDimension()-1;

        int yOffset = guiSchemaList.getY();
        String sizeString = String.format("%s: %s (%dx%dx%d)", sizeLabel, selectedSchema.getSize().getName(), dim, dim, dim);
        fontRenderer.drawString(sizeString, 230, yOffset, 0xFFFFFFFF, false);
        yOffset += 10;

        String descriptionLabel = I18n.format("gui.compactmachines3.compactsky.configuration.label.Description");
        String descriptionString = selectedSchema.getDescription().length() == 0 ? I18n.format("gui.compactmachines3.compactsky.configuration.warning.PleaseSpecifyADescription"): selectedSchema.getDescription();
        String descriptionStringFormatted = String.format("%s:\n%s", descriptionLabel, descriptionString);
        fontRenderer.drawSplitString(descriptionStringFormatted, 230, yOffset, this.width-240, 0xFFFFFFFF);
    }

    private static class SchemaScrollingList extends GuiScrollingList {
        int selected = -1;
        GuiSkyWorldConfiguration parent;
        List<Schema> schemaList;

        public SchemaScrollingList(GuiSkyWorldConfiguration parent, int x, int y, int listWidth, int listHeight, int entryHeight) {
            super(parent.mc, listWidth, listHeight, y, y + listHeight, x, entryHeight, parent.width, parent.height);
            this.parent = parent;
            this.schemaList = new ArrayList<>(SchemaRegistry.instance.getSchemas());
            // TODO: Sort by schema name

            if(parent.config.schema != null) {
                this.selected = this.schemaList.indexOf(parent.config.schema);
            }
        }

        public int getY() {
            return this.top;
        }

        public Schema getSelected() {
            return this.schemaList.get(selected);
        }

        @Override
        protected int getSize() {
            return this.schemaList.size();
        }

        @Override
        protected void elementClicked(int index, boolean doubleClick) {
            selected = index;

            parent.config.schema = this.schemaList.get(selected);
            parent.parent.chunkProviderSettingsJson = parent.config.getAsJsonString();
        }

        @Override
        protected boolean isSelected(int index) {
            return index == selected;
        }

        @Override
        protected void drawBackground() {

        }

        @Override
        protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
            FontRenderer font = this.parent.mc.fontRenderer;

            font.drawString(this.schemaList.get(slotIdx).getName(), this.left + 4, slotTop + 4, 0xFFFFFF);
        }
    }
}
