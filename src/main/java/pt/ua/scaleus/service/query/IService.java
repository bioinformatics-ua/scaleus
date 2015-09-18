/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.scaleus.service.query;

import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import pt.ua.scaleus.service.data.Namespace;
import pt.ua.scaleus.service.data.Triple;

/**
 *
 * @author Pedro Sernadela <sernadela at ua.pt>
 */
public interface IService {

    public abstract Response sparqler(String dataset, String query);

    public abstract Response addDataset(String name);

    public abstract Response removeDataset(String name);

    public abstract Response listDataset(String name);

    public abstract Response getNamespaces(String database);

    public abstract Response putNamespace(String database, Namespace namespace);

    public abstract Response removeNamespace(String database, String prefix);

    public abstract Response storeTriple(String database, Triple triple);
    
    public abstract Response removeTriple(String database, Triple triple);
}
