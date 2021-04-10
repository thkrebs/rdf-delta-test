import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class QueryBench extends Thread {

    private ArrayList<String> ids;  // sample of ids in database
    private int threads;
    private Random rand;
    private boolean running;
    private long time;
    private String uri;

    private List<Double> responseTimes;

    public QueryBench(int threads, long time, String uri) {
        this.threads = threads;
        this.ids = new ArrayList<String>();
        this.rand = new Random();
        this.running = true;
        this.time = time;
        this.uri = uri;
        this.responseTimes = Collections.synchronizedList(new ArrayList<Double>());
    }

    public void initIds() {
        ResultSet results = execQuery(this.uri,
                "prefix dcat: <http://www.w3.org/ns/dcat#> \n" +
                        "prefix dct: <http://purl.org/dc/terms/> \n" +
                        "SELECT (?_id AS ?id) WHERE { ?x a dcat:Dataset  . \n" +
                        "?x dct:identifier ?_id} LIMIT 10000",
                false);
        while(results.hasNext()) {
            QuerySolution qs = results.next();
            Literal l = qs.get("?id").asLiteral();
            this.ids.add(l.toString());
        }
    }

    public long getTriples() {
        ResultSet results = execQuery(this.uri,
                "SELECT (COUNT(?x) AS ?cnt) WHERE { ?x ?y ?z }",
                false);
        Literal ret = null;
        if (results.hasNext()) {
            QuerySolution qs = results.next();
            ret = qs.get("?cnt").asLiteral();
        }
        return ret.getLong();
    }

    public String getUri() {
        return this.uri;
    }

    public String getRandId() {
        return this.ids.get(Math.abs(this.rand.nextInt()) % this.ids.size());
    }

    public boolean running() {
        return this.running;
    }

    public ResultSet execQuery(String serviceURI, String query, boolean record) {
        long start = System.currentTimeMillis();
        QueryExecution q = QueryExecutionFactory.sparqlService(serviceURI, query);
        ResultSet results = q.execSelect();
        long end = System.currentTimeMillis();
        System.out.format("Query executed in %d (ms) \n", (end-start) );
        if (record) {
            this.responseTimes.add((double) end-start);
        }
        return results;
    }

    public Model execConstruct(String serviceURI, String query, boolean record) {
        long start = System.currentTimeMillis();
        QueryExecution q = QueryExecutionFactory.sparqlService(serviceURI, query);
        Model m = q.execConstruct();
        long end = System.currentTimeMillis();
        System.out.format("Query executed in %d (ms)\n", (end-start) );
        if (record) {
            this.responseTimes.add((double) end-start);
        }
        return m;
    }

    public void runTest() throws InterruptedException {
        ExecutorService es = Executors.newFixedThreadPool(this.threads);
        for(int i=0; i < this.threads; i++)
            es.execute(new QueryRunner(this));
        sleep(this.time);
        System.out.println("Waiting for completion...\n");
        this.running = false;
        es.shutdown();
        boolean finished = es.awaitTermination(1, TimeUnit.MINUTES);
    }

    public Map<String, Double> getStats() {
        Double[] arr = new Double[this.responseTimes.size()];
        arr = responseTimes.toArray(arr);
        double[] vals = ArrayUtils.toPrimitive(arr);
        DescriptiveStatistics stats = new DescriptiveStatistics(vals);

        // collect results
        HashMap<String, Double> results = new HashMap<String, Double>();
        results.put("p50",stats.getPercentile(50));
        results.put("p90",stats.getPercentile(90));
        results.put("p95",stats.getPercentile(95));
        results.put("p99",stats.getPercentile(99));
        results.put("p99.9",stats.getPercentile(99.9));
        results.put("min",stats.getMin());
        results.put("max", stats.getMax());
        results.put("count", (double) this.responseTimes.size());

        return results;

    }
    public static void main(String[] argv) throws IOException {
        int threads = Integer.parseInt(argv[0]);
        int time = Integer.parseInt(argv[1]);
        String uri = argv[2];

        System.out.format("Running test for %d(s) with %d threads\n", time, threads);

        QueryBench qb = new QueryBench(threads, time * 1000, uri);
        // get the ids in the database from whichh we are sampling
        qb.initIds();
        try {
            qb.runTest();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // prepare the stats
        Double nTriples = (double) qb.getTriples();
        Map<String, Double> results = qb.getStats();
        results.put("triples",nTriples);

        final String DELIM = ",";
        StringBuffer sb = new StringBuffer();
        SimpleDateFormat formatter= new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        sb.append(formatter.format(date)).append(DELIM);

        // make sure data is written always in the same order to be able to append to CSV file
        SortedSet<String> keys = new TreeSet<>(results.keySet());
        for (String key : keys) {
            Double value = results.get(key);
            sb.append(value).append(DELIM);
        }
        sb.append("\n");

        // append result to result file
        Writer output;
        output = new BufferedWriter(new FileWriter("results.csv",true));  //clears file every time
        output.append(sb);
        output.close();
        System.out.println(results);
    }
}
