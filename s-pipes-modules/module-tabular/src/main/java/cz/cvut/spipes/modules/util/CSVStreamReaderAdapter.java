package cz.cvut.spipes.modules.util;

import cz.cvut.spipes.InvalidQuotingTokenizer;
import cz.cvut.spipes.modules.ResourceFormat;
import cz.cvut.spipes.modules.exception.MissingArgumentException;
import cz.cvut.spipes.modules.model.Region;
import cz.cvut.spipes.registry.StreamResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CSVStreamReaderAdapter implements StreamReaderAdapter {
    private ICsvListReader listReader;
    private CsvPreference csvPreference;
    String [] header = null;
    String [] firstRow = null;
    boolean acceptInvalidQuoting;
    Charset inputCharset;
    StreamResource sourceResource;
    private final static Logger log = LoggerFactory.getLogger(CSVStreamReaderAdapter.class);

    public CSVStreamReaderAdapter(char quoteCharacter, int delimiter, boolean acceptInvalidQuoting, Charset inputCharset) {
        this.csvPreference = new CsvPreference.Builder(quoteCharacter, delimiter, System.lineSeparator()).build();
        this.acceptInvalidQuoting = acceptInvalidQuoting;
        this.inputCharset = inputCharset;
    }

    @Override
    public void initialise(InputStream inputStream, ResourceFormat sourceResourceFormat, int tableIndex, StreamResource sourceResource) throws IOException {
        this.sourceResource = sourceResource;
        listReader = getCsvListReader(csvPreference);
        if (listReader == null) {
            throwMissingQuoteError();
        }
    }

    @Override
    public String[] getHeader(boolean skipHeader) throws IOException {
        header = listReader.getHeader(true);
        if (skipHeader) {
            firstRow = header;
        }
        return header;
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

    private void throwMissingQuoteError() throws MissingArgumentException {
        String message = "Quote character must be specified when using custom tokenizer.";
        log.error(message);
        throw new MissingArgumentException(message);
    }
}
