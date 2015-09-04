/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.scaleus.api;

/**
 *
 * @author Pedro Sernadela <sernadela at ua.pt>
 */
public class Init {

    static API api = null;

    public static API getAPI() {
        if (api == null) {
            api = new API();
        }
        return api;
    }
}
