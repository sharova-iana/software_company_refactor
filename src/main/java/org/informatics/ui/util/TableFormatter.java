package org.informatics.ui.util;

import java.util.ArrayList;
import java.util.List;

public class TableFormatter {

    /**
     * Main Orchestrator: Renders a structured, dynamically padded
     * ASCII data grid box frame to the console stream.
     */
    public static void printTable(String[] headers, List<String[]> rows) {
        if (headers == null || rows == null) return;

        // Helper 1: Calculate the exact maximum widths needed for columns
        int[] columnWidths = calculateColumnWidths(headers, rows);

        // Helper 2: Generate the horizontal border separator line string
        String dividerLine = generateDividerLine(columnWidths);

        // Print header segment block frame
        System.out.println(dividerLine);
        printRow(headers, columnWidths);
        System.out.println(dividerLine);

        // Helper 3: Process and output the grid's rows
        printTableDataRows(rows, columnWidths, headers.length);

        // Close bottom boundary box frame line
        System.out.println(dividerLine);
    }

    /**
     * Helper 1: Analyzes rows and headers to compute maximum character capacities.
     */
    public static int[] calculateColumnWidths(String[] headers, List<String[]> rows) {
        int[] columnWidths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            columnWidths[i] = headers[i].length();
        }

        for (String[] row : rows) {
            for (int i = 0; i < headers.length; i++) {
                if (i < row.length && row[i] != null) {
                    columnWidths[i] = Math.max(columnWidths[i], row[i].length());
                }
            }
        }
        return columnWidths;
    }

    /**
     * Helper 2: Dynamically builds the cross-platform grid divider line layout string.
     */
    public static String generateDividerLine(int[] columnWidths) {
        StringBuilder dividerBuilder = new StringBuilder();
        for (int width : columnWidths) {
            dividerBuilder.append("+").append("-".repeat(width + 2));
        }
        dividerBuilder.append("+");
        return dividerBuilder.toString();
    }

    /**
     * Helper 3: Iterates through row sets or safely prints an explicit empty data state placeholder.
     */
    private static void printTableDataRows(List<String[]> rows, int[] columnWidths, int headerCount) {
        if (rows.isEmpty()) {
            // Create a clean row where every single column matches its header width
            String[] emptyRow = new String[headerCount];
            for (int i = 0; i < headerCount; i++) {
                emptyRow[i] = "-"; // Short primitive string ensures columnWidths stay exactly matching header lengths
            }
            printRow(emptyRow, columnWidths);
        } else {
            for (String[] row : rows) {
                printRow(row, columnWidths);
            }
        }
    }


    /**
     * Formats and outputs a single row line to the text console stream with padded gaps.
     */
    public static void printRow(String[] columns, int[] columnWidths) {
        StringBuilder rowBuilder = new StringBuilder();
        for (int i = 0; i < columnWidths.length; i++) {
            String cellValue = (i < columns.length && columns[i] != null) ? columns[i] : "";

            // Left-align text string and pad with spaces dynamically
            String formatPattern = "| %-" + columnWidths[i] + "s ";
            rowBuilder.append(String.format(formatPattern, cellValue));
        }
        rowBuilder.append("|");
        System.out.println(rowBuilder.toString());
    }
}
