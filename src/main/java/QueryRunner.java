import java.util.concurrent.Callable;

public class QueryRunner extends Thread  {

    private final QueryBench queryBench;

    public QueryRunner(QueryBench qb) {
        queryBench = qb;
    }

    public void run() {
        int i = 0; // we skip the first 100 queries -> allow for some warmup

        while (queryBench.running()) {
            queryBench.execConstruct(
                    queryBench.getUri(),
                    "prefix dcat: <http://www.w3.org/ns/dcat#> \n" +
                            "prefix dct: <http://purl.org/dc/terms/> \n" +
                            "prefix foaf: <http://xmlns.com/foaf/0.1/> \n" +
                            "prefix dcat: <http://www.w3.org/ns/dcat#> \n" +
                            "prefix dcatde: <http://dcat-ap.de/def/dcatde/> \n" +
                            "CONSTRUCT {  ?id ?prop ?val . \n" +
                            "?child ?childProp ?childPropVal . \n" +
                            "?someSubj ?incomingChildProp ?child . \n" +
                            "} \n" +
                            "WHERE { \n" +
                            "?id dct:identifier \"" + queryBench.getRandId() + "\"  . \n" +
                            "?id ?prop ?val ; \n" +
                            "(dct:accrualPeriodicity|!dct:accrualPeriodicity)+ ?child . \n" +
                            "?child ?childProp ?childPropVal. \n" +
                            " ?someSubj ?incomingChildProp ?child. }"
                    , (i++ >= 100));
        }
    }
}
