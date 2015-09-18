/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.scaleus.service.query;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import pt.ua.scaleus.api.Init;

/**
 *
 * @author Pedro Sernadela <sernadela at ua.pt>
 */
@Path("/")
public class ResourceService {
    @GET
    @Path("/{dataset}/{prefix}/{resource}")
    public Response sparqler(@PathParam("dataset") String database, @PathParam("prefix") String prefix,@PathParam("resource") String id) {
        return Response.status(200).entity(Init.getAPI().describeResource(database, prefix, id)).build();
    }
}
