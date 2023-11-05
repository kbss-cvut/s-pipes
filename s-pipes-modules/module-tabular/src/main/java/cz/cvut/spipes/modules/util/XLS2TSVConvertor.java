package cz.cvut.spipes.modules.util;

import cz.cvut.spipes.modules.ResourceFormat;
import cz.cvut.spipes.modules.model.Region;
import cz.cvut.spipes.registry.StreamResource;
import cz.cvut.spipes.registry.StringStreamResource;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Module for converting tabular data from XLS to TSV. Converts specific sheet of the xls file.
 */
public class XLS2TSVConvertor implements TSVConvertor {
    private int sheetNumber;
    private ResourceFormat format;

    public XLS2TSVConvertor(int sheetNumber, ResourceFormat format) {
        this.sheetNumber = sheetNumber;
        this.format = format;
    }

    @Override
    public StringStreamResource convertToTSV(StreamResource streamResource){
        try {
            Workbook workbook;
            if(format == ResourceFormat.XLS)workbook = new HSSFWorkbook(new ByteArrayInputStream(streamResource.getContent()));
            else workbook = new XSSFWorkbook(new ByteArrayInputStream(streamResource.getContent()));
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
                    ResourceFormat.TSV.toString()
            );
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Region> getMergedRegions(StreamResource streamResource){
        Workbook workbook;
        List<Region> list = new ArrayList<>();
        try {
            if(format == ResourceFormat.XLS)workbook = new HSSFWorkbook(new ByteArrayInputStream(streamResource.getContent()));
            else workbook = new XSSFWorkbook(new ByteArrayInputStream(streamResource.getContent()));
            Sheet sheet = workbook.getSheetAt(sheetNumber-1);

            for(int i = 0;i < sheet.getNumMergedRegions();i++){
                CellRangeAddress region = sheet.getMergedRegion(i);
                list.add(new Region(
                        region.getFirstRow(),
                        region.getFirstColumn(),
                        region.getLastRow(),
                        region.getLastColumn())
                );
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public int getTablesCount(StreamResource streamResource){
        try {
            if(format == ResourceFormat.XLS)return new HSSFWorkbook(new ByteArrayInputStream(streamResource.getContent())).getNumberOfSheets();
            else return new XSSFWorkbook(new ByteArrayInputStream(streamResource.getContent())).getNumberOfSheets();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getTableName(StreamResource streamResource){
        try {
            Workbook workbook;
            if(format == ResourceFormat.XLS)workbook = new HSSFWorkbook(new ByteArrayInputStream(streamResource.getContent()));
            else workbook = new XSSFWorkbook(new ByteArrayInputStream(streamResource.getContent()));
            Sheet sheet = workbook.getSheetAt(sheetNumber-1);
            return sheet.getSheetName();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
