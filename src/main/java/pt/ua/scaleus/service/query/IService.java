/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.scaleus.service.query;

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
import pt.ua.scaleus.service.data.Namespace;
import pt.ua.scaleus.service.data.Triple;

/**
 *
 * @author Pedro Sernadela <sernadela at ua.pt>
 */
public interface IService {

    @GET
    @Path("/sparqler/{dataset}/sparql")
    public abstract Response sparqler(@PathParam("dataset") String dataset, @QueryParam("query") String query);

    @GET
    @Path("/resource/{database}/{prefix}/{id}/{format}")
    public abstract Response resource(@PathParam("database") String database, @PathParam("prefix") String prefix, @PathParam("id") String id, @PathParam("format") String format);

    @POST
    @Path("/dataset/{name}")
    public abstract Response addDataset(@PathParam("name") String name);

    @DELETE
    @Path("/dataset/{name}")
    public abstract Response removeDataset(@PathParam("name") String name);

    @GET
    @Path("/dataset")
    @Produces(MediaType.APPLICATION_JSON)
    public abstract Response listDataset(@PathParam("name") String name);

    @GET
    @Path("/namespaces/{database}")
    @Produces(MediaType.APPLICATION_JSON)
    public abstract Response getNamespaces(@PathParam("database") String database);

    @POST
    @Path("/namespace/{database}")
    @Consumes(MediaType.APPLICATION_JSON)
    public abstract Response putNamespace(@PathParam("database") String database, Namespace namespace);

    @DELETE
    @Path("/namespace/{database}/{prefix}")
    public abstract Response removeNamespace(@PathParam("database") String database, @PathParam("prefix") String prefix);

    @POST
    @Path("/store/{database}")
    @Consumes(MediaType.APPLICATION_JSON)
    public abstract Response store(@PathParam("database") String database, Triple triple);
}
