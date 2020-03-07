package cz.cvut.spipes.cli;

public enum SubCommand {

    EXECUTE_MODULE("execute", ExecuteModuleCLI.class);

    String name;
    Class klass;
    
    private SubCommand(String name, Class klass) {
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
