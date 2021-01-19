package org.dave.compactmachines3.gui.framework.widgets;

import com.google.common.collect.Sets;
import net.minecraft.client.gui.GuiScreen;
import org.dave.compactmachines3.gui.framework.event.IEvent;
import org.dave.compactmachines3.gui.framework.event.MouseClickEvent;
import org.dave.compactmachines3.gui.framework.event.MouseEnterEvent;
import org.dave.compactmachines3.gui.framework.event.MouseExitEvent;
import org.dave.compactmachines3.gui.framework.event.MouseMoveEvent;
import org.dave.compactmachines3.gui.framework.event.ValueChangedEvent;
import org.dave.compactmachines3.gui.framework.event.WidgetEventResult;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class WidgetPanel extends Widget {
    List<Widget> children;
    List<Widget> previouslyHovered;

    public WidgetPanel() {
        this.children = new LinkedList<>();
        this.previouslyHovered = new ArrayList<>();

        // Pass mouse move events along to the children, shift positions accordingly
        // Also notify widgets when the mouse entered or exited their area
        this.addListener(MouseMoveEvent.class, (event, widget) -> {

            for (Widget child : children) {
                MouseMoveEvent shifted = new MouseMoveEvent(event.x, event.y);
                child.fireEvent(shifted);

                if (!child.isPosInside(event.x, event.y)) {
                    if (previouslyHovered.contains(child)) {
                        child.fireEvent(new MouseExitEvent());
                        previouslyHovered.remove(child);
                    }
                    continue;
                }

                if (!previouslyHovered.contains(child)) {
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

            for (Widget child : children) {
                if (!child.visible) {
                    continue;
                }

                if (!child.isPosInside(event.x, event.y)) {
                    continue;
                }

                if (child.fireEvent(new MouseClickEvent(event.x, event.y, event.button)) == WidgetEventResult.HANDLED) {
                    return WidgetEventResult.HANDLED;
                }
            }

            return WidgetEventResult.CONTINUE_PROCESSING;
        }));

        Set<Class<? extends IEvent>> eventsToIgnore = Sets.newHashSet(
                MouseClickEvent.class, MouseMoveEvent.class, MouseEnterEvent.class, MouseExitEvent.class,
                ValueChangedEvent.class
        );

        // Forward all other events to all children directly
        this.addAnyListener(((event, widget) -> {
            if (eventsToIgnore.contains(event.getClass())) {
                return WidgetEventResult.CONTINUE_PROCESSING;
            }

            for (Widget child : children) {
                WidgetEventResult immediateResult = child.fireEvent(event);
                if (immediateResult == WidgetEventResult.HANDLED) {
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

    public List<Widget> getHoveredWidgets() {
        List<Widget> result = new ArrayList<>();
        this.getHoveredWidgets(result);
        return result;
    }

    private void getHoveredWidgets(List<Widget> result) {
        for (Widget widget : previouslyHovered) {
            if (widget instanceof WidgetPanel) {
                ((WidgetPanel) widget).getHoveredWidgets(result);
            } else {
                result.add(widget);
            }
        }
    }

    public Widget getHoveredWidget(int mouseX, int mouseY) {
        for (Widget child : this.children) {
            if (!child.visible)
                continue;

            if (child.isPosInside(mouseX, mouseY)) {
                if (child instanceof WidgetPanel) {
                    Widget possible = ((WidgetPanel) child).getHoveredWidget(mouseX, mouseY);

                    if (possible != null && possible.hasTooltip()) {
                        return possible;
                    }
                } else if (child.hasTooltip()) {
                    return child;
                }
            }
        }

        return null;
    }

    @Override
    public void draw(GuiScreen screen) {
        for (Widget child : children) {
            if (!child.visible)
                continue;

            child.shiftAndDraw(screen);
        }
    }
}
