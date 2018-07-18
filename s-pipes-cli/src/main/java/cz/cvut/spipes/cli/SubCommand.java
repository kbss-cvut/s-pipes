/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.spipes.cli;

/**
 *
 * @author blcha
 */
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
