package cz.cvut.spipes.modules.tabular;

import cz.cvut.spipes.exception.SPipesException;
import cz.cvut.spipes.modules.model.Column;
import cz.cvut.spipes.modules.model.Region;
import cz.cvut.spipes.modules.model.TableSchema;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.ICsvListReader;

import java.io.IOException;
import java.util.*;

public class CSVReader implements TabularReader {

    ICsvListReader listReader;
    private int numberOfRows = 0;

    private static final Logger LOG = LoggerFactory.getLogger(CSVReader.class);

    public CSVReader(ICsvListReader listReader) {
        this.listReader = listReader;
    }

    @Override
    public List<String> getHeader() throws IOException{
        try {
            return Arrays.asList((listReader.getHeader(true))); // skip the header (can't be used with CsvListReader);
        } catch (NullPointerException e) {
            return null;
        }
    }

    @Override
    public List<Statement> getRowStatements(List<String>header, List<Column>outputColumns, TableSchema tableSchema) throws IOException {
        List<Statement>statements = new ArrayList<>();
        List<String> row;
        int rowNumber = 0;
        //for each row
        while ((row = listReader.read()) != null) {
            rowNumber++;

            for (int i = 0; i < header.size(); i++) {
                // 4.6.8.1
                Column column = outputColumns.get(i);
                String cellValue = getValueFromRow(row, i, header.size(), rowNumber);
                if (cellValue != null) statements.add(createRowResource(cellValue, rowNumber, column,tableSchema));
                //TODO: URITemplate

                // 4.6.8.5 - else, if value is list and cellOrdering == true
                // 4.6.8.6 - else, if value is list
                // 4.6.8.7 - else, if cellValue is not null
            }
        }
        numberOfRows = rowNumber;
        listReader.close();
        return statements;
    }

    @Override
    public int getNumberOfRows(){
        return listReader.getRowNumber() - 1;
    }

    @Override
    public String getTableName() {
        return null;
    }

    @Override
    public int getTablesCount() {
        return 1;
    }

    @Override
    public List<Region> getMergedRegions(){
        List<Region> list = new ArrayList<>();
        return list;
    }

    private Statement createRowResource(String cellValue, int rowNumber, Column column, TableSchema tableSchema) {
        Resource rowResource = ResourceFactory.createResource(tableSchema.createAboutUrl(rowNumber));

        return ResourceFactory.createStatement(
                rowResource,
                ResourceFactory.createProperty(column.getPropertyUrl()),
                ResourceFactory.createPlainLiteral(cellValue));
    }

    private String getValueFromRow(List<String> row, int index, int expectedRowLength, int currentRecordNumber) {
        try {
            return row.get(index);
        } catch (IndexOutOfBoundsException e) {
            String recordDelimiter = "\n----------\n";
            StringBuilder record = new StringBuilder(recordDelimiter);
            for (int i = 0; i < row.size(); i++) {
                record
                        .append(i)
                        .append(":")
                        .append(row.get(i))
                        .append(recordDelimiter);
            }
            LOG.error("Reading input file failed when reading record #{} (may not reflect the line #).\n" +
                            " It was expected that the current record contains {} values" +
                            ", but {}. element was not retrieved before whole record was processed.\n" +
                            "The problematic record: {}",
                    currentRecordNumber,
                    expectedRowLength,
                    index+1,
                    record
            );
            throw new SPipesException("Reading input file failed.", e);
        }
    }

}
