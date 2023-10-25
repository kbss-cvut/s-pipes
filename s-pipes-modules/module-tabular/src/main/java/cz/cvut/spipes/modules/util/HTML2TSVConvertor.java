package cz.cvut.spipes.modules.util;

import cz.cvut.spipes.constants.HTML;
import cz.cvut.spipes.modules.ResourceFormat;
import cz.cvut.spipes.modules.model.Region;
import cz.cvut.spipes.registry.StreamResource;
import cz.cvut.spipes.registry.StringStreamResource;
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
        for (Element cell : cells) {
                if (cell != cells.get(0)) sb.append('\t');
                sb.append(cell.html().replace("\t","  "));
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
            int colNum = 1;
//            System.err.println(rowNum);
            for(Element cell : cells) {
                int colspan = parseInt(cell.attr("colspan"), 1);
                int rowspan = parseInt(cell.attr("rowspan"), 1);
//                System.out.println("index: [" + rowNum + "," + colNum + "]");
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
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public String getTableName(StreamResource streamResource) {
        throw new UnsupportedOperationException("Not implemented yet.");
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
