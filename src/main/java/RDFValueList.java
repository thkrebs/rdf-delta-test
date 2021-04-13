import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.tdb.store.Hash;
import org.apache.jena.vocabulary.SKOS;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Demonstrate how a multilanguage list of valid values can be constructed from a SKOS vocabulary
 */
public class RDFValueList {

    static final String labelURI = "http://www.w3.org/2004/02/skos/core#prefLabel";

    public static void main(String[] args) {
        String uri = "http://publications.europa.eu/resource/authority/data-theme";
        if (args.length > 1) {
            uri = args[0];
        }

        // results are stored in a map each label is mapped to a hashmap which hashes the language key to its translation
        // label id -> (language -> localized label )
        HashMap<String,Map> labels = new HashMap<String, Map>();
        Model model = RDFDataMgr.loadModel("http://publications.europa.eu/resource/authority/data-theme", Lang.RDFXML);
        for (StmtIterator i = model.listStatements(); i.hasNext(); ) {
            Statement stmt = i.nextStatement();  // get next statement
            Resource subject = stmt.getSubject();     // get the subject
            String localResName = subject.getLocalName();
            Property predicate = stmt.getPredicate();   // get the predicate
            RDFNode object = stmt.getObject();      // get the object
            Model topicModel = RDFDataMgr.loadModel(subject.getURI(), Lang.RDFXML);
            Resource r = model.getResource(subject.getURI());

            Map langMap = new HashMap<String, String>();

            // select the label statements
            StmtIterator it = topicModel.listStatements(
                    new SimpleSelector(null, null, (RDFNode) null) {
                        @Override
                        public boolean selects(Statement s) {
                            Property p = s.getPredicate();
                            return p.getURI().equals(labelURI);
                        }
                    }
            );

            for (; it.hasNext();) {
                Statement s = it.nextStatement();
                langMap.put(s.getLanguage(),s.getLiteral().getString());
            }
            labels.put(localResName,langMap);

        }
        System.out.println("Done");
        // check
        System.out.println(labels.get("EDUC").get("de").toString());
    }
}
