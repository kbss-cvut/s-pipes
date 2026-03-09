# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.7.0] - 2025-07-03

### Changed
- Upgraded Apache Jena from 3.17.0 to 5.2.0
- Migrated deprecated Jena APIs:
  - QueryExecutionFactory → QueryExecutionHTTP
  - RDFFormat.JSONLD_EXPAND_FLAT → JSONLD_FLAT
  - jena-tdb → jena-tdb1
  - RDFDataMgr.parse → RDFParser.source.lang.parse
- Replaced unsupported Jena reified statement API with custom implementation
- Migrated reified statements API to use java.nio.file.Files

### Removed
- Tarql module due to incompatibility with new Jena version

## [0.6.0] - 2025-07-03

### Changed
- Migrated JOPA from 0.19.3 to 2.2.0
- Upgraded Eclipse RDF4J from 3.7.3 to 5.0.3
- Refactored dependency management across modules
- Removed jopa-spring-boot-loader dependency

### Fixed
- Compatibility issues with Spring+JOPA

### Added
- Tag support to Docker image push workflows

## [0.5.0] - 2025-06-30

### Changed
- Major refactoring to remove SPIN dependencies [See Migration Guide](doc/migration-guide.md#version-050---core-script-vocabulary-changed)
- Replaced SPIN-based expression evaluation with custom SPINUtils.evaluate()
- Replaced SPINModuleRegistry with SPipesUtil based on SHACL API
- Migrated spin vocabulary classes to custom implementations
- Refactored BaseRDFNodeHandler.getEffectiveValue

### Removed
- SPIN RDF queries from TTL files
- SPIN-based module registry

### Fixed
- Text analysis module strange groupid in s-pipes-modules-registry pom

## [0.4.0] - 2025-06-27

### Added
- Tag support to Docker engine image push workflow
- Initial versioning and release structure
