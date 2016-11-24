package com.graphhopper.jsprit.core.reporting;

import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.Alignment;
import com.graphhopper.jsprit.core.reporting.DynamicTableDefinition.ColumnDefinition;


/**
 * @author balage
 * @param <C>
 */
public class ConfigurableTablePrinter<C extends PrinterContext> {

    public class TableRow {
        private String row[] = new String[tableDef.size()];
        private int lastIndex = 0;

        public TableRow() {
            super();
            Arrays.fill(row, "");
        }

        public TableRow set(int index, Object data) {
            if (index < 0 || index >= row.length) {
                throw new IndexOutOfBoundsException("Invalid index: " + index);
            }
            if (data != null) {
                if (!tableDef.getColumns().get(index).getType().accepts(data)) {
                    throw new ClassCastException("Cannot assign " + data.getClass().getSimpleName()
                                    + " to " + tableDef.getColumns().get(index).getType().getClass()
                                    .getSimpleName()
                                    + "( index: " + index + ")");
                }
            }
            String val = tableDef.getColumns().get(index).getType().convert(data);

            row[index] = val;
            return this;
        }

        public TableRow add(Object data) {
            return set(lastIndex++, data);
        }

        public TableRow add(int data) {
            return add(new Integer(data));
        }

        public TableRow add(long data) {
            return add(new Long(data));
        }

        public TableRow add(double data) {
            return add(new Double(data));
        }

        public TableRow add(boolean data) {
            return add(Boolean.valueOf(data));
        }

        public String get(int index) {
            if (index < 0 || index >= row.length) {
                throw new IndexOutOfBoundsException("Invalid index: " + index);
            }
            return row[index];
        }
    }

    private class Separator extends TableRow {
    }

    private PrinterColumnList<C> columnList;
    private DynamicTableDefinition tableDef;

    List<TableRow> rows = new ArrayList<>();

    public ConfigurableTablePrinter(PrinterColumnList<C> columnList) {
        super();
        this.columnList = columnList;
        tableDef = columnList.getTableDefinition();
    }

    public void addRow(C context) {
        TableRow row = new TableRow();
        columnList.populateRow(row, context);
        rows.add(row);
    }

    public void addSeparator() {
        rows.add(new Separator());
    }


    private String repeat(char c, int w) {
        return CharBuffer.allocate(w).toString().replace('\0', c);
    }

    public String print() {
        StringBuilder sb = new StringBuilder();
        int[] colWidth = calculateWidthInfo();
        int totalWidth = colWidth.length * (tableDef.getPadding() * 2 + 1) + 1;
        for (int w : colWidth) {
            totalWidth += w;
        }

        char corner = tableDef.getCorner();
        char horizontal = tableDef.getHorizontal();
        char vertical = tableDef.getVertical();
        int padding = tableDef.getPadding();

        String paddingChars = repeat(' ', padding);

        StringBuilder sbSep = new StringBuilder();
        sbSep.append(corner);
        for (int w : colWidth) {
            sbSep.append(repeat(horizontal, w + 2 * padding)).append(corner);
        }
        sbSep.append("\n");
        String separatorLine = sbSep.toString();

        if (tableDef.getHeading() != null) {
            sb.append(corner).append(repeat(horizontal, totalWidth - 2)).append(corner)
            .append("\n");
            sb.append(vertical).append(paddingChars)
            .append(Alignment.LEFT.align(tableDef.getHeading(),
                            totalWidth - 2 * padding - 2))
            .append(paddingChars)
            .append(vertical)
            .append("\n");
        }

        sb.append(separatorLine);
        sb.append(vertical);
        for (int i = 0; i < tableDef.size(); i++) {
            ColumnDefinition cd = tableDef.getColumns().get(i);
            sb.append(paddingChars).append(Alignment.LEFT.align(cd.getTitle(), colWidth[i]))
            .append(paddingChars).append(vertical);
        }
        sb.append("\n");
        sb.append(separatorLine);

        for(TableRow row : rows) {
            if (row instanceof ConfigurableTablePrinter.Separator) {
                sb.append(separatorLine);
            } else {
                sb.append(vertical);
                for (int i = 0; i < tableDef.size(); i++) {
                    ColumnDefinition cd = tableDef.getColumns().get(i);
                    sb.append(paddingChars).append(cd.getAlignment().align(row.get(i), colWidth[i]))
                    .append(paddingChars).append(vertical);
                }
                sb.append("\n");
            }
        }
        sb.append(separatorLine);

        return sb.toString();
    }

    private int[] calculateWidthInfo() {
        int colWidth[] = new int[tableDef.size()];
        IntStream.range(0, tableDef.size()).forEach(i -> {
            int max = rows.stream()
                            .filter(r -> r instanceof ConfigurableTablePrinter.TableRow)
                            .map(r -> r.get(i))
                            .filter(d -> d != null)
                            .mapToInt(d -> d.length())
                            .max().orElse(0);
            ColumnDefinition colDef = tableDef.getColumns().get(i);
            colWidth[i] = Math.max(colDef.getTitle().length(),
                            Math.max(colDef.getMinWidth(), Math.min(colDef.getMaxWidth(), max)));
        });
        return colWidth;
    }


}
