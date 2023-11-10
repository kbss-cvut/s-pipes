package cz.cvut.spipes.modules.tabular;

import org.supercsv.io.ICsvListReader;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class CSVReader implements TabularReader {

    ICsvListReader listReader;

    public CSVReader(ICsvListReader listReader) {
        this.listReader = listReader;
    }

    @Override
    public List<String> getHeader() throws IOException {
        return Arrays.asList((listReader.getHeader(true))); // skip the header (can't be used with CsvListReader);
    }
}
