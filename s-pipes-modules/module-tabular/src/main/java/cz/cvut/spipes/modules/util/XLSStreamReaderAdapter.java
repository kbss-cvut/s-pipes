package cz.cvut.spipes.modules.util;

import cz.cvut.spipes.modules.ResourceFormat;
import cz.cvut.spipes.modules.exception.SheetDoesntExistsException;
import cz.cvut.spipes.modules.model.Region;
import cz.cvut.spipes.registry.StreamResource;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class XLSStreamReaderAdapter implements StreamReaderAdapter {
    private Sheet sheet;
    private Iterator<org.apache.poi.ss.usermodel.Row> rowIterator;
    Boolean skipHeader;
    private static final Logger LOG = LoggerFactory.getLogger(XLSStreamReaderAdapter.class);

    @Override
    public void initialise(InputStream inputStream, ResourceFormat sourceResourceFormat, int tableIndex, StreamResource sourceResource) throws IOException {
        Workbook workbook;
        if (sourceResourceFormat == ResourceFormat.XLS) {
            workbook = new HSSFWorkbook(inputStream);
        } else {
            workbook = new XSSFWorkbook(inputStream);
        }
        LOG.debug("Number of sheets: {}", workbook.getNumberOfSheets());
        if ((tableIndex > workbook.getNumberOfSheets()) || (tableIndex < 1)) {
                LOG.error("Requested sheet doesn't exist, number of sheets in the doc: {}, requested sheet: {}",
                        workbook.getNumberOfSheets(),
                        tableIndex
                );
                    throw new SheetDoesntExistsException("Requested sheet doesn't exists.");
                }
        sheet = workbook.getSheetAt(tableIndex - 1);
        rowIterator = sheet.iterator();
    }

    @Override
    public String[] getHeader(boolean skipHeader) throws IOException {
        Row headerRow = sheet.getRow(0);
        if (skipHeader) {
            return null;
        }
        else {
            rowIterator.next(); // move iterator to 2nd row
            return StreamSupport.stream(headerRow.spliterator(), false)
                    .map(cell -> cell.getStringCellValue())
                    .toArray(String[]::new);
        }
    }

    @Override
    public List<String> getNextRow() {
        if (!rowIterator.hasNext())
            return null;
        Row currentRow = rowIterator.next();
        DataFormatter formatter = new DataFormatter();
        List<String> row = StreamSupport.stream(currentRow.spliterator(), false)
                .map(cell -> {
                    String cellValue = formatter.formatCellValue(cell);
                    cellValue = fixNumberFormat(cellValue);
                    return cellValue.isEmpty() ? null : cellValue;
                })
                .collect(Collectors.toList());
        return row;
    }

    @Override
    public List<Region> getMergedRegions() {
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

    @Override
    public String getSheetLabel(){
        return sheet.getSheetName();
    }

    public String fixNumberFormat (String cellValue){
        //xls uses ',' as decimal separator, so we should convert it to '.'
        if (cellValue != null && cellValue.matches("[-+]?[0-9]*\\,?[0-9]+")) {
            cellValue = cellValue.replace(",", ".");
        }
        return cellValue;
    }

    @Override
    public void close() {
    }
}

