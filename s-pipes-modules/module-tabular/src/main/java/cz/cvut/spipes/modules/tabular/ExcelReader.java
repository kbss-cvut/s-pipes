package cz.cvut.spipes.modules.tabular;

import cz.cvut.spipes.modules.ResourceFormat;
import cz.cvut.spipes.modules.model.Column;
import cz.cvut.spipes.modules.model.TableSchema;
import cz.cvut.spipes.registry.StreamResource;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelReader implements TabularReader{
    private int sheetNumber;
    private ResourceFormat format;
    private StreamResource streamResource;

    public ExcelReader(int sheetNumber, ResourceFormat format, StreamResource streamResource) {
        this.sheetNumber = sheetNumber;
        this.format = format;
        this.streamResource = streamResource;
    }

    @Override
    public List<String> getHeader() throws IOException{
        List<String> header = new ArrayList<>();

        Workbook workbook;
        if(format == ResourceFormat.XLS)workbook = new HSSFWorkbook(new ByteArrayInputStream(streamResource.getContent()));
        else workbook = new XSSFWorkbook(new ByteArrayInputStream(streamResource.getContent()));
        Sheet sheet = workbook.getSheetAt(sheetNumber-1);
        Row row = sheet.getRow(0);
        for (Cell cell : row)
            header.add(cell.toString());

        workbook.close();

        return header;
    }

    @Override
    public List<Statement> getRowStatements(List<String> header, List<Column> outputColumns, TableSchema tableSchema) throws IOException {
        List<Statement>statements = new ArrayList<>();

        Workbook workbook;
        if(format == ResourceFormat.XLS)workbook = new HSSFWorkbook(new ByteArrayInputStream(streamResource.getContent()));
        else workbook = new XSSFWorkbook(new ByteArrayInputStream(streamResource.getContent()));
        Sheet sheet = workbook.getSheetAt(sheetNumber-1);

        for (int i = 1;i < sheet.getPhysicalNumberOfRows();i++) {
            Row row = sheet.getRow(i);
            for (Cell cell : row) {
                int rowNumber = cell.getRowIndex();
                int colNumber = cell.getColumnIndex();
                Column column = outputColumns.get(colNumber);
                String cellValue = cell.toString();
                if (cellValue != null) statements.add(createRowResource(cellValue, rowNumber, column,tableSchema));
            }
        }

        return statements;
    }

    @Override
    public int getNumberOfRows(){
        Workbook workbook;
        try {
            if (format == ResourceFormat.XLS) workbook = new HSSFWorkbook(new ByteArrayInputStream(streamResource.getContent()));
            else workbook = new XSSFWorkbook(new ByteArrayInputStream(streamResource.getContent()));
        } catch (IOException e) {
                throw new RuntimeException(e);
        }
        Sheet sheet = workbook.getSheetAt(sheetNumber-1);

        return sheet.getPhysicalNumberOfRows() - 1;
    }

    private Statement createRowResource(String cellValue, int rowNumber, Column column, TableSchema tableSchema) {
        Resource rowResource = ResourceFactory.createResource(tableSchema.createAboutUrl(rowNumber));

        return ResourceFactory.createStatement(
                rowResource,
                ResourceFactory.createProperty(column.getPropertyUrl()),
                ResourceFactory.createPlainLiteral(cellValue));
    }
}
