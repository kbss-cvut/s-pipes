package cz.cvut.spipes.modules.util;

import cz.cvut.spipes.registry.StreamResource;
import cz.cvut.spipes.registry.StringStreamResource;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Module for converting tabular data from XLS to TSV. Converts specific sheet of the xls file.
 */
public class XLS2TSVConvertor {

    public StringStreamResource convertToTSV(StreamResource streamResource,int sheetNumber){
        try {
            Workbook workbook = new HSSFWorkbook(new ByteArrayInputStream(streamResource.getContent()));
            Sheet sheet = workbook.getSheetAt(sheetNumber-1);

            StringBuilder tsvStringBuilder = new StringBuilder();
            for (Row row : sheet) {
                for (Cell cell : row) {
                    tsvStringBuilder.append(cell.toString().replace('\t', ' '));
                    tsvStringBuilder.append('\t');
                }
                tsvStringBuilder.deleteCharAt(tsvStringBuilder.length() - 1);
                tsvStringBuilder.append('\n');
            }
            return new StringStreamResource(
                    streamResource.getUri(),
                    tsvStringBuilder.toString().getBytes(),
                    "text/tsv"
            );
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getNumberOfSheets(StreamResource streamResource){
        try {
            return new HSSFWorkbook(new ByteArrayInputStream(streamResource.getContent())).getNumberOfSheets();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
