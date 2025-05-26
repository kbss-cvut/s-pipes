# Tabular module

## Model 

![Model of CSVW ontology and its KBSS extension](./doc/tabular-model.svg)

## Supported input
The tabular module supports tables in the following formats:
- HTML `<table>`
- excel XLS, XLSM and XLSX
- CSV with configurable quote character, delimiter and input file encoding

### HTML Specifics
Tabular module converts cell values in HTML tables to plain text or html (preserves html tags) based on the `preserveTags`
attributes defined in the table and its elements. HTML tags in a cell value are preserved if a `preserveTags=true`
attribute is specified on the cell, row, column header or table element. Otherwise, cell value is converted to plain text.

Here are some examples:
- to convert all cell values in a table to plain text - no need to specify `preserveTags` anywhere
- to preserve all tags in all cells - specify `preserveTags="true"` only on table element
```
<table preserveTags="true">
...
</table>
```

- to preserve tags of cell values of a column (`Description`) - specify `preserveTags="true"` on the column (`Description`)
```
<table >
<ht><td>Id</td>  <td>Label</td>   <td preserveTags="true">Description</td></th>
<tr><td>1</td>   <td>Sugar</td>   <td><span>Sugar</span> is sweet</td></tr>
<tr><td>2</td>   <td>Salt</td>    <td><a href="...">Salt</a> is salty</td></tr>
...
</table>
```
- to preserve tags for all rows in a column (`Description`) except for cell at row 2 - specify `preserveTags="true"` 
on the column (`Description`) and `preserveTags="false"` on second row cell in the column (`Description`).
```
<table>
<ht><td>Id</td>  <td>Label</td>   <td preserveTags="true">Description</td></th>
<tr><td>1</td>   <td>Sugar</td>   <td><span>Sugar</span> is sweet</td></tr>
<tr><td>2</td>   <td>Salt</td>    <td preserveTags="false"><a href="...">Salt</a> is salty</td></tr>
...
</table>
```