/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.scaleus.service.data;

/**
 *
 * @author Pedro Sernadela <sernadela at ua.pt>
 */
public class Namespace {
    
    String prefix;
    String namespace;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    
    @Override
    public String toString() {
        return "Namespace [" + prefix + " " + namespace + "]";
    }
    
}
