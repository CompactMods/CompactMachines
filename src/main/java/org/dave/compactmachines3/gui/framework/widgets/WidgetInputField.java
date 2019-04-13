package org.dave.compactmachines3.gui.framework.widgets;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.math.MathHelper;
import org.dave.compactmachines3.gui.framework.event.*;
import org.dave.compactmachines3.utility.Logz;


public class WidgetInputField extends WidgetWithValue<String> {
    public String text = "";
    public String placeholder = "";

    private int maxStringLength = 32;
    private int cursorCounter;

    /** The current character index that should be used as start of the rendered text. */
    private int lineScrollOffset;
    private int cursorPosition;
    /** other selection position, maybe the same as the cursor */
    private int selectionEnd;

    /** Called to check if the text is valid */
    private Predicate<String> validator = Predicates.<String>alwaysTrue();

    int placeholderColor = 0x33FFFFFF;
    int textColor = 0xFFFFFFFF;
    int disabledTextColor = 0xFF707070;
    int backgroundColor = 0xFF333333;
    int borderColor = 0xFF000000;

    int updateTicks = 0;

    //GuiTextField textField;

    public WidgetInputField(String fieldIdentifier) {
        //this.textField = new GuiTextField(0, Minecraft.getMinecraft().fontRenderer, 0, 0, 120, 20);

        //this.id = id;
        this.setId("InputField[" + fieldIdentifier + "]");

        this.addListener(MouseClickEvent.class, new IWidgetListener<MouseClickEvent>() {
            @Override
            public WidgetEventResult call(MouseClickEvent event, Widget widget) {
                Logz.info("Mouse clicked @ x=%d, y=%d", event.x, event.y);
                //textField.mouseClicked(event.x, event.y, event.button);
                return WidgetEventResult.CONTINUE_PROCESSING;
            }
        });

        this.addListener(UpdateScreenEvent.class, new IWidgetListener<UpdateScreenEvent>() {
            @Override
            public WidgetEventResult call(UpdateScreenEvent event, Widget widget) {
                ((WidgetInputField)widget).updateTicks++;
                return WidgetEventResult.CONTINUE_PROCESSING;
            }
        });

        this.addListener(KeyTypedEvent.class, new IWidgetListener<KeyTypedEvent>() {
            @Override
            public WidgetEventResult call(KeyTypedEvent event, Widget widget) {
                WidgetInputField self = (WidgetInputField) widget;
                if (!self.focused) {
                    return WidgetEventResult.CONTINUE_PROCESSING;
                }
                else if (GuiScreen.isKeyComboCtrlA(event.keyCode))
                {
                    self.setCursorPositionEnd();
                    self.setSelectionPos(0);
                    return WidgetEventResult.HANDLED;
                }
                else if (GuiScreen.isKeyComboCtrlC(event.keyCode))
                {
                    GuiScreen.setClipboardString(self.getSelectedText());
                    return WidgetEventResult.HANDLED;
                }
                else if (GuiScreen.isKeyComboCtrlV(event.keyCode))
                {
                    if (self.enabled)
                    {
                        self.writeText(GuiScreen.getClipboardString());
                    }

                    return WidgetEventResult.HANDLED;
                }
                else if (GuiScreen.isKeyComboCtrlX(event.keyCode))
                {
                    GuiScreen.setClipboardString(self.getSelectedText());

                    if (self.enabled)
                    {
                        self.writeText("");
                    }

                    return WidgetEventResult.HANDLED;
                }
                else
                {
                    switch (event.keyCode)
                    {
                        case 14:

                            if (GuiScreen.isCtrlKeyDown())
                            {
                                if (self.enabled)
                                {
                                    self.deleteWords(-1);
                                }
                            }
                            else if (self.enabled)
                            {
                                self.deleteFromCursor(-1);
                            }

                            return WidgetEventResult.HANDLED;
                        case 199:

                            if (GuiScreen.isShiftKeyDown())
                            {
                                self.setSelectionPos(0);
                            }
                            else
                            {
                                self.setCursorPositionZero();
                            }

                            return WidgetEventResult.HANDLED;
                        case 203:

                            if (GuiScreen.isShiftKeyDown())
                            {
                                if (GuiScreen.isCtrlKeyDown())
                                {
                                    self.setSelectionPos(self.getNthWordFromPos(-1, self.getSelectionEnd()));
                                }
                                else
                                {
                                    self.setSelectionPos(self.getSelectionEnd() - 1);
                                }
                            }
                            else if (GuiScreen.isCtrlKeyDown())
                            {
                                self.setCursorPosition(self.getNthWordFromCursor(-1));
                            }
                            else
                            {
                                self.moveCursorBy(-1);
                            }

                            return WidgetEventResult.HANDLED;
                        case 205:

                            if (GuiScreen.isShiftKeyDown())
                            {
                                if (GuiScreen.isCtrlKeyDown())
                                {
                                    self.setSelectionPos(self.getNthWordFromPos(1, self.getSelectionEnd()));
                                }
                                else
                                {
                                    self.setSelectionPos(self.getSelectionEnd() + 1);
                                }
                            }
                            else if (GuiScreen.isCtrlKeyDown())
                            {
                                self.setCursorPosition(self.getNthWordFromCursor(1));
                            }
                            else
                            {
                                self.moveCursorBy(1);
                            }

                            return WidgetEventResult.HANDLED;
                        case 207:

                            if (GuiScreen.isShiftKeyDown())
                            {
                                self.setSelectionPos(self.text.length());
                            }
                            else
                            {
                                self.setCursorPositionEnd();
                            }

                            return WidgetEventResult.HANDLED;
                        case 211:

                            if (GuiScreen.isCtrlKeyDown())
                            {
                                if (self.enabled)
                                {
                                    self.deleteWords(1);
                                }
                            }
                            else if (self.enabled)
                            {
                                self.deleteFromCursor(1);
                            }

                            return WidgetEventResult.HANDLED;
                        default:

                            if (ChatAllowedCharacters.isAllowedCharacter(event.typedChar))
                            {
                                if (self.enabled)
                                {
                                    self.writeText(Character.toString(event.typedChar));
                                }

                                return WidgetEventResult.HANDLED;
                            }
                            else
                            {
                                return WidgetEventResult.CONTINUE_PROCESSING;
                            }
                    }
                }
            }
        });

        //addListener(MouseClickEvent.class, (event, widget) -> textField.mouseClicked(event.x + widget.lastDrawX, event.y + widget.lastDrawY, event.button));
        //this.addListener(KeyTypedEvent.class, (((event, widget) -> textField.textboxKeyTyped(event.typedChar, event.keyCode))));
    }

    /**
     * Increments the cursor counter
     */
    public void updateCursorCounter()
    {
        ++this.cursorCounter;
    }

    /**
     * Sets the text of the textbox, and moves the cursor to the end.
     */
    public void setText(String textIn)
    {
        if (this.validator.apply(textIn))
        {
            String oldText = this.text;
            if (textIn.length() > this.maxStringLength)
            {
                this.text = textIn.substring(0, this.maxStringLength);
            }
            else
            {
                this.text = textIn;
            }

            this.setCursorPositionEnd();
            this.valueChanged(oldText, this.text);
        }
    }

    /**
     * Returns the contents of the textbox
     */
    public String getText()
    {
        return this.text;
    }

    /**
     * returns the text between the cursor and selectionEnd
     */
    public String getSelectedText()
    {
        int i = this.cursorPosition < this.selectionEnd ? this.cursorPosition : this.selectionEnd;
        int j = this.cursorPosition < this.selectionEnd ? this.selectionEnd : this.cursorPosition;
        return this.text.substring(i, j);
    }

    public void setValidator(Predicate<String> theValidator)
    {
        this.validator = theValidator;
    }

    /**
     * Adds the given text after the cursor, or replaces the currently selected text if there is a selection.
     */
    public void writeText(String textToWrite)
    {
        String s = "";
        String s1 = ChatAllowedCharacters.filterAllowedCharacters(textToWrite);
        int i = this.cursorPosition < this.selectionEnd ? this.cursorPosition : this.selectionEnd;
        int j = this.cursorPosition < this.selectionEnd ? this.selectionEnd : this.cursorPosition;
        int k = this.maxStringLength - this.text.length() - (i - j);

        if (!this.text.isEmpty())
        {
            s = s + this.text.substring(0, i);
        }

        int l;

        if (k < s1.length())
        {
            s = s + s1.substring(0, k);
            l = k;
        }
        else
        {
            s = s + s1;
            l = s1.length();
        }

        if (!this.text.isEmpty() && j < this.text.length())
        {
            s = s + this.text.substring(j);
        }

        if (this.validator.apply(s))
        {
            String oldText = this.text;
            this.text = s;
            this.moveCursorBy(i - this.selectionEnd + l);
            this.valueChanged(oldText, s);
        }
    }

    /**
     * Deletes the given number of words from the current cursor's position, unless there is currently a selection, in
     * which case the selection is deleted instead.
     */
    public void deleteWords(int num)
    {
        if (!this.text.isEmpty())
        {
            if (this.selectionEnd != this.cursorPosition)
            {
                this.writeText("");
            }
            else
            {
                this.deleteFromCursor(this.getNthWordFromCursor(num) - this.cursorPosition);
            }
        }
    }

    /**
     * Deletes the given number of characters from the current cursor's position, unless there is currently a selection,
     * in which case the selection is deleted instead.
     */
    public void deleteFromCursor(int num)
    {
        if (!this.text.isEmpty())
        {
            if (this.selectionEnd != this.cursorPosition)
            {
                this.writeText("");
            }
            else
            {
                boolean flag = num < 0;
                int i = flag ? this.cursorPosition + num : this.cursorPosition;
                int j = flag ? this.cursorPosition : this.cursorPosition + num;
                String s = "";

                if (i >= 0)
                {
                    s = this.text.substring(0, i);
                }

                if (j < this.text.length())
                {
                    s = s + this.text.substring(j);
                }

                if (this.validator.apply(s))
                {
                    this.text = s;

                    if (flag)
                    {
                        this.moveCursorBy(num);
                    }
                }
            }
        }
    }

    /**
     * Gets the starting index of the word at the specified number of words away from the cursor position.
     */
    public int getNthWordFromCursor(int numWords)
    {
        return this.getNthWordFromPos(numWords, this.getCursorPosition());
    }

    /**
     * Gets the starting index of the word at a distance of the specified number of words away from the given position.
     */
    public int getNthWordFromPos(int n, int pos)
    {
        return this.getNthWordFromPosWS(n, pos, true);
    }

    /**
     * Like getNthWordFromPos (which wraps this), but adds option for skipping consecutive spaces
     */
    public int getNthWordFromPosWS(int n, int pos, boolean skipWs)
    {
        int i = pos;
        boolean flag = n < 0;
        int j = Math.abs(n);

        for (int k = 0; k < j; ++k)
        {
            if (!flag)
            {
                int l = this.text.length();
                i = this.text.indexOf(32, i);

                if (i == -1)
                {
                    i = l;
                }
                else
                {
                    while (skipWs && i < l && this.text.charAt(i) == ' ')
                    {
                        ++i;
                    }
                }
            }
            else
            {
                while (skipWs && i > 0 && this.text.charAt(i - 1) == ' ')
                {
                    --i;
                }

                while (i > 0 && this.text.charAt(i - 1) != ' ')
                {
                    --i;
                }
            }
        }

        return i;
    }

    /**
     * Moves the text cursor by a specified number of characters and clears the selection
     */
    public void moveCursorBy(int num)
    {
        this.setCursorPosition(this.selectionEnd + num);
    }

    /**
     * Sets the current position of the cursor.
     */
    public void setCursorPosition(int pos)
    {
        this.cursorPosition = pos;
        int i = this.text.length();
        this.cursorPosition = MathHelper.clamp(this.cursorPosition, 0, i);
        this.setSelectionPos(this.cursorPosition);
    }

    /**
     * Moves the cursor to the very start of this text box.
     */
    public void setCursorPositionZero()
    {
        this.setCursorPosition(0);
    }

    /**
     * Moves the cursor to the very end of this text box.
     */
    public void setCursorPositionEnd()
    {
        this.setCursorPosition(this.text.length());
    }

    /**
     * Sets the maximum length for the text in this text box. If the current text is longer than this length, the
     * current text will be trimmed.
     */
    public void setMaxStringLength(int length)
    {
        this.maxStringLength = length;

        if (this.text.length() > length)
        {
            this.text = this.text.substring(0, length);
        }
    }

    /**
     * returns the maximum number of character that can be contained in this textbox
     */
    public int getMaxStringLength()
    {
        return this.maxStringLength;
    }

    /**
     * returns the current position of the cursor
     */
    public int getCursorPosition()
    {
        return this.cursorPosition;
    }

    /**
     * Sets the color to use when drawing this text box's text. A different color is used if this text box is disabledHillProperties.
     */
    public void setTextColor(int color)
    {
        this.textColor = color;
    }

    /**
     * Sets the color to use for text in this text box when this text box is disabledHillProperties.
     */
    public void setDisabledTextColour(int color)
    {
        this.disabledTextColor = color;
    }

    /**
     * the side of the selection that is not the cursor, may be the same as the cursor
     */
    public int getSelectionEnd()
    {
        return this.selectionEnd;
    }

    /**
     * returns the width of the textbox
     */
    public int getWidth()
    {
        return this.width;
    }

    /**
     * Sets the position of the selection anchor (the selection anchor and the cursor position mark the edges of the
     * selection). If the anchor is set beyond the bounds of the current text, it will be put back inside.
     */
    public void setSelectionPos(int position)
    {
        int i = this.text.length();

        if (position > i)
        {
            position = i;
        }

        if (position < 0)
        {
            position = 0;
        }

        this.selectionEnd = position;

        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        if (fr != null)
        {
            if (this.lineScrollOffset > i)
            {
                this.lineScrollOffset = i;
            }

            int j = this.getWidth();
            String s = fr.trimStringToWidth(this.text.substring(this.lineScrollOffset), j);
            int k = s.length() + this.lineScrollOffset;

            if (position == this.lineScrollOffset)
            {
                this.lineScrollOffset -= fr.trimStringToWidth(this.text, j, true).length();
            }

            if (position > k)
            {
                this.lineScrollOffset += position - k;
            }
            else if (position <= this.lineScrollOffset)
            {
                this.lineScrollOffset -= this.lineScrollOffset - position;
            }

            this.lineScrollOffset = MathHelper.clamp(this.lineScrollOffset, 0, i);
        }
    }

    @Override
    public void draw(GuiScreen screen) {
        super.draw(screen);

        int x = 0;
        int y = 0;
        int width = this.width;
        int height = this.height;

        //Logz.info("Drawing input field @ %d,%d [width=%d, height=%d]", x, y, width, height);

        Gui.drawRect(x, y, x+width, y+height, borderColor);
        Gui.drawRect(x+1, y+1, x+width-1, y+height-1, backgroundColor);


        if(!focused && placeholder.length() > 0 && text.isEmpty()) {
            GlStateManager.enableBlend();
            screen.mc.fontRenderer.drawSplitString(placeholder, x + 3, y + 6, width - 2, placeholderColor);
            return;
        }

        int i = this.enabled ? this.textColor : this.disabledTextColor;
        int j = this.cursorPosition - this.lineScrollOffset;
        int k = this.selectionEnd - this.lineScrollOffset;
        String s = screen.mc.fontRenderer.trimStringToWidth(this.text.substring(this.lineScrollOffset), this.getWidth());
        boolean flag = j >= 0 && j <= s.length();
        boolean flag1 = this.focused && this.cursorCounter / 6 % 2 == 0 && flag;
        int l = x + 4;
        int i1 = y + (height - 8) / 2;
        int j1 = l;

        if (k > s.length())
        {
            k = s.length();
        }

        if (!s.isEmpty())
        {
            String s1 = flag ? s.substring(0, j) : s;
            j1 = screen.mc.fontRenderer.drawStringWithShadow(s1, (float)l, (float)i1, i);
        }

        boolean flag2 = this.cursorPosition < this.text.length() || this.text.length() >= this.getMaxStringLength();
        int k1 = j1;

        if (!flag)
        {
            k1 = j > 0 ? l + this.width : l;
        }
        else if (flag2)
        {
            k1 = j1 - 1;
            --j1;
        }

        if (!s.isEmpty() && flag && j < s.length())
        {
            j1 = screen.mc.fontRenderer.drawStringWithShadow(s.substring(j), (float)j1, (float)i1, i);
        }

        if (flag1)
        {
            if (flag2)
            {
                Gui.drawRect(k1, i1 - 1, k1 + 1, i1 + 1 + screen.mc.fontRenderer.FONT_HEIGHT, -3092272);
            }
            else
            {
                screen.mc.fontRenderer.drawStringWithShadow("_", (float)k1, (float)i1, i);
            }
        }

        if (k != j)
        {
            int l1 = l + screen.mc.fontRenderer.getStringWidth(s.substring(0, k));
            this.drawSelectionBox(k1, i1 - 1, l1 - 1, i1 + 1 + screen.mc.fontRenderer.FONT_HEIGHT);
        }

        /*
        String s = screen.mc.fontRenderer.trimStringToWidth(this.text.substring(this.lineScrollOffset), this.getWidth());

        if(!focused && placeholder.length() > 0 && text.isEmpty()) {
            GlStateManager.enableBlend();
            screen.mc.fontRenderer.drawSplitString(placeholder, x + 3, y + 6, width - 2, placeholderColor);
        }

        String text = this.text;
        if (this.updateTicks / 6 % 2 == 0) {
            text = text + (this.focused ? "_" : "");
        } else {
            text = text + TextFormatting.GRAY + (this.focused ? "_" : "");
        }

        screen.mc.fontRenderer.drawSplitString(text, x + 3, y + 6, width - 2, textColor);

        */
        /*textField.width = width;
        textField.height = height;
        textField.x = x;
        textField.y = y;

        textField.drawTextBox();
        */
    }

    /**
     * Draws the blue selection box.
     */
    private void drawSelectionBox(int startX, int startY, int endX, int endY)
    {
        if (startX < endX)
        {
            int i = startX;
            startX = endX;
            endX = i;
        }

        if (startY < endY)
        {
            int j = startY;
            startY = endY;
            endY = j;
        }

        if (endX > this.x + this.width)
        {
            endX = this.x + this.width;
        }

        if (startX > this.x + this.width)
        {
            startX = this.x + this.width;
        }

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.color(0.0F, 0.0F, 255.0F, 255.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.enableColorLogic();
        GlStateManager.colorLogicOp(GlStateManager.LogicOp.OR_REVERSE);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos((double)startX, (double)endY, 0.0D).endVertex();
        bufferbuilder.pos((double)endX, (double)endY, 0.0D).endVertex();
        bufferbuilder.pos((double)endX, (double)startY, 0.0D).endVertex();
        bufferbuilder.pos((double)startX, (double)startY, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.disableColorLogic();
        GlStateManager.enableTexture2D();
    }
}
