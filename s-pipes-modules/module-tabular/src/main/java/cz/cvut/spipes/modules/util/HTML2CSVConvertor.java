package cz.cvut.spipes.modules.util;

import cz.cvut.spipes.constants.HTML;
import cz.cvut.spipes.registry.StreamResource;
import cz.cvut.spipes.registry.StringStreamResource;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * This class can be used to read the HTML table from input and convert it to CSV file.
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
public class HTML2CSVConvertor {

    public StringStreamResource convertToCSV(StreamResource streamResource) {
        StringBuilder csvStringBuilder = new StringBuilder();

        Document doc = Jsoup.parseBodyFragment(new String(streamResource.getContent()));
        Elements rows = doc.getElementsByTag(HTML.TABLE_ROW_TAG);

        for (Element row : rows) {
            processTag(row, csvStringBuilder, HTML.TABLE_HEADER_TAG);
            processTag(row, csvStringBuilder, HTML.TABLE_CELL_TAG);
            csvStringBuilder.append("\n");
        }

        return new StringStreamResource(
                streamResource.getUri(),
                csvStringBuilder.toString().getBytes(),
                "text/csv"
        );
    }

    private void processTag(Element row, StringBuilder sb, String tag) {
        Elements cells = row.getElementsByTag(tag);
        for (Element cell : cells) {
            if (cell != cells.get(0)) sb.append(",");
            sb.append("\"").append(StringEscapeUtils.escapeJava(cell.text())).append("\"");
        }
    }
}
