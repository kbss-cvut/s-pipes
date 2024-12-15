package cz.cvut.spipes.modules.util;

import cz.cvut.spipes.modules.ResourceFormat;
import cz.cvut.spipes.modules.model.Region;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CSVFileReaderAdapter implements FileReaderAdapter {
    private ICsvListReader listReader;
    private CsvPreference csvPreference;

    public CSVFileReaderAdapter(CsvPreference csvPreference) {
        this.csvPreference = csvPreference;
    }

    @Override
    public void initialise(InputStream inputStream, ResourceFormat sourceResourceFormat, int tableIndex) throws IOException {
        listReader = new CsvListReader(new InputStreamReader(inputStream), csvPreference);
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
    public List<Region> getMergedRegions() {
         return new ArrayList<>();
    }

    @Override
    public String getLabel(){
        return null;
    }
}
