/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.scaleus.api;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.log4j.Logger;

/**
 *
 * @author Pedro Sernadela <sernadela at ua.pt>
 */
public class Init {

    static API api = null;
    private static final Logger log = Logger.getLogger(Init.class);

    public static API getAPI() {
        if (api == null) {
            api = new API();
        }
        return api;
    }

    public static void dataImport(String database, String location) {
        try {
            File file = new File(location);
            if (file.isDirectory()) {
                String[] list = Utils.getFolderContentList(file.getAbsolutePath());
                for (String l : list) {
                    File f= new File(location + "/" + l);
                    log.debug("Importing: " + f.getAbsolutePath());
                    Path input = Paths.get(f.getAbsolutePath());
                    getAPI().read(database, input.toUri().toString());
                }
            } else {
                log.debug("Importing: " + file.getAbsolutePath());
                getAPI().read(database, file.getAbsolutePath());
            }
        } catch (Exception e) {
            log.error("Data import failed", e);
        }

    }
}
