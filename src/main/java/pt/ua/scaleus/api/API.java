/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.scaleus.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.text.EntityDefinition;
import org.apache.jena.query.text.TextDatasetFactory;
import org.apache.jena.query.text.TextIndexConfig;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.resultset.ResultsFormat;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.util.PrintUtil;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDFS;
import org.apache.log4j.Logger;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;

import pt.ua.scaleus.service.data.NQuad;
import pt.ua.scaleus.service.data.NTriple;

/**
 *
 * @author Pedro Sernadela <sernadela at ua.pt>
 */
public class API {

    private static final Logger log = Logger.getLogger(API.class);
    //String input = "resources/data/output.rdf";
    String directory = "datasets/";
    String index = "index/";
    HashMap<String, Dataset> datasets = new HashMap<>();

    public HashMap<String, Dataset> getDatasets() {
        return datasets;
    }

    public API() {
        initDatasets();
    }

    public final void initDatasets() {
        File mainDir = new File(directory);
        if (!mainDir.exists()) {
            mainDir.mkdir();
        }
        String[] datasets_list = getDatasetsList();
        for (String dataset : datasets_list) {
            getDataset(dataset);
        }
    }

    /**
     * Get all datasets list
     *
     * @return
     */
    public String[] getDatasetsList() {
        return Utils.getFolderContentList(directory);
    }

    /**
     * Generates PREFIX set for SPARQL querying.
     *
     * @param database
     * @return a String with the PREFIX set.
     */
    public String getSparqlPrefixes(String database) {
        Map<String, String> prefixes = getNsPrefixMap(database);
        String p = "";
        for (String o : prefixes.keySet()) {
            p += "PREFIX " + o + ": " + "<" + prefixes.get(o) + ">\n";
        }
        return p;
    }

    public Map<String, String> getNsPrefixMap(String database) {
        Dataset dataset = getDataset(database);
        Map<String, String> namespaces = null;
        dataset.begin(ReadWrite.READ);
        try {
            Model model = dataset.getDefaultModel();
            namespaces = model.getNsPrefixMap();
            //model.close();
        } finally {
            dataset.end();
        }
        return namespaces;
    }

    public void setNsPrefix(String database, String prefix, String namespace) {
        Dataset dataset = getDataset(database);
        dataset.begin(ReadWrite.WRITE);
        try {
            Model model = dataset.getDefaultModel();
            model.setNsPrefix(prefix, namespace);
            dataset.commit();
            //model.close();
        } finally {
            dataset.end();
        }
    }

    public void removeNsPrefix(String database, String prefix) {
        Dataset dataset = getDataset(database);
        dataset.begin(ReadWrite.WRITE);
        try {
            Model model = dataset.getDefaultModel();
            model.removeNsPrefix(prefix);
            dataset.commit();
            //model.close();
        } finally {
            dataset.end();
        }
    }

    public Dataset getDataset(String name) {
        Dataset dataset = null;
        if (datasets.containsKey(name)) {
            dataset = datasets.get(name);
        } else {
            log.debug("Loading... " + name);

            Dataset dataset_aux = TDBFactory.createDataset(directory + name);
            Model dataset_model = ModelFactory.createOntologyModel();

            dataset_aux.begin(ReadWrite.READ);
            try {
                dataset_model = dataset_aux.getDefaultModel();
            } finally {
                dataset_aux.end();
            }

            // Define the index mapping 
            EntityDefinition entDef = new EntityDefinition("uri", "text");
            entDef.setPrimaryPredicate(RDFS.label.asNode());
            entDef.setUidField("uid");//synchronized within the index
            entDef.setLangField("lang");

            //Disable properties from index used for literals and resources at same time
            ExtendedIterator<Statement> op = dataset_model.listStatements();
            Set<Property> literal_props = new HashSet<>();
            Set<Property> other_props = new HashSet<>();
            while (op.hasNext()) {
                Statement stat = op.next();
                if (stat.getObject().isLiteral()) {
                    literal_props.add(stat.getPredicate());
                } else {
                    other_props.add(stat.getPredicate());
                }
            }
            Set<Property> intersection = new HashSet<>(literal_props);
            intersection.retainAll(other_props);
            literal_props.removeAll(intersection);
            for (Property prop : literal_props) {
                entDef.setPrimaryPredicate(prop);
            }

            // Lucene, in memory.
            // Join together into a dataset
            TextIndexConfig config = new TextIndexConfig(entDef);
            config.setValueStored(true);//save literals

            File indexFile = new File(index + name);
            boolean indexIsCreated = indexFile.exists();

            try {
                Directory dir = new SimpleFSDirectory(indexFile);
                dataset = TextDatasetFactory.createLucene(dataset_aux, dir, config);
            } catch (IOException ex) {
                log.error("Index disabled in " + name);
                dataset = dataset_aux; //Lucene is disable here
            }

            dataset.begin(ReadWrite.WRITE);
            try {
                Model model = dataset.getDefaultModel();
                if (!indexIsCreated) {
                    log.debug("Indexing... " + name);
                    model.removeAll();
                    model.add(dataset_model);
                }else{
                    model.setNsPrefixes(dataset_model.getNsPrefixMap());
                }
                dataset.commit();
            } finally {
                dataset.end();
            }

            datasets.put(name, dataset);
        }
        return dataset;
    }

    public void removeDataset(String name) throws IOException, Exception {
        if (datasets.containsKey(name)) {
            //Dataset d = datasets.get(name);
            Dataset d = datasets.remove(name);
            TDBFactory.release(d);
            //d.close();
            File nameFile = new File(directory + name);
            if (nameFile.exists()) {
                log.debug("Deleting: " + nameFile.getAbsolutePath());
                Utils.deleteDirectory(nameFile);
            }
        }
    }

    /**
     * Perform a SPARQL SELECT query with inference to TDB.
     *
     * @param database
     * @param query the SPARQL query (no prefixes).
     * @param inf
     * @param rules
     * @param format
     * @return
     * @throws java.lang.Exception
     */
    public String select(String database, String query, Boolean inf, String rules, String format) throws Exception {
        String response = "";
        //System.out.println(rules);
        // initiate prefixes only if rules are used
        Map<String, String> prefixes = new HashMap<>();
        if (!rules.isEmpty()) {
            prefixes = getNsPrefixMap(database);
        }

        Dataset dataset = getDataset(database);
        dataset.begin(ReadWrite.READ);
        try {
            Model model = dataset.getDefaultModel();
            QueryExecution qe;
            // test if inference are used
            if (inf != null && inf) {
                InfModel inference;
                // if rules are not used use only RDFS inference 
                if (rules.isEmpty()) {
                    inference = ModelFactory.createRDFSModel(model);
                } else {
                    PrintUtil.registerPrefixMap(prefixes);
                    Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
                    inference = ModelFactory.createInfModel(reasoner, model);
                }
                qe = QueryExecutionFactory.create(query, inference);
            } else {
                qe = QueryExecutionFactory.create(query, dataset);
            }
            response = execute(qe, format);
        } finally {
            dataset.end();
        }
        return response;
    }

    /**
     * DESCRIBES a resource in the TDB.
     *
     * @param database
     * @param prefix
     * @param resource
     * @param format
     * @return
     */
    public String describeResource(String database, String prefix, String resource, String format) {
        Model describedModel = ModelFactory.createDefaultModel();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Dataset dataset = getDataset(database);
        dataset.begin(ReadWrite.READ);

        try {
            Model model = dataset.getDefaultModel();
            String namespace = model.getNsPrefixMap().get(prefix);
            Resource res = model.getResource(namespace + resource);
            StmtIterator stat = model.listStatements(res, null, (RDFNode) null);
            describedModel.add(stat);
            describedModel.setNsPrefixes(model.getNsPrefixMap());
            switch (format) {
                case "json":
                case "js":
                    RDFDataMgr.write(os, describedModel, RDFFormat.RDFJSON);
                    break;
                case "rdf":
                    RDFDataMgr.write(os, describedModel, RDFFormat.RDFXML);
                    break;
                case "ld":
                    RDFDataMgr.write(os, describedModel, RDFFormat.JSONLD);
                    break;
                case "ttl":
                    RDFDataMgr.write(os, describedModel, RDFFormat.TTL);
                    break;
                default:
                    RDFDataMgr.write(os, describedModel, RDFFormat.RDFXML);
            }
        } finally {
            dataset.end();
        }
        return os.toString();
    }

    /**
     * Executes SPARQL queries.
     *
     * @param qe Jena QueryExecution object.
     * @param format expected return format.
     * @return
     */
    private String execute(QueryExecution qe, String format) throws Exception {
        String response = "";
        ResultSet rs = qe.execSelect();
        switch (format) {
            case "txt":
            case "text":
                response = ResultSetFormatter.asText(rs);
                break;
            case "json":
            case "js": {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ResultSetFormatter.outputAsJSON(os, rs);
                response = os.toString();
                break;
            }
            case "xml":
                response = ResultSetFormatter.asXMLString(rs);
                break;
            case "rdf": {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ResultSetFormatter.output(os, rs, ResultsFormat.FMT_RDF_XML);
                response = os.toString();
                break;
            }
            case "ttl": {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ResultSetFormatter.output(os, rs, ResultsFormat.FMT_RDF_TTL);
                response = os.toString();
                break;
            }
            case "csv": {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ResultSetFormatter.outputAsCSV(os, rs);
                response = os.toString();
                break;
            }
            default: {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ResultSetFormatter.output(os, rs, ResultsFormat.FMT_RDF_XML);
                response = os.toString();
                break;
            }
        }
        return response;
    }

    public void read(String database, String input) {
        Dataset dataset = getDataset(database);
        dataset.begin(ReadWrite.WRITE);
        try {
            Model model = dataset.getDefaultModel();
            model.read(input);
            dataset.commit();
            //model.close();
        } finally {
            dataset.end();
        }
    }

    public void read(String database, InputStream input, Lang lang) throws Exception {
        Dataset dataset = getDataset(database);
        dataset.begin(ReadWrite.WRITE);
        try {
            RDFDataMgr.read(dataset, input, lang);
            dataset.commit();
            //model.close();
        } finally {
            dataset.end();
        }
    }

    /**
     * Removes the given triple statement in the database.
     *
     * @param database
     * @param triple
     */
    public void removeStatement(String database, NTriple triple) {
        Dataset dataset = getDataset(database);
        dataset.begin(ReadWrite.WRITE);
        try {
            Model model = dataset.getDefaultModel();

            Resource s = model.createResource(triple.getS());
            Property p = model.createProperty(triple.getP());

            UrlValidator urlValidator = new UrlValidator();
            if (urlValidator.isValid(triple.getO())) {
                Resource o = model.createResource(triple.getO());
                Statement stat = model.createStatement(s, p, o);
                model.remove(stat);
            } else {
                Statement stat = model.createLiteralStatement(s, p, triple.getO());
                if (model.contains(stat)) {
                    model.remove(stat);
                }
            }

            dataset.commit();
            //model.close();
        } finally {
            dataset.end();
        }
    }

    /**
     * Adds the given quad statement to the database.
     *
     * @param database
     * @param quad
     * @return success of the operation.
     */
    public boolean addStatement(String database, NQuad quad) {
        Dataset dataset = getDataset(database);
        dataset.begin(ReadWrite.WRITE);

        try {

            DatasetGraph ds = dataset.asDatasetGraph();
            Node c = NodeFactory.createURI(quad.getC());
            Node s = NodeFactory.createURI(quad.getS());
            Node p = NodeFactory.createURI(quad.getP());

            UrlValidator urlValidator = new UrlValidator();
            if (urlValidator.isValid(quad.getO())) {
                Node o = NodeFactory.createURI(quad.getO());
                ds.add(c, s, p, o);
            } else {
                Node o = NodeFactory.createLiteral(quad.getO());
                ds.add(c, s, p, o);
            }

            dataset.commit();
            //model.close();
        } catch (Exception e) {
            log.error("Add statement failed", e);
        } finally {
            dataset.end();
        }
        return true;
    }

    /**
     * Adds the given triple statement to the database.
     *
     * @param database
     * @param triple
     * @return success of the operation.
     */
    public boolean addStatement(String database, NTriple triple) {
        Dataset dataset = getDataset(database);
        dataset.begin(ReadWrite.WRITE);

        try {
            Model model = dataset.getDefaultModel();
            Resource s = model.createResource(triple.getS());
            Property p = model.createProperty(triple.getP());

            UrlValidator urlValidator = new UrlValidator();
            if (urlValidator.isValid(triple.getO())) {
                Resource o = model.createResource(triple.getO());
                model.add(s, p, o);
            } else {
                model.add(s, p, triple.getO());
            }

            dataset.commit();
            //model.close();
        } catch (Exception e) {
            log.error("Add statement failed", e);
        } finally {
            dataset.end();
        }
        return true;
    }

    public String getRDF(String database) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Dataset dataset = getDataset(database);
        dataset.begin(ReadWrite.READ);
        try {
            if (dataset.getDefaultModel().size() < 2000) {
                RDFDataMgr.write(out, dataset.getDefaultModel(), Lang.TTL);
            } else {
                return "Data is too long to show!";
            }
        } finally {
            dataset.end();
        }
        return out.toString();
    }

    public void storeData(String database, String data) throws Exception {
        Model m = ModelFactory.createDefaultModel();
        Dataset dataset = getDataset(database);
        dataset.begin(ReadWrite.WRITE);
        try {
            InputStream is = new ByteArrayInputStream(data.getBytes());
            m.read(is, null, "TTL");
            Model model = dataset.getDefaultModel();
            model.removeAll();
            model.add(m);
            dataset.commit();
        } finally {
            dataset.end();
        }
    }

    public Set<String> getProperties(String database) {
        Set<String> set = new HashSet<>();
        Dataset dataset = getDataset(database);
        dataset.begin(ReadWrite.READ);
        try {
            //auxiliar model
            OntModel auxModel = ModelFactory.createOntologyModel();
            auxModel.add(dataset.getDefaultModel());
            ExtendedIterator<OntProperty> op = auxModel.listOntProperties();

            while (op.hasNext()) {
                OntProperty prop = op.next();
                //if (prop.toString().startsWith(location)) {
                set.add(prop.toString());
                //}
            }
        } finally {
            dataset.end();
        }
        return set;
    }

    public Set<String> getResources(String database) {
        Set<String> set = new HashSet<>();
        Dataset dataset = getDataset(database);
        dataset.begin(ReadWrite.READ);
        try {
            //auxiliar model
            Model model = dataset.getDefaultModel();
            ResIterator it = model.listResourcesWithProperty(null);
            for (Iterator iterator = it; it.hasNext();) {
                Object next = iterator.next();
                set.add(next.toString());
            }
        } finally {
            dataset.end();
        }
        return set;
    }

    public void storeData(String database, InputStream uploadedInputStream, String fileName) throws Exception {

        String format = FilenameUtils.getExtension(fileName);
        switch (format) {
            case "ttl":
                read(database, uploadedInputStream, Lang.TTL);
                break;
            case "rdf":
            case "owl":
                read(database, uploadedInputStream, Lang.RDFXML);
                break;
            case "nt":
                read(database, uploadedInputStream, Lang.NT);
                break;
            case "jsonld":
                read(database, uploadedInputStream, Lang.JSONLD);
                break;
            case "rj":
                read(database, uploadedInputStream, Lang.RDFJSON);
                break;
            case "n3":
                read(database, uploadedInputStream, Lang.N3);
                break;
            case "trig":
                read(database, uploadedInputStream, Lang.TRIG);
                break;
            case "trix":
                read(database, uploadedInputStream, Lang.TRIX);
                break;
            case "trdf":
            case "rt":
                read(database, uploadedInputStream, Lang.RDFTHRIFT);
                break;
            default:
                break;
        }

    }

}
