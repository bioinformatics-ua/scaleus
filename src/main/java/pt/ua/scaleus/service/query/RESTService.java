/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.scaleus.service.query;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import pt.ua.scaleus.api.API;
import pt.ua.scaleus.api.Init;
import pt.ua.scaleus.service.data.Namespace;
import pt.ua.scaleus.service.data.Triple;

/**
 *
 * @author Pedro Sernadela <sernadela at ua.pt>
 */
@Path("/v1")
public class RESTService implements IService {

    API api = Init.getAPI();

    @GET
    @Path("/sparqler/{dataset}/sparql")
    @Override
    public Response sparqler(@PathParam("dataset") String dataset, @QueryParam("query") String query) {
        return Response.status(200).entity(api.select(dataset, query)).build();
    }

    @GET
    @Path("/resource/{database}/{prefix}/{id}/{format}")
    @Override
    public Response resource(@PathParam("database") String database, @PathParam("prefix") String prefix, @PathParam("id") String id, @PathParam("format") String format) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Model m = ModelFactory.createDefaultModel();
        try {
            String resource = prefix + ":" + id;
            m = api.describe(database, resource);
            RDFDataMgr.write(os, m, RDFFormat.RDFXML);
        } catch (Exception ex) {
            Logger.getLogger(RESTService.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            m.close();
        }

        return Response.status(200).entity(os.toString()).build();
    }

    @POST
    @Path("/dataset/{name}")
    @Override
    public Response addDataset(@PathParam("name") String name) {
        api.getDataset(name);
        return Response.status(200).build();
    }

    @DELETE
    @Path("/dataset/{name}")
    @Override
    public Response removeDataset(@PathParam("name") String name) {
        try {
            api.removeDataset(name);
        } catch (IOException ex) {
            Logger.getLogger(RESTService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.status(200).build();
    }

    @GET
    @Path("/dataset")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public Response listDataset(@PathParam("name") String name) {
        Set<String> datasets = api.getDatasets().keySet();
        JSONArray json = new JSONArray();
        for (String dataset : datasets) {
            json.add(dataset);
        }
        return Response.status(200).entity(json.toJSONString()).build();
    }

    @GET
    @Path("/namespaces/{database}")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public Response getNamespaces(@PathParam("database") String database) {
        JSONObject json = null;
        try {
            Map namespaces = api.getNsPrefixMap(database);
            json = new JSONObject(namespaces);
        } catch (Exception ex) {
            Logger.getLogger(RESTService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.status(200).entity(json.toJSONString()).build();
    }

    @POST
    @Path("/namespace/{database}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Override
    public Response putNamespace(@PathParam("database") String database, Namespace namespace) {
        try {
            String prefix = namespace.getPrefix();
            String uri = namespace.getNamespace();
            api.setNsPrefix(database, prefix, uri);
        } catch (Exception ex) {
            Logger.getLogger(RESTService.class.getName()).log(Level.SEVERE, null, ex);
        }

        return Response.status(200).build();
    }

    @DELETE
    @Path("/namespace/{database}/{prefix}")
    @Override
    public Response removeNamespace(@PathParam("database") String database, @PathParam("prefix") String prefix) {
        try {
            api.removeNsPrefix(database, prefix);
        } catch (Exception ex) {
            Logger.getLogger(RESTService.class.getName()).log(Level.SEVERE, null, ex);
        }

        return Response.status(200).build();
    }

    @POST
    @Path("/store/{database}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Override
    public Response storeTriple(@PathParam("database") String database, Triple triple) {
        System.out.println(triple);
        try {
            api.addStatement(database, triple);
        } catch (Exception ex) {
            Logger.getLogger(RESTService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.status(200).build();
    }

    @DELETE
    @Path("/remove/{database}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Override
    public Response removeTriple(@PathParam("database") String database, Triple triple) {
        System.out.println(triple);
        try {
            api.removeStatement(database, triple);
        } catch (Exception ex) {
            Logger.getLogger(RESTService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.status(200).build();
    }
}
