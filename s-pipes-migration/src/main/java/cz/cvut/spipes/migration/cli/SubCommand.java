package cz.cvut.spipes.migration.cli;

public enum SubCommand {

    MIGRATE("migrate", MigrateCLI.class),
    REFORMAT("reformat", ReformatCLI.class);

    String name;
    Class klass;

    SubCommand(String name, Class klass) {
        this.name = name;
        this.klass = klass;
    }

    public Class getAssociatedClass() {
        return klass;
    }

    @Override
    public String toString() {
        return name;
    }
}
