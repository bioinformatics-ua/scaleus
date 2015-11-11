/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.scaleus.service;

import java.io.InputStream;

import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import pt.ua.scaleus.service.data.NTriple;
import pt.ua.scaleus.service.data.Namespace;

/**
 *
 * @author Pedro Sernadela <sernadela at ua.pt>
 */
public interface IService {

    public abstract Response sparqler(String dataset, String query, Boolean inf, String rules, String format);

    public abstract Response addDataset(String name);

    public abstract Response removeDataset(String name);

    public abstract Response listDataset();

    public abstract Response getNamespaces(String database);

    public abstract Response putNamespace(String database, Namespace namespace);

    public abstract Response removeNamespace(String database, String prefix);

    public abstract Response storeTriple(String database, NTriple triple);
    
    public abstract Response removeTriple(String database, NTriple triple);
    
    public abstract Response getData(String database);
    
    public abstract Response storeData(String database, String data);

    public abstract Response resource(String database, String prefix, String id, String format);
    
    public abstract Response getProperties(String database, String match);
    
    public abstract Response getResources(String database, String match);

    public abstract Response uploadFile(String database, InputStream uploadedInputStream, FormDataContentDisposition fileDetail);

}
