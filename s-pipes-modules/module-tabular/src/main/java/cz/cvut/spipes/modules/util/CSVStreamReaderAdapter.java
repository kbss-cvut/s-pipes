package cz.cvut.spipes.modules.util;

import cz.cvut.spipes.InvalidQuotingTokenizer;
import cz.cvut.spipes.modules.ResourceFormat;
import cz.cvut.spipes.modules.model.Region;
import cz.cvut.spipes.registry.StreamResource;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.StringReader;

public class CSVStreamReaderAdapter implements StreamReaderAdapter {
    private ICsvListReader listReader;
    private CsvPreference csvPreference;
    String [] header = null;
    String [] firstRow = null;
    boolean acceptInvalidQuoting;
    Charset inputCharset;
    StreamResource sourceResource;

    public CSVStreamReaderAdapter(CsvPreference csvPreference) {
        this.csvPreference = csvPreference;
    }

    @Override
    public void initialise(InputStream inputStream, ResourceFormat sourceResourceFormat, int tableIndex,
                           boolean acceptInvalidQuoting, Charset inputCharset, StreamResource sourceResource) throws IOException {
        //listReader = new CsvListReader(new InputStreamReader(inputStream), csvPreference);
        this.acceptInvalidQuoting = acceptInvalidQuoting;
        this.inputCharset = inputCharset;
        this.sourceResource = sourceResource;
        listReader = getCsvListReader(csvPreference);
    }

    @Override
    public String[] getHeader(Boolean skipHeader) throws IOException {
        header = listReader.getHeader(true);
        if (skipHeader) {
            firstRow = header;
        }
        return header;
    }

    @Override
    public boolean hasNextRow() throws IOException {
        return ((listReader.read() != null) || (firstRow != null));
    }

    @Override
    public List<String> getNextRow() throws IOException {
        if (firstRow != null) {
            List<String> row = Arrays.asList(firstRow);
            firstRow = null;
            return row;
        }
        return listReader.read();
    }

    @Override
    public List<Region> getMergedRegions() {
         return new ArrayList<>();
    }

    @Override
    public String getSheetLabel(){
        return null;
    }

    @Override
    public void close() throws IOException{
        listReader.close();
    }

    private ICsvListReader getCsvListReader(CsvPreference csvPreference) {
        if (acceptInvalidQuoting) {
            if (getQuote() == '\0') {
                return null;
            } else
                return new CsvListReader(new InvalidQuotingTokenizer(getReader(), csvPreference), csvPreference);
        }
        return new CsvListReader(getReader(), csvPreference);
    }

    private Reader getReader() {
        return new StringReader(new String(sourceResource.getContent(), inputCharset));
    }

    public char getQuote() {
        return csvPreference.getQuoteChar();
    }
}
