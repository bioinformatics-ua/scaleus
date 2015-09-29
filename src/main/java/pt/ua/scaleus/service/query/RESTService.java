/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.scaleus.service.query;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import pt.ua.scaleus.api.API;
import pt.ua.scaleus.api.Init;
import pt.ua.scaleus.service.data.NTriple;
import pt.ua.scaleus.service.data.Namespace;

/**
 *
 * @author Pedro Sernadela <sernadela at ua.pt>
 */
@Path("/v1")
public class RESTService implements IService {

    API api = Init.getAPI();

    @GET
    @Path("/sparqler/{dataset}/sparql")
    @Produces(MediaType.TEXT_PLAIN)
    @Override
    public Response sparqler(@PathParam("dataset") String dataset, @QueryParam("query") String query, @DefaultValue("false") @QueryParam("inference") Boolean inf, @DefaultValue("json") @QueryParam("format") String format) {
        String resp = api.select(dataset, query, inf, format);
        return Response.status(200).entity(resp).build();
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
            Map<String, String> namespaces = api.getNsPrefixMap(database);
            JSONArray ja = new JSONArray();
            for (Map.Entry<String, String> entry : namespaces.entrySet()) {
                JSONObject mapJo = new JSONObject();
                mapJo.put("prefix", entry.getKey());
                mapJo.put("uri", entry.getValue());
                ja.add(mapJo);
            }
            json = new JSONObject();
            json.put("namespaces", ja);
        } catch (Exception ex) {
            Logger.getLogger(RESTService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.status(200).entity(json.toJSONString()).build();
    }

    @POST
    @Path("/namespaces/{database}")
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
    @Path("/namespaces/{database}/{prefix}")
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
    public Response storeTriple(@PathParam("database") String database, NTriple triple) {
        System.out.println(triple);
        System.out.println(database);
        try {
            api.addStatement(database, triple);
        } catch (Exception ex) {
            Logger.getLogger(RESTService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.status(200).build();
    }

    @DELETE
    @Path("/store/{database}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Override
    public Response removeTriple(@PathParam("database") String database, NTriple triple) {
        System.out.println(triple);
        try {
            api.removeStatement(database, triple);
        } catch (Exception ex) {
            Logger.getLogger(RESTService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.status(200).build();
    }

    @GET
    @Path("/data/{database}")
    @Produces(MediaType.TEXT_PLAIN)
    @Override
    public Response getData(@PathParam("database") String database) {
        String response = "";
        try {
            response = api.getRDF(database);
        } catch (Exception ex) {
            Logger.getLogger(RESTService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.status(200).entity(response).build();
    }

    @POST
    @Path("/data/{database}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Override
    public Response storeData(@PathParam("database") String database, @FormParam("data") String data) {
        try {
            api.storeData(database, data);
            //System.out.println(data);
        } catch (Exception ex) {
            Logger.getLogger(RESTService.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("failed");
            return Response.serverError().build();
        }
        return Response.status(200).build();
    }
    
    @GET
    @Path("/resource/{database}/{prefix}/{id}/{format}")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public Response resource(@PathParam("database") String database, @PathParam("prefix") String prefix, @PathParam("id") String id, @PathParam("format") String format) {
    	String response="";
        try {
        	response = api.describeResource(database, prefix, id, format);
        } catch (Exception ex) {
            Logger.getLogger(RESTService.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return Response.status(200).entity(response).build();
    }

    @GET
    @Path("/properties/{database}/{match}")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public Response getProperties(@PathParam("database") String database, @PathParam("match") String match) {
        JSONArray ja = new JSONArray();
        try {
            Set<String> prop = api.getProperties(database);
            for (String prop1 : prop) {
                if (prop1.contains(match)) {
                    ja.add(prop1);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(RESTService.class.getName()).log(Level.SEVERE, null, ex);
            return Response.serverError().build();
        }
        return Response.status(200).entity(ja.toJSONString()).build();
    }

    @GET
    @Path("/resources/{database}/{match}")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public Response getResources(@PathParam("database") String database, @PathParam("match") String match) {
        JSONArray ja = new JSONArray();
        try {
            Set<String> prop = api.getResources(database);
            for (String prop1 : prop) {
                if (prop1.contains(match)) {
                    ja.add(prop1);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(RESTService.class.getName()).log(Level.SEVERE, null, ex);
            return Response.serverError().build();
        }
        return Response.status(200).entity(ja.toJSONString()).build();
    }

}
