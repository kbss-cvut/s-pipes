package cz.cvut.spipes.modules.util;

import cz.cvut.spipes.InvalidQuotingTokenizer;
import cz.cvut.spipes.constants.HTML;
import cz.cvut.spipes.modules.ResourceFormat;
import cz.cvut.spipes.modules.model.Region;
import cz.cvut.spipes.registry.StreamResource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HTMLFileReaderAdapter implements FileReaderAdapter {
    private ICsvListReader listReader;
    private CsvPreference csvPreference;
    private TSVConvertor tsvConvertor;
    private String label;
    private StreamResource sourceResource;
    private StreamResource originalSourceResource;
    private Charset inputCharset = Charset.defaultCharset();
    private boolean acceptInvalidQuoting = false;

    public HTMLFileReaderAdapter(CsvPreference csvPreference) {
        this.csvPreference = csvPreference;
    }

    @Override
    public void initialise(StreamResource sourceResource, ResourceFormat sourceResourceFormat, int tableIndex) throws IOException {
        this.sourceResource = sourceResource;
        tsvConvertor = new HTML2TSVConvertor(tableIndex);
        listReader = getCsvListReader(csvPreference);
        this.sourceResource = tsvConvertor.convertToTSV(sourceResource);
    }

    @Override
    public String[] getHeader() throws IOException {
        return listReader.getHeader(true);
    }

    @Override
    public boolean hasNext() throws IOException {
        return listReader.read() != null;
    }

    @Override
    public List<String> getNextRow() throws IOException {
        return listReader.read();
    }

    @Override
    public List<Region> getMergedRegions(StreamResource sourceResource) {
        List<Region> list = new ArrayList<>();
        Document doc = Jsoup.parseBodyFragment(new String(sourceResource.getContent()));

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
    public String getLabel(){
        return this.label;
    }

    private ICsvListReader getCsvListReader(CsvPreference csvPreference) {
        if (acceptInvalidQuoting) {
            if (csvPreference.getQuoteChar() == '\0') {
                return null;
            } else
                return new CsvListReader(new InvalidQuotingTokenizer(getReader(), csvPreference), csvPreference);
        }
        return new CsvListReader(getReader(), csvPreference);
    }

    private Reader getReader() {
        return new StringReader(new String(sourceResource.getContent(), inputCharset));
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
