package cz.cvut.spipes.modules.util;

import cz.cvut.spipes.constants.HTML;
import cz.cvut.spipes.registry.StreamResource;
import cz.cvut.spipes.registry.StringStreamResource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
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
public class HTML2TSVConvertor {

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
                "text/tsv"
        );
    }

    private void processTag(Element row, StringBuilder sb, String tag) {
        Elements cells = row.getElementsByTag(tag);
        for (Element cell : cells) {
                if (cell != cells.get(0)) sb.append('\t');
                sb.append(cell.html().replace("\t","  "));
        }
    }
}
