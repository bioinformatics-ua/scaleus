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
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.resultset.ResultsFormat;
import org.apache.jena.tdb.TDBFactory;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import pt.ua.scaleus.service.data.NQuad;
import pt.ua.scaleus.service.data.NTriple;

/**
 *
 * @author Pedro Sernadela <sernadela at ua.pt>
 */
public class API {

    private static final Logger logger = Logger.getLogger(API.class.getName());
    //String input = "resources/data/output.rdf";
    String directory = "datasets/";
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
            dataset = TDBFactory.createDataset(directory + name);
            datasets.put(name, dataset);
        }
        return dataset;
    }

    public void removeDataset(String name) throws IOException {
        if (datasets.containsKey(name)) {
            Dataset d = datasets.get(name);
            datasets.remove(name, d);
            TDBFactory.release(d);
            File nameFile = new File(directory + name);
            if (nameFile.exists()) {
                logger.debug("Deleting: " + nameFile.getAbsolutePath());
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
     * @return
     */
    public String select(String database, String query, Boolean inf, String format) {
        String response = "";
        Dataset dataset = getDataset(database);
        dataset.begin(ReadWrite.READ);
        try {
            Model model = dataset.getDefaultModel();
            QueryExecution qe;
            if (inf != null && inf) {
                InfModel inference = ModelFactory.createRDFSModel(model);
                qe = QueryExecutionFactory.create(query, inference);
            } else {
                qe = QueryExecutionFactory.create(query, model);
            }
            response = execute(qe, format);
            //ResultSet rs = qe.execSelect();
            //ByteArrayOutputStream os = new ByteArrayOutputStream();
            //ResultSetFormatter.outputAsJSON(os, rs);
            //response = os.toString();
            //qe.close();
            //model.close();
            //inf.close();
        } catch (Exception e) {
            e.printStackTrace();
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
     * @param id
     * @return
     */
    public String describeResource(String database, String prefix, String id) {
        String response = "";
        Dataset dataset = getDataset(database);
        dataset.begin(ReadWrite.READ);
        try {
            Model model = dataset.getDefaultModel();
            String namespace = model.getNsPrefixMap().get(prefix);
            Resource resource = model.getResource(namespace + id);
            StmtIterator stat = model.listStatements(resource, null, (RDFNode) null);

            while (stat.hasNext()) {
                Statement next = stat.next();
                if (next.getObject().isResource()) {
                    response += "<h2><a href='../resource/" + database + "/" + prefix + "/" + next.getSubject().getLocalName() + "'>" + next.getSubject().toString() + "</a> " + next.getPredicate().toString() + " <a href='/resource/" + database + "/" + prefix + "/" + next.getObject().asResource().getLocalName() + "'>" + next.getObject().toString() + "</a></h2>";
                } else {
                    response += "<h2><a href='../resource/" + database + "/" + prefix + "/" + next.getSubject().getLocalName() + "'>" + next.getSubject().toString() + "</a> " + next.getPredicate().toString() + " " + next.getObject().toString() + "</a></h2>";
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dataset.end();
        }

        return response;
    }

    /**
     * Executes SPARQL queries.
     *
     * @param qe Jena QueryExecution object.
     * @param format expected return format.
     * @return
     */
    private String execute(QueryExecution qe, String format) {
        String response = "";
        try {
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
                    ResultSetFormatter.outputAsJSON(os, rs);
                    response = os.toString();
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
        } finally {
            dataset.end();
        }
        return true;
    }

    public String getRDF(String database) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Dataset dataset = getDataset(database);
        dataset.begin(ReadWrite.READ);
        try {
            if (dataset.getDefaultModel().size() < 2000) {
                RDFDataMgr.write(out, dataset.getDefaultModel(), Lang.TTL);
            } else {
                return "Data is too long to show!";
            }
        } catch (Exception e) {
            e.printStackTrace();
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

}
