import org.apache.jena.query.*;
import org.apache.jena.rdf.model.RDFNode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class RDFQuery {

    public static void execSelectAndPrint(String serviceURI, String query) {
        long start = System.currentTimeMillis();
        QueryExecution q = QueryExecutionFactory.sparqlService(serviceURI,
                query);
        ResultSet results = q.execSelect();
        long end = System.currentTimeMillis();
        System.out.format("Query executed in %d (ms)", (end-start) );
        ResultSetFormatter.out(System.out, results);

        while (results.hasNext()) {
            QuerySolution soln = results.nextSolution();
            RDFNode x = soln.get("x");
            System.out.println(x);
        }
    }

    public static void main(String[] argv) throws IOException {
        execSelectAndPrint(
                "http://localhost:3031/ds",
                "prefix dcat: <http://www.w3.org/ns/dcat#> \n" +
                       // "SELECT (COUNT(?x) AS ?cnt) WHERE { ?x  a dcat:Dataset }");
                       // "SELECT (COUNT(?x) AS ?cnt) WHERE { ?x ?y ?z }");
                        "SELECT (?x AS ?cnt) WHERE { ?x a dcat:Dataset} LIMIT 10");
        execSelectAndPrint(
                "http://localhost:3031/ds",
                "prefix dcat: <http://www.w3.org/ns/dcat#> \n" +
                        "prefix dct: <http://purl.org/dc/terms/> \n" +
                        "prefix foaf: <http://xmlns.com/foaf/0.1/> \n" +
                        "SELECT ?id ?desc ?creator_mbox WHERE { ?id  a dcat:Dataset . \n" +
                          "?id dct:description ?desc . \n " +
                        "?id dct:identifier \"f1fbe8fa-55f4-45c8-9822-e47a1da600bb\" ." +
                        "?id dct:creator ?c ." +
                        "?c foaf:mbox ?creator_mbox}");

        /* id <https://portal.dih.telekom.net/dataset/97dcda26-d36a-46e0-ada3-ba49627eadff> */
    }

}
