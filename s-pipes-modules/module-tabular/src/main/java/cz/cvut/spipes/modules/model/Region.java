package cz.cvut.spipes.modules.model;

public class Region {
    private int firstRow;
    private int lastRow;
    private int firstColumn;
    private int lastColumn;
    public Region(int firstRow, int firstColumn, int lastRow, int lastColumn) {
        this.firstRow = firstRow;
        this.firstColumn = firstColumn;
        this.lastRow = lastRow;
        this.lastColumn = lastColumn;
    }

    public int getFirstRow() {
        return firstRow;
    }

    public void setFirstRow(int firstRow) {
        this.firstRow = firstRow;
    }

    public int getLastRow() {
        return lastRow;
    }

    public void setLastRow(int lastRow) {
        this.lastRow = lastRow;
    }

    public int getFirstColumn() {
        return firstColumn;
    }

    public void setFirstColumn(int firstColumn) {
        this.firstColumn = firstColumn;
    }

    public int getLastColumn() {
        return lastColumn;
    }

    public void setLastColumn(int lastColumn) {
        this.lastColumn = lastColumn;
    }
}
