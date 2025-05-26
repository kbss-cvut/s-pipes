# Tabular module

## Model 

![Model of CSVW ontology and its KBSS extension](./doc/tabular-model.svg)

## Supported input
The tabular module supports tables in the following formats:
- HTML `<table>`
- excel XLS, XLSM and XLSX
- CSV with configurable quote character, delimiter and input file encoding

### HTML Specifics
When processing html tables, tabular module converts cell values to plain text or html based on the `preserveTags` 
property of table elements. Whether to convert cell value to plain text or html is determined based on the value of the 
first define `preserveTags` attribute of cell, row and table. If non is defined convert cell value to plain text.

Here are some examples:
- convert all cell values in table to plain text - no need to specify `preserveTags` anywhere
- preserve all tags in all cells - specify `preserveTags="true"` only at table element
```
<table preserveTags="true">
...
<table>
```

- preserve tags for description column - specify `preserveTags="true"` only at the columns the cell values of which
should retain tags 
```
<table >
<ht><td>Id</td>  <td>Label</td>   <td preserveTags="true">Description</td></th>
<tr><td>1</td>   <td>Sugar</td>   <td><span>Sugar</span> is sweet</td></tr>
<tr><td>2</td>   <td>Salt</td>    <td preserveTags="true"><a href="...">Salt</a> is salty</td></tr>
...
<table>
```
- preserve tags all rows in description column except for row 2 - specify `preserveTags="true"` at selected columns and `preserveTags="false"` for selected to be converted to plain text
```
<table >
<ht><td>Id</td>  <td>Label</td>   <td preserveTags="true">Description</td></th>
<tr><td>1</td>   <td>Sugar</td>   <td><span>Sugar</span> is sweet</td></tr>
<tr><td>2</td>   <td>Salt</td>    <td preserveTags="true"><a href="...">Salt</a> is salty</td></tr>
...
<table>
```