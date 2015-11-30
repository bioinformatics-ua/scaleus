/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.scaleus.server;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.glassfish.jersey.servlet.ServletContainer;

import pt.ua.scaleus.api.Init;
import pt.ua.scaleus.service.RESTService;

/**
 *
 * @author Pedro Sernadela <sernadela at ua.pt>
 */
public class JettyServer {

    private static final String WEBAPPDIR = "webapp/WEB-INF/";
    private static final Logger log = Logger.getLogger(JettyServer.class);

    public static void main(String[] args) {
        
        int port = 80;
        String baseWebPath = "/scaleus";
        String database = "default";
        String data_import = "resources/";
        boolean hasDataImport = false;

        try {
            Options options = new Options();
            options.addOption("p", "port", true, "Server port");
            options.addOption("d", "database", true, "Database name");
            options.addOption("i", "import", true, "Folder or file location to import");
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("p")) {
                String cmd_port = cmd.getOptionValue("p");
                port = Integer.parseInt(cmd_port);
            }
            if (cmd.hasOption("d") && cmd.hasOption("i")) {
                database = cmd.getOptionValue("d");
                data_import = cmd.getOptionValue("i");
                hasDataImport = true;
            }

        } catch (ParseException ex) {
            log.error("CommandLine parse error", ex);
        }
        
        Server server = new Server(port);

        // setup the web pages/scripts app
        final URL warUrl = JettyServer.class.getClassLoader().getResource(WEBAPPDIR);
        final String warUrlString = warUrl.toExternalForm();
        ServletContextHandler webpages = new ServletContextHandler();		
        webpages.setContextPath(baseWebPath);		
        webpages.setResourceBase(warUrlString);		
        webpages.addServlet(DefaultServlet.class, "/");

        //API init parameters
        Map<String, String> apiInit = new HashMap<>();
        apiInit.put("jersey.config.server.provider.classnames", RESTService.class.getCanonicalName()+";org.glassfish.jersey.media.multipart.MultiPartFeature");
        // for request filtering add also: ;org.glassfish.jersey.filter.LoggingFilter
        //Test init parameters
        //Map<String, String> testInit = new HashMap<>();
        //testInit.put("jersey.config.server.provider.classnames", TestService.class.getCanonicalName());
        
        Handler[] handlers = new Handler[]{
                JettyUtils.createServletHandler(ServletContainer.class, baseWebPath, "/api/*", apiInit).getHandler(), // API
                //JettyUtils.createServletHandler(ServletContainer.class, baseWebPath, "/test/*", testInit).getHandler(), 
                webpages, //Web static content
        };

        // register the handlers on the server
        ContextHandlerCollection contextHandlers = new ContextHandlerCollection();
        contextHandlers.setHandlers(handlers);
        server.setHandler(contextHandlers);
        
        try {
            Init.getAPI().getDataset(database);
            if(hasDataImport) Init.dataImport(database, data_import);
            server.start();
            log.info("Server started.");
            server.join();
        } catch (Exception e) {
            log.error("Server cannot start", e);
        } finally {
            if(server.isStarted())
                server.destroy();
        }
    }

}
