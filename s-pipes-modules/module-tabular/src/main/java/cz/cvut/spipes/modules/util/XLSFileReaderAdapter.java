package cz.cvut.spipes.modules.util;

import cz.cvut.spipes.modules.ResourceFormat;
import cz.cvut.spipes.modules.model.Region;
import cz.cvut.spipes.registry.StreamResource;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class XLSFileReaderAdapter implements FileReaderAdapter {
    private Sheet sheet;
    private Iterator<org.apache.poi.ss.usermodel.Row> rowIterator;

    @Override
    public void initialise(StreamResource sourceResource, ResourceFormat sourceResourceFormat, int tableIndex) throws IOException {
        Workbook workbook;
        if (sourceResourceFormat == ResourceFormat.XLS) {
            workbook = new HSSFWorkbook(new ByteArrayInputStream(sourceResource.getContent()));
        } else {
            workbook = new XSSFWorkbook(new ByteArrayInputStream(sourceResource.getContent()));
        }
        sheet = workbook.getSheetAt(tableIndex - 1);
        rowIterator = sheet.iterator();
    }

    @Override
    public String[] getHeader() throws IOException {
        org.apache.poi.ss.usermodel.Row headerRow = sheet.getRow(0);
        return StreamSupport.stream(headerRow.spliterator(), false)
                .map(cell -> cell.getStringCellValue())
                .toArray(String[]::new);
    }

    @Override
    public boolean hasNext() {
        return rowIterator.hasNext();
    }

    @Override
    public List<String> getNextRow() {
        if (!rowIterator.hasNext())
            return null;
        org.apache.poi.ss.usermodel.Row currentRow = rowIterator.next();
        DataFormatter formatter = new DataFormatter();
        List<String> row = StreamSupport.stream(currentRow.spliterator(), false)
                .map(cell -> {
                    String cellValue = formatter.formatCellValue(cell);
                    if (cellValue != null && cellValue.matches("[-+]?[0-9]*\\,?[0-9]+")) {
                        cellValue = cellValue.replace(",", ".");
                    }
                    return cellValue.isEmpty() ? null : cellValue;
                })
                .collect(Collectors.toList());
        return row;
    }

    @Override
    public List<Region> getMergedRegions(StreamResource sourceResource) {
        List<Region> regions = new ArrayList<>();
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress region = sheet.getMergedRegion(i);
            regions.add(new Region(
                    region.getFirstRow(),
                    region.getFirstColumn(),
                    region.getLastRow(),
                    region.getLastColumn()
            ));
        }
        return regions;
    }

    public String getLabel(){
        return sheet.getSheetName();
    }
}

