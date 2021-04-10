import org.apache.commons.csv.CSVPrinter;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.system.Txn;
import org.graalvm.compiler.debug.CSVUtil;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;

public class RDFDatabaseInitializer {
    public static DataGenerator gen = new DataGenerator();

    public static String getTemplate(String filename) {
        String contents = "";
        try {
            contents = new String(Files.readAllBytes(Paths.get(filename)));
        }
        catch(Exception e) {
            System.err.println(e);
        }
        return contents;
    }

    public static String getInstance(String template) {
        String id = gen.getId();
        String theme = gen.getTheme();
        String description = gen.getDescription();

        template = template.replaceAll("\\$\\{id\\}",id);
        template = template.replaceAll("\\$\\{description\\}", description);
        template = template.replaceAll("\\$\\{theme\\}", theme);
        template = template.replaceAll("\\$\\{mail\\}", gen.getMail());
        template = template.replaceAll("\\$\\{name\\}", gen.getName());
        template = template.replaceAll("\\$\\{org_name\\}", gen.getOrgName());
        template = template.replaceAll("\\$\\{org_mail\\}", gen.getOrgMail());
        return template;
    }

    public static void uploadRDF(InputStream in, String serviceURI, ArrayList<Long> stats)
            throws IOException {

        // parse the file
        Model m = ModelFactory.createDefaultModel();
        /* try (FileInputStream in = new FileInputStream(rdf)) {
            m.read(in, null, "TTL");
        }
        System.out.println("----");
        */
        m.read(in, null, "TTL");
        long start = System.currentTimeMillis();
        // upload the resulting model
        try ( RDFConnectionFuseki conn = RDFConnectionFactory.connectFuseki(serviceURI) ) {
            Txn.executeWrite(conn, ()-> {
                conn.load(m);
            }) ;
            conn.commit();
        };
        long end = System.currentTimeMillis();
        long elapsed = end - start;
        System.out.format("Added in %d (ms) \n", (int) (elapsed));
/*
        DatasetAccessor accessor = DatasetAccessorFactory
                .createHTTP(serviceURI);
        long start = System.currentTimeMillis();
        accessor.add(m);
        long end = System.currentTimeMillis();
        long elapsed = end - start;
        stats.add(elapsed);
        System.out.format("Added in %d (ms) \n", (int) (elapsed));*/
        //RDFDataMgr.write(System.out, m, Lang.NTRIPLES);
    }

    public static void execSelectAndPrint(String serviceURI, String query) {
        QueryExecution q = QueryExecutionFactory.sparqlService(serviceURI,
                query);
        ResultSet results = q.execSelect();

        ResultSetFormatter.out(System.out, results);

        while (results.hasNext()) {
            QuerySolution soln = results.nextSolution();
            RDFNode x = soln.get("x");
            System.out.println(x);
        }
    }

    public static void execSelectAndProcess(String serviceURI, String query) {
        QueryExecution q = QueryExecutionFactory.sparqlService(serviceURI,
                query);
        ResultSet results = q.execSelect();

        while (results.hasNext()) {
            QuerySolution soln = results.nextSolution();
            // assumes that you have an "?x" in your query
            RDFNode x = soln.get("x");
            System.out.println(x);
        }
    }

    public static void main(String[] argv) throws IOException {
        String template = getTemplate("test.ttl.template");
        ArrayList<Long> stats = new ArrayList<>();

        // number of records to produce
        int nr = Integer.parseInt(argv[0]);

        for (int i = 1; i <= nr; i++) {
            String inData = getInstance(template);
            InputStream dataStream = new ByteArrayInputStream(inData.getBytes());
            uploadRDF(dataStream, "http://localhost:3031/ds", stats);
        }

        String csvFile = "stats.csv";
        /*
        uploadRDF(new File("test.ttl"), "http://localhost:3031/ds");
        execSelectAndPrint(
                "http://localhost:3031/ds",
                "SELECT ?x ?y WHERE { ?x  ?y <http://www.w3.org/ns/dcat#Distribution>}");

        execSelectAndProcess(
                "http://localhost:3031/ds",
                "SELECT ?x WHERE { ?x  <http://www.w3.org/2001/vcard-rdf/3.0#FN>  \"John Smith\" }");

         */
    }

/*

    public RDFExample1() {}

    public static void main(String ...args) {
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination("http://localhost:3031");

    }*/

}
