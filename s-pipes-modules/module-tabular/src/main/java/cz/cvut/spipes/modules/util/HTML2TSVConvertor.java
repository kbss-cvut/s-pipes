package cz.cvut.spipes.modules.util;

import cz.cvut.spipes.constants.HTML;
import cz.cvut.spipes.modules.ResourceFormat;
import cz.cvut.spipes.modules.model.Region;
import cz.cvut.spipes.registry.StreamResource;
import cz.cvut.spipes.registry.StringStreamResource;
import org.apache.jena.atlas.lib.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Module for converting tabular data (e.g. CSV, TSV, table represented in HTML) to RDF
 * <p>
 * This class can be used to read the HTML table from input and convert it to TSV file without quoting
 * and tab characters ("\t") are replaced by two spaces.
 * The HTML table must contain at least these two tags ({@literal <}td/>, {@literal <}tr/>) to be processed correctly.
 * The recommended format is shown in the example below:
 * <table>
 *     <tr>
 *         <th>Column 1</th>
 *         <th>Column 2</th>
 *     </tr>
 *     <tr>
 *         <td>Value 1</td>
 *         <td>Value 2</td>
 *     </tr>
 * </table
 */
public class HTML2TSVConvertor implements TSVConvertor {

    private final List<Pair<Integer, Integer> > cellColSpan = new ArrayList<>();

    @Override
    public StringStreamResource convertToTSV(StreamResource streamResource) {
        StringBuilder tsvStringBuilder = new StringBuilder();

        Document doc = Jsoup.parseBodyFragment(new String(streamResource.getContent()));
        doc.outputSettings(new Document.OutputSettings().prettyPrint(false));
        Elements rows = doc.getElementsByTag(HTML.TABLE_ROW_TAG);

        for (Element row : rows) {
            processTag(row, tsvStringBuilder, HTML.TABLE_HEADER_TAG);
            processTag(row, tsvStringBuilder, HTML.TABLE_CELL_TAG);
            tsvStringBuilder.append("\n");
        }

        return new StringStreamResource(
                streamResource.getUri(),
                tsvStringBuilder.toString().getBytes(),
                ResourceFormat.TSV.toString()
        );
    }

    private void processTag(Element row, StringBuilder sb, String tag) {
        Elements cells = row.getElementsByTag(tag);
        if(cells.isEmpty())return;
        if(cellColSpan.isEmpty()) {
            for (int i = 0; i < cells.size(); i++) cellColSpan.add(new Pair<>(0, 1));
        }
        int curColIndex = 0;
        Boolean isFirst = true;
        for (Element cell : cells) {
            if(cellColSpan.get(curColIndex).getLeft() != 0){
                for(int i = 0; i < cellColSpan.get(curColIndex).getRight(); i++){
                    if(!isFirst)sb.append('\t');
                    isFirst = false;
                    sb.append("");
                }
                cellColSpan.set(curColIndex,new Pair<>(
                        cellColSpan.get(curColIndex).getLeft()-1,
                        cellColSpan.get(curColIndex).getRight()));
                curColIndex += cellColSpan.get(curColIndex).getRight();
            }
            if (!isFirst) sb.append('\t');
            isFirst = false;
            sb.append(cell.html().replace("\t","  "));
            int colspan = parseInt(cell.attr("colspan"), 1);
            if(colspan > 1)
                for(int k = 1;k < colspan;k++)sb.append("\t");
            int rowspan = parseInt(cell.attr("rowspan"), 1);
            cellColSpan.set(curColIndex, new Pair<>(rowspan-1,colspan));
            curColIndex += colspan;
        }
        // Process merged cells in the end of the row
        while(curColIndex < cellColSpan.size()) {
            if (cellColSpan.get(curColIndex).getLeft() != 0) {
                for (int i = 0; i < cellColSpan.get(curColIndex).getRight(); i++) {
                    sb.append("\t");
                }
                cellColSpan.set(curColIndex, new Pair<>(
                        cellColSpan.get(curColIndex).getLeft() - 1,
                        cellColSpan.get(curColIndex).getRight()));
                curColIndex += cellColSpan.get(curColIndex).getRight();
            }
        }
    }

    @Override
    public List<Region> getMergedRegions(StreamResource streamResource){
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

    @Override
    public int getNumberTables(StreamResource streamResource) {
        Document doc = Jsoup.parseBodyFragment(new String(streamResource.getContent()));
        doc.outputSettings(new Document.OutputSettings().prettyPrint(false));
        Elements tables = doc.getElementsByTag(HTML.TABLE);
        return tables.size();
    }

    @Override
    public String getTableName(StreamResource streamResource) {
        Document doc = Jsoup.parseBodyFragment(new String(streamResource.getContent()));
        doc.outputSettings(new Document.OutputSettings().prettyPrint(false));
        Elements tables = doc.getElementsByTag(HTML.TABLE);
        return tables.get(0).attr("data-name");
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
}
