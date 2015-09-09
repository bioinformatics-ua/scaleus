/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.scaleus;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import pt.ua.scaleus.service.query.RESTService;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import pt.ua.scaleus.api.Init;

/**
 *
 * @author Pedro Sernadela <sernadela at ua.pt>
 */
public class JettyServer {

    static final String WEBAPPDIR_STATIC = "webapp/WEB-INF/static";
    static final String WEBAPPDIR_APP = "webapp/WEB-INF/app";

    public static void main(String[] args) {
        
        int port = 80;
        String database = "default";
        String data_import = "resources/";

        try {
            Options options = new Options();
            options.addOption("p", "port", true, "Server port");
            options.addOption("d", "database", true, "Database name");
            options.addOption("i", "import", true, "Folder or file location to import");
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);
            if( cmd.hasOption( "p" ) ) {
                String cmd_port = cmd.getOptionValue( "p" );
                port = Integer.parseInt(cmd_port);
            }
            if( cmd.hasOption( "d" ) && cmd.hasOption( "i" ) ) {
                database = cmd.getOptionValue( "d" );
                data_import = cmd.getOptionValue( "i" );
            }
            
        } catch (ParseException ex) {
            Logger.getLogger(JettyServer.class.getName()).log(Level.SEVERE, null, ex);
        }
   

        Server jettyServer = new Server(port);

        URL warURL = JettyServer.class.getClassLoader().getResource(WEBAPPDIR_STATIC);
        //System.err.println(warURL);
        final String warURLString_statics = warURL.toExternalForm();
        warURL = JettyServer.class.getClassLoader().getResource(WEBAPPDIR_APP);
        final String warURLString_app = warURL.toExternalForm();

        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setResourceBase(warURLString_statics);
        HandlerList handlers = new HandlerList();
//        ServletContextHandler servletContextHandler = new ServletContextHandler(server, ServerConfigurations.getInstance().getBaseurl() + BASEURL, true, true);

        ServletContextHandler appContext = new ServletContextHandler();
        appContext.setContextPath("/app/");
        appContext.setResourceBase(warURLString_app);
        appContext.addServlet(DefaultServlet.class, "/");

        ServletContextHandler servletContextHandler = new ServletContextHandler(jettyServer, "/api/", true, false);
        ServletHolder jerseyServlet = servletContextHandler.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/*");
        jerseyServlet.setInitOrder(0);
        // Tells the Jersey Servlet which REST service/class to load.
        jerseyServlet.setInitParameter("jersey.config.server.provider.classnames", RESTService.class.getCanonicalName());

        Handler[] sh = jettyServer.getHandlers();
        Handler[] h = new Handler[sh.length + 2];
        System.arraycopy(sh, 0, h, 0, sh.length);
        h[h.length - 2] = resource_handler;
        h[h.length - 1] = appContext;
        handlers.setHandlers(h);

        jettyServer.setHandler(handlers);

        try {
            Init.getAPI();
            Init.dataImport(database, data_import);
            jettyServer.start();
            jettyServer.join();
        } catch (Exception e) {
            Logger.getLogger(JettyServer.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            jettyServer.destroy();
        }
    }

}
