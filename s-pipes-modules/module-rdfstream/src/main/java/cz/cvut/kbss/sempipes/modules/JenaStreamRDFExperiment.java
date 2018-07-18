/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.kbss.sempipes.modules;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.mem.GraphMem;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelChangedListener;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.ModelCom;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;

/**
 *
 * @author Bogdan Kostov <bogdan.kostov@fel.cvut.cz>
 */
public class JenaStreamRDFExperiment {
    
    public static void main(String[] args) {
        testStreamGraphImplementation();
    }
    
    public static void testStreamGraphImplementation() {
        final Model m = new ModelCom(new GraphMem(){
            @Override
            public void add(Triple t) {
                System.out.println("listener: recieving tripple : " + t);
            }
        });
        runMockParser(m);
    }
    
    
    
    public static void testStreamTestWithModelChangedListener() {
        final Model m = ModelFactory.createDefaultModel();
        m.register(new ModelChangedListener() {
            @Override
            public void addedStatement(Statement s) {
                m.remove(s);
                System.out.println("listener: recieving tripple : " + s);
            }
            @Override
            public void addedStatements(Statement[] statements) {}
            @Override
            public void addedStatements(List<Statement> statements) {}
            @Override
            public void addedStatements(StmtIterator statements) {}
            @Override
            public void addedStatements(Model m) {}
            @Override
            public void removedStatement(Statement s) {}
            @Override
            public void removedStatements(Statement[] statements) {}
            @Override
            public void removedStatements(List<Statement> statements) {}
            @Override
            public void removedStatements(StmtIterator statements) {}
            @Override
            public void removedStatements(Model m) {}
            @Override
            public void notifyEvent(Model m, Object event) {}
            
        });
        runMockParser(m);
    }
    
    
    protected static void runMockParser(Model m){
        final StreamRDF dest = StreamRDFLib.graph(m.getGraph()) ;
        try {
            System.out.println("mock parser: sending first triple");
            dest.triple(new Triple(
                    NodeFactory.createURI("http://ont.fel.cvut.cz/Person"),
                    NodeFactory.createURI("http://ont.fel.cvut.cz/birthDate"),
                    NodeFactory.createLiteralByValue(new Date(), XSDDatatype.XSDdate)));

            Thread.sleep(3000);
            System.out.println("mock parser: sending second triple");
            dest.triple(new Triple(
                    NodeFactory.createURI("http://ont.fel.cvut.cz/Person"),
                    NodeFactory.createURI("http://ont.fel.cvut.cz/name"),
                    NodeFactory.createLiteral("George", "en")));
        } catch (InterruptedException ex) {
            Logger.getLogger(JenaStreamRDFExperiment.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
