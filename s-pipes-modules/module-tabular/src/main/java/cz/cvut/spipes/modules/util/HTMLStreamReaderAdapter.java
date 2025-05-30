package cz.cvut.spipes.modules.util;

import cz.cvut.spipes.modules.ResourceFormat;
import cz.cvut.spipes.modules.model.Region;
import cz.cvut.spipes.registry.StreamResource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;
import java.util.stream.Stream;

public class HTMLStreamReaderAdapter implements StreamReaderAdapter {
    public static final String PRESERVE_TAGS = "preserveTags";

    private Elements rows;
    private int currentIndex;
    private Element table;
    private String label;
    private Boolean tablePreserveTags;
    private Boolean[] columnPreserveTags;

    private List<Region> mergedRegions;
    private Map<Integer, Map<Integer, String>> mergedCells;

    @Override
    public void initialise(InputStream inputStream, ResourceFormat sourceResourceFormat,
                           int tableIndex, StreamResource sourceResource) throws IOException {
        Document doc = Jsoup.parse(inputStream, "UTF-8", "");
        Element table = doc.select("table").first();
        rows = table.select("tr");
        currentIndex = 0;
        this.table = table;
        mergedRegions = extractMergedRegions(table);
        mergedCells = new HashMap<>();
        label = table.attr("data-name");
        Elements header = getHeaderElements();
        tablePreserveTags = getPreserveTags(table);
        columnPreserveTags = new Boolean[header.size()];
        for (int i = 0; i < header.size(); i++) {
            columnPreserveTags[i] = getPreserveTags(header.get(i));
        }
    }

    protected Boolean getPreserveTags(Node n) {
        return n.hasAttr(PRESERVE_TAGS) ? Boolean.parseBoolean(n.attr(PRESERVE_TAGS)) : null;
    }

    protected boolean isCellPreserveTags(Node cell, int index){
        Boolean cellPreserveTags = getPreserveTags(cell);
        return Stream.of(cellPreserveTags, columnPreserveTags[index], tablePreserveTags)
                .filter(Objects::nonNull)
                .findFirst().orElse(false);
    }

    public Elements getHeaderElements()  {
        return rows.get(0).select("th, td");
    }
    @Override
    public String[] getHeader(boolean skipHeader) throws IOException {
        Elements headerCells = getHeaderElements();
        return headerCells.stream()
                .map(Element::text)
                .toArray(String[]::new);
    }

    private boolean hasNextRow() {
        return currentIndex < rows.size() - 1; // Skip header row
    }

    @Override
    public List<String> getNextRow() {
        if (!hasNextRow()) {
            return null;
        }

        currentIndex++;
        Elements cells = rows.get(currentIndex).select("td, th");
        List<String> row = new ArrayList<>();
        int cellIndex = 0;

        for (int j = 0; j < cells.size(); j++) {
            Element cell = cells.get(j);
            int colspan = Integer.parseInt(cell.attr("colspan").isEmpty() ? "1" : cell.attr("colspan"));
            int rowspan = Integer.parseInt(cell.attr("rowspan").isEmpty() ? "1" : cell.attr("rowspan"));

            String cellValue = isCellPreserveTags(cell, j) ? cell.html() : cell.text();

            if (cellValue != null && cellValue.matches("[-+]?[0-9]*\\,?[0-9]+")) {
                cellValue = cellValue.replace(",", ".");
            }

            while (row.size() < cellIndex) {
                row.add(null);
            }

            row.add(cellValue);

            for (int i = 1; i < colspan; i++) {
                row.add(null);
            }

            if (rowspan > 1) {
                for (int i = 1; i < rowspan; i++) {
                    mergedCells.computeIfAbsent(currentIndex + i, k -> new HashMap<>()).put(cellIndex, cellValue);
                }
            }

            cellIndex += colspan;
        }

        if (mergedCells.containsKey(currentIndex)) {
            Map<Integer, String> rowMergedCells = mergedCells.get(currentIndex);
            for (Map.Entry<Integer, String> entry : rowMergedCells.entrySet()) {
                row.add(entry.getKey(), null);
            }
            mergedCells.remove(currentIndex);
        }

        return row;
    }

    @Override
    public List<Region> getMergedRegions() {
        return mergedRegions;
    }

    private List<Region> extractMergedRegions(Element table) {
        List<Region> regions = new ArrayList<>();
        Elements rows = table.select("tr");
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            Elements cells = rows.get(rowIndex).select("td, th");
            for (int colIndex = 0; colIndex < cells.size(); colIndex++) {
                Element cell = cells.get(colIndex);
                int colspan = Integer.parseInt(cell.attr("colspan").isEmpty() ? "1" : cell.attr("colspan"));
                int rowspan = Integer.parseInt(cell.attr("rowspan").isEmpty() ? "1" : cell.attr("rowspan"));
                if (colspan > 1 || rowspan > 1) {
                    regions.add(new Region(rowIndex, colIndex, rowIndex + rowspan - 1, colIndex + colspan - 1));
                }
            }
        }
        return regions;
    }

    @Override
    public String getSheetLabel(){
        return label;
    }

    @Override
    public void close() {
    }
}
