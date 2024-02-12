package cz.cvut.spipes.modules.tabular;

import cz.cvut.spipes.constants.HTML;
import cz.cvut.spipes.modules.model.Column;
import cz.cvut.spipes.modules.model.TableSchema;
import cz.cvut.spipes.registry.StreamResource;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class HtmlReader implements TabularReader{

    private final StreamResource streamResource;
    private final List<Pair<Integer, Integer> > cellColSpan = new ArrayList<>();
    public HtmlReader(StreamResource streamResource) {
        this.streamResource = streamResource;
    }

    @Override
    public List<String> getHeader() {
        List<String> header;

        Document doc = Jsoup.parseBodyFragment(new String(streamResource.getContent()));
        doc.outputSettings(new Document.OutputSettings().prettyPrint(false));
        Elements rows = doc.getElementsByTag(HTML.TABLE_ROW_TAG);
        Element headerRow = rows.first();

        header = processHeaderRow(headerRow, HTML.TABLE_HEADER_TAG);

        return header;
    }

    @Override
    public List<Statement> getRowStatements(List<String> header, List<Column> outputColumns, TableSchema tableSchema) {
        List<Statement>statements = new ArrayList<>();

        Document doc = Jsoup.parseBodyFragment(new String(streamResource.getContent()));
        doc.outputSettings(new Document.OutputSettings().prettyPrint(false));
        Elements rows = doc.getElementsByTag(HTML.TABLE_ROW_TAG);

        for (int i = 1; i < rows.size(); i++) {
            Element row = rows.get(i);
            statements.addAll(getRowStatementsFromRow(row, i, HTML.TABLE_CELL_TAG,outputColumns,tableSchema));
        }

        return statements;
    }

    @Override
    public int getNumberOfRows() {
        Document doc = Jsoup.parseBodyFragment(new String(streamResource.getContent()));
        doc.outputSettings(new Document.OutputSettings().prettyPrint(false));
        Elements rows = doc.getElementsByTag(HTML.TABLE_ROW_TAG);
        return rows.size() - 1;
    }

    private List<Statement> getRowStatementsFromRow(Element row,int rowNumber, String tag, List<Column> outputColumns, TableSchema tableSchema) {
        List<Statement>statements = new ArrayList<>();
        Elements cells = row.getElementsByTag(tag);
        if(cells.isEmpty()){
            throw new RuntimeException();
        }
        if(cellColSpan.isEmpty()) {
            for (int i = 0; i < cells.size(); i++) cellColSpan.add(new Pair<>(0, 1));
        }
        int colNumber = 0;
        for (Element cell : cells) {
            if(cellColSpan.get(colNumber).getLeft() != 0){
                cellColSpan.set(colNumber,new Pair<>(
                        cellColSpan.get(colNumber).getLeft()-1,
                        cellColSpan.get(colNumber).getRight()));
                colNumber += cellColSpan.get(colNumber).getRight();
            }
            statements.add(createRowResource(cell.html(),rowNumber,outputColumns.get(colNumber),tableSchema));
            int colspan = parseInt(cell.attr("colspan"), 1);
            int rowspan = parseInt(cell.attr("rowspan"), 1);
            cellColSpan.set(colNumber, new Pair<>(rowspan-1,colspan));
            colNumber += colspan;
        }
        // Process merged cells in the end of the row
        while(colNumber < cellColSpan.size()) {
            if (cellColSpan.get(colNumber).getLeft() != 0) {
                cellColSpan.set(colNumber, new Pair<>(
                        cellColSpan.get(colNumber).getLeft() - 1,
                        cellColSpan.get(colNumber).getRight()));
                colNumber += cellColSpan.get(colNumber).getRight();
            }
        }
        return statements;
    }

    private List<String> processHeaderRow(Element row, String tag) {
        List<String>cellValues = new ArrayList<>();
        Elements cells = row.getElementsByTag(tag);
        if(cells.isEmpty())return cellValues;
        if(cellColSpan.isEmpty()) {
            for (int i = 0; i < cells.size(); i++) cellColSpan.add(new Pair<>(0, 1));
        }
        int curColIndex = 0;
        for (Element cell : cells) {
            if(cellColSpan.get(curColIndex).getLeft() != 0){
                cellColSpan.set(curColIndex,new Pair<>(
                        cellColSpan.get(curColIndex).getLeft()-1,
                        cellColSpan.get(curColIndex).getRight()));
                curColIndex += cellColSpan.get(curColIndex).getRight();
            }
            cellValues.add(cell.html());
            int colspan = parseInt(cell.attr("colspan"), 1);
            int rowspan = parseInt(cell.attr("rowspan"), 1);
            cellColSpan.set(curColIndex, new Pair<>(rowspan-1,colspan));
            curColIndex += colspan;
        }
        // Process merged cells in the end of the row
        while(curColIndex < cellColSpan.size()) {
            if (cellColSpan.get(curColIndex).getLeft() != 0) {
                cellColSpan.set(curColIndex, new Pair<>(
                        cellColSpan.get(curColIndex).getLeft() - 1,
                        cellColSpan.get(curColIndex).getRight()));
                curColIndex += cellColSpan.get(curColIndex).getRight();
            }
        }
        return cellValues;
    }

    int parseInt(String s,int defaultValue){
        int res = 0;
        try {
            res = Integer.parseInt(s);
        } catch (java.lang.NumberFormatException e){
            res = defaultValue;
        }
        return res;
    }

    private Statement createRowResource(String cellValue, int rowNumber, Column column, TableSchema tableSchema) {
        Resource rowResource = ResourceFactory.createResource(tableSchema.createAboutUrl(rowNumber));

        return ResourceFactory.createStatement(
                rowResource,
                ResourceFactory.createProperty(column.getPropertyUrl()),
                ResourceFactory.createPlainLiteral(cellValue));
    }
}
