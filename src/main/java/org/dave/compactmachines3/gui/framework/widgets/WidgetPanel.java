package org.dave.compactmachines3.gui.framework.widgets;

import com.google.common.collect.Sets;
import net.minecraft.client.gui.GuiScreen;
import org.dave.compactmachines3.gui.framework.event.*;
import org.dave.compactmachines3.utility.Logz;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class WidgetPanel extends Widget {
    List<Widget> children;
    List<Widget> previouslyHovered;

    public int paddingTop = 0;
    public int paddingBottom = 0;
    public int paddingLeft = 0;
    public int paddingRight = 0;

    public WidgetPanel() {
        this.children = new LinkedList<>();
        this.previouslyHovered = new ArrayList<>();

        // Pass mouse move events along to the children, shift positions accordingly
        // Also notify widgets when the mouse entered or exited their area
        this.addListener(MouseMoveEvent.class, (event, widget)-> {

            //Logz.info("[%s] Moved mouse to x=%d, y=%d", widget.id, innerX, innerY);
            for(Widget child : children) {
                MouseMoveEvent shifted = new MouseMoveEvent(event.x, event.y);
                child.fireEvent(shifted);

                if(!child.isPosInside(event.x, event.y)) {
                    if(previouslyHovered.contains(child)) {
                        child.fireEvent(new MouseExitEvent());
                        previouslyHovered.remove(child);
                    }
                    continue;
                }

                if(!previouslyHovered.contains(child)) {
                    child.fireEvent(new MouseEnterEvent());
                    previouslyHovered.add(child);
                }
            }

            return WidgetEventResult.CONTINUE_PROCESSING;
        });

        // Pass click events along to the children, shift click position accordingly
        this.addListener(MouseClickEvent.class, ((event, widget) -> {
            int innerX = event.x - widget.getActualX();
            int innerY = event.y - widget.getActualY();
            //Logz.info("%s: Click: screen@%d,%d, panel@%d,%d", this.toString(), event.x, event.y, innerX, innerY);

            for(Widget child : children) {
                if(!child.visible) {
                    continue;
                }

                //Logz.info("Checking child: %s @ %d,%d", child.toString(), innerX, innerY);
                if(!child.isPosInside(event.x, event.y)) {
                    continue;
                }

                //Logz.info("Passing along to child=%s with %d,%d", child.toString(), innerX, innerY);
                child.fireEvent(new MouseClickEvent(event.x, event.y, event.button));
            }

            return WidgetEventResult.CONTINUE_PROCESSING;
        }));

        Set<Class<? extends IEvent>> eventsToIgnore = Sets.newHashSet(
                MouseClickEvent.class, MouseMoveEvent.class, MouseEnterEvent.class, MouseExitEvent.class,
                ValueChangedEvent.class
        );

        // Forward all other events to all children directly
        this.addAnyListener(((event, widget) -> {
            if(eventsToIgnore.contains(event.getClass())) {
                return WidgetEventResult.CONTINUE_PROCESSING;
            }

            for(Widget child : children) {
                WidgetEventResult immediateResult = child.fireEvent(event);
                if(immediateResult == WidgetEventResult.HANDLED) {
                    return WidgetEventResult.HANDLED;
                }
            }

            return WidgetEventResult.CONTINUE_PROCESSING;
        }));

    }

    @Override
    public boolean focusable() {
        return false;
    }

    public void clear() {
        this.children.clear();
    }

    public void add(Widget widget) {
        children.add(widget);
        widget.setParent(this);
    }

    public void remove(Widget widget) {
        children.remove(widget);
    }

    public void setPadding(int padding) {
        this.paddingTop = padding;
        this.paddingBottom = padding;
        this.paddingLeft = padding;
        this.paddingRight = padding;
    }

    public void setPadding(int top, int bottom, int left, int right) {
        this.paddingTop = top;
        this.paddingBottom = bottom;
        this.paddingLeft = left;
        this.paddingRight = right;
    }

    public void setPaddingTop(int paddingTop) {
        this.paddingTop = paddingTop;
    }

    public void setPaddingBottom(int paddingBottom) {
        this.paddingBottom = paddingBottom;
    }

    public void setPaddingLeft(int paddingLeft) {
        this.paddingLeft = paddingLeft;
    }

    public void setPaddingRight(int paddingRight) {
        this.paddingRight = paddingRight;
    }

    public List<Widget> getHoveredWidgets() {
        List<Widget> result = new ArrayList<>();
        this.getHoveredWidgets(result);
        return result;
    }

    private void getHoveredWidgets(List<Widget> result) {
        for(Widget widget : previouslyHovered) {
            if(widget instanceof WidgetPanel) {
                ((WidgetPanel) widget).getHoveredWidgets(result);
            } else {
                result.add(widget);
            }
        }
    }

    public Widget getHoveredWidget(int mouseX, int mouseY) {
        for(Widget child : children) {
            if(!child.visible) {
                continue;
            }

            if(child.isPosInside(mouseX, mouseY)) {
                Widget maybeResult = null;
                if(child instanceof WidgetPanel) {
                    maybeResult = ((WidgetPanel) child).getHoveredWidget(mouseX, mouseY);
                }

                if(maybeResult != null && maybeResult.hasToolTip()) {
                    return maybeResult;
                }

                return child;
            }
        }

        return null;
    }

    @Override
    public void draw(GuiScreen screen) {
        for(Widget child : children) {
            if(!child.visible) {
                continue;
            }

            child.shiftAndDraw(screen);
        }
    }
}
