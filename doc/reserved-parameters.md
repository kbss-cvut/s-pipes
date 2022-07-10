#Reserved Parameter Names for SPipes REST API

There is naming convention of reserved parameters for HTTP requests. All parameters except of a special parameter start with prefix `_p`.

* `_pId` - *id* of a *function* / *module* to be executed
* `_pConfigURL` - URL of the resource containing configuration. 
* `_pInputBindingURL` - URL to load *input bindings* of a *module* / *function*.
* `_pOutputBindingURL` - URL to save *output bindings* of a *module* / *function*.