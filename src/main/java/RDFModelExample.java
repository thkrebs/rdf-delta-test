import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.VCARD;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.DCTypes;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.RDF;

import java.util.UUID;

public class RDFModelExample {

    public static void main(String[] args) {
        // used vocabulary is not thought through and is not relevant for this use case
        String nsMdp = "http://mdp#";
        String nsDCAT = "http://www.w3.org/ns/dcat#";
        String nsLOCN = "https://www.w3.org/ns/locn#";
        String nsOGC     = "http://www.opengis.net/ont/geosparql#";
        String pubURI    = "http://mdp/pub";
        String givenName    = "John";
        String familyName   = "Smith";
        String fullName     = givenName + " " + familyName;
        String title        = "pub title";


        // create an empty Model
        Model model = ModelFactory.createDefaultModel();

        final String uuid = UUID.randomUUID().toString().replace("-", "");

        Resource publication
                = model.createResource(pubURI)
                .addProperty(DCTerms.creator, fullName)
                .addLiteral(DCTerms.created, System.currentTimeMillis())
                .addLiteral(DCTerms.modified,System.currentTimeMillis())
                .addProperty(
                        model.createProperty(nsMdp + "frequency"), "everyMinute")
                .addProperty(DCTerms.type, "Publication")
                .addProperty(DCTerms.identifier, uuid)
                .addProperty(DCTerms.title, title )
                .addProperty(
                        model.createProperty(nsMdp + "download"),
                        model.createResource()
                                 .addProperty(DCTerms.title, title)
                                 .addProperty(DCTerms.format,"application/json")
                                 .addProperty(DCTerms.source,"http://www.sss")
                )
                .addProperty(DCTerms.spatial,
                            model.createResource()
                            .addProperty(model.createProperty(nsOGC + "asWKT"),
                                       "POLYGON((8.938072886283729 50.1454812418305,10.146568980033729 50.42627199652193,10.454186167533729 50.0891242062885,11.783531870658729 49.85594983487162,11.552818980033729 49.578922368032906,11.102379526908729 49.557547082694164,10.849693980033729 49.4290987716831,10.201500620658729 49.550419907636545,9.871910776908729 49.706977583289955,9.487389292533729 49.58604538333982,9.300621714408729 49.657218352475994,9.201744761283729 49.8771942002736,8.597496714408729 49.91965490428638,8.938072886283729 50.1454812418305))"
                ));

        // now write the model in XML form to a file
        // now write the model in a pretty form

        RDFDataMgr.write(System.out, model, Lang.NTRIPLES);
    }
}