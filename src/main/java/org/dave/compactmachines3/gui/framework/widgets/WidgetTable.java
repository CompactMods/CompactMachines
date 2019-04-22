package org.dave.compactmachines3.gui.framework.widgets;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;

import java.util.Map;

public class WidgetTable extends WidgetPanel {
    Table<Integer, Integer, Widget> table;
    int cellPadding = 0;

    public WidgetTable() {
        table = TreeBasedTable.create();
    }

    public WidgetTable setCellPadding(int cellPadding) {
        this.cellPadding = cellPadding;
        return this;
    }

    public void remove(int column, int row) {
        Widget cell = this.table.get(row, column);
        if(cell == null) {
            return;
        }

        this.remove(cell);
        this.table.remove(row, column);

        this.repositionCells();
    }

    public void add(int column, int row, Widget cell) {
        this.table.put(row, column, cell);
        this.add(cell);
        this.repositionCells();
    }

    public int getCellHeight(int column, int row) {
        if(!table.contains(row, column)) {
            return 0;
        }

        Widget widget = table.get(row, column);
        return widget.height;
    }

    public int getCellWidth(int column, int row) {
        if(!table.contains(row, column)) {
            return 0;
        }

        Widget widget = table.get(row, column);
        return widget.width;
    }

    public int getColumnWidth(int column) {
        int maxWidth = 0;
        for(Widget cell : table.columnMap().get(column).values()) {
            maxWidth = Math.max(maxWidth, cell.width);
        }
        return maxWidth;
    }

    public int getRowHeight(int row) {
        int maxHeight = 0;
        for(Widget cell : table.rowMap().get(row).values()) {
            maxHeight = Math.max(maxHeight, cell.height);
        }
        return maxHeight;
    }

    public int getColumnCount() {
        return table.columnMap().size();
    }

    public int getRowCount() {
        return table.rowMap().size();
    }

    private void repositionCells() {
        int xOffset = 0;
        for(Map.Entry<Integer, Map<Integer, Widget>> columnData : table.columnMap().entrySet()) {
            int column = columnData.getKey();
            int columnWidth = getColumnWidth(column);

            int yOffset = 0;
            for(Map.Entry<Integer, Widget> cellData : columnData.getValue().entrySet()) {
                int row = cellData.getKey();
                int rowHeight = getRowHeight(row);

                Widget cell = cellData.getValue();
                cell.setX(xOffset);
                cell.setY(yOffset);

                yOffset += rowHeight + cellPadding;
            }

            xOffset += columnWidth + cellPadding;
        }
    }
}
