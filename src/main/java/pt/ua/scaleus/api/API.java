/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.scaleus.api;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.tdb.TDBFactory;
import org.apache.log4j.Logger;


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
        if(!mainDir.exists()) mainDir.mkdir();
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
            model.close();
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
            model.close();
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
            model.close();
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
     * @return
     */
    public String select(String database, String query) {
        String response = "";
        Dataset dataset = getDataset(database);
        dataset.begin(ReadWrite.READ);
        try {
            Model model = dataset.getDefaultModel();
            InfModel inf = ModelFactory.createRDFSModel(model);
            QueryExecution qe = QueryExecutionFactory.create(query, model);
            ResultSet rs = qe.execSelect();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ResultSetFormatter.outputAsJSON(os, rs);
            response = os.toString();
            qe.close();
            model.close();
            inf.close();
        } finally {
            dataset.end();
        }
        return response;
    }

    /**
     * Perform a SPARQL DESCRIBE query to TDB.
     *
     * @param database
     * @param resource
     * @return
     */
    public Model describe(String database, String resource) {
        String queryString = getSparqlPrefixes(database) + "DESCRIBE " + resource;
        Model describedModel = ModelFactory.createDefaultModel();
        Dataset dataset = getDataset(database);
        dataset.begin(ReadWrite.READ);

        try {
            Model model = dataset.getDefaultModel();
            Query query = QueryFactory.create(queryString);
            System.out.println(query.isDescribeType());
            QueryExecution qe = QueryExecutionFactory.create(query, model);
            System.out.println(query);
            describedModel = qe.execDescribe();
            qe.close();
            //model.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            dataset.end();
        }

        return describedModel;
    }

    public void read(String database, String input) {
        Dataset dataset = getDataset(database);
        dataset.begin(ReadWrite.WRITE);
        try {
            Model model = dataset.getDefaultModel();
            model.read(input);
            dataset.commit();
            model.close();
        } finally {
            dataset.end();
        }
    }

    /**
     * Adds the given triple statement to given database.
     *
     * @param database
     * @param subject a Resource for to the statement subject.
     * @param predicate a Property for the statement predicate.
     * @param object a Resource for the statement object.
     * @return success of the operation.
     */
    public boolean addStatement(String database, Resource subject, Property predicate, Resource object) {
        Dataset dataset = getDataset(database);
        dataset.begin(ReadWrite.WRITE);
        try {
            Model model = dataset.getDefaultModel();
            model.add(subject, predicate, object);
            dataset.commit();
            model.close();
        } finally {
            dataset.end();
        }
        return true;
    }

    /**
     * Adds the given triple statement to given database.
     *
     * @param database
     * @param subject a Resource for to the statement subject.
     * @param predicate a Property for the statement predicate.
     * @param object a Resource for the statement object.
     * @return success of the operation.
     */
    public boolean addStatement(String database, Resource subject, Property predicate, String object) {

        Dataset dataset = getDataset(database);
        dataset.begin(ReadWrite.WRITE);
        try {
            Model model = dataset.getDefaultModel();
            model.add(subject, predicate, object);
            dataset.commit();
            model.close();
        } finally {
            dataset.end();
        }
        return true;
    }

    /**
     * Creates a new Resource.
     *
     * @param database
     * @param uri the URI for the new Resource.
     * @return the newly created Resource.
     */
    public Resource createResource(String database, String uri) {
        Dataset dataset = getDataset(database);
        Model model = dataset.getDefaultModel();
        Resource resource = model.createResource(uri);
        model.close();

        return resource;
    }

    /**
     * Creates a new Resource.
     *
     * @param database
     * @param uri the URI for the new Resource.
     * @return the newly created Resource.
     */
    public Property createProperty(String database, String uri) {

        Dataset dataset = getDataset(database);
        Model model = dataset.getDefaultModel();
        Property property = model.createProperty(uri);
        model.close();

        return property;
    }
}
