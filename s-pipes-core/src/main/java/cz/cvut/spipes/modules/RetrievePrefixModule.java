package cz.cvut.spipes.modules;

import org.apache.jena.shared.PrefixMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RetrievePrefixModule{
    private static final Logger LOG = LoggerFactory.getLogger(RetrievePrefixModule.class.getName());
    PrefixMapping prefixMapping = PrefixMapping.Factory.create();

    public void retrieveFromScripts(ArrayList<String> scriptPaths){
        for (String scriptPath : scriptPaths) {
            getPrefixesFromFile(scriptPath);
        }
    }
    private void getPrefixesFromFile(String filePath){
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String pattern = "@prefix\\s+(.*):\\s+<([^>]+)>.*";
                Pattern prefixPattern = Pattern.compile(pattern);
                Matcher matcher = prefixPattern.matcher(line);
                if(matcher.matches()){
                    String prefix = matcher.group(1);
                    String uri = matcher.group(2);
                    if( (prefixMapping.getNsPrefixURI(prefix) != null) && (!Objects.equals(prefixMapping.getNsPrefixURI(prefix), uri))){
                        LOG.info("Conflict assigning prefix \"{}\" to {}. Prefix \"{}\" is already assigned to {}",
                                prefix,
                                uri,
                                prefix,
                                prefixMapping.getNsPrefixURI(prefix));
                    }
                    else prefixMapping.setNsPrefix(prefix,uri);
//                    System.out.println("Pref: \""+prefix+"\"");
//                    System.out.println("uri: \""+uri+"\"\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, String> getPrefixMap(){
        return prefixMapping.getNsPrefixMap();
    }

//    public static void main(String[] args) {
//        Model model = ModelFactory.createDefaultModel();
////        Resource subject = ResourceFactory.createResource("http://example.org/resorce/person");
////        Property property = ResourceFactory.createProperty("http://example.org/property/hasName");
////        Literal literal = ResourceFactory.createPlainLiteral("alex");
////        Statement statement = ResourceFactory.createStatement(subject,property,literal);
////        model.add(statement);
//
//        PrefixMapping prefixMapping = PrefixMapping.Factory.create();
//        prefixMapping.setNsPrefix("foaf", "http://xmlns.com/foaf/0.1/");
//        model.setNsPrefixes(prefixMapping.getNsPrefixMap());
//        System.out.println(prefixMapping.numPrefixes());
//        System.out.println(model.getNsPrefixMap());
//
//        System.out.println("\n\n");
//        Resource person = model.createResource("foaf:Person");
//        Resource subject = model.createResource("http://example.com/subject");
//        model.add(subject, RDF.type, person);
//        model.write(System.out,"TURTLE");
//    }
}