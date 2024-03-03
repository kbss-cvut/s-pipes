package cz.cvut.spipes.modules.tabular;

import cz.cvut.spipes.constants.HTML;
import cz.cvut.spipes.modules.model.Column;
import cz.cvut.spipes.modules.model.Region;
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

    @Override
    public String getTableName() {
        Document doc = Jsoup.parseBodyFragment(new String(streamResource.getContent()));
        doc.outputSettings(new Document.OutputSettings().prettyPrint(false));
        Elements tables = doc.getElementsByTag(HTML.TABLE);
        return tables.get(0).attr("data-name");
    }

    @Override
    public int getTablesCount() {
        Document doc = Jsoup.parseBodyFragment(new String(streamResource.getContent()));
        doc.outputSettings(new Document.OutputSettings().prettyPrint(false));
        Elements tables = doc.getElementsByTag(HTML.TABLE);
        return tables.size();
    }

    @Override
    public List<Region> getMergedRegions(){
        List<Region> list = new ArrayList<>();
        Document doc = Jsoup.parseBodyFragment(new String(streamResource.getContent()));

        Elements rows = doc.getElementsByTag(HTML.TABLE_ROW_TAG);

        for (Element row : rows) {
            Elements cells = row.getElementsByTag(HTML.TABLE_HEADER_TAG);
            cells.addAll(row.getElementsByTag(HTML.TABLE_CELL_TAG));
            int rowNum = row.elementSiblingIndex();
            int colNum = 0;
            for(Element cell : cells) {
                int colspan = parseInt(cell.attr("colspan"), 1);
                int rowspan = parseInt(cell.attr("rowspan"), 1);
                if (colspan > 1 || rowspan > 1) {
                    list.add(new Region(
                            rowNum,
                            colNum,
                            rowNum+rowspan-1,
                            colNum+colspan-1)
                    );
                }
                colNum+=colspan;
            }
        }
        return list;
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
            if(cell.hasText())statements.add(createRowResource(cell.html(),rowNumber,outputColumns.get(colNumber),tableSchema));
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

    private Statement createRowResource(String cellValue, int rowNumber, Column column, TableSchema tableSchema) {
        Resource rowResource = ResourceFactory.createResource(tableSchema.createAboutUrl(rowNumber));

        return ResourceFactory.createStatement(
                rowResource,
                ResourceFactory.createProperty(column.getPropertyUrl()),
                ResourceFactory.createPlainLiteral(cellValue));
    }
}
