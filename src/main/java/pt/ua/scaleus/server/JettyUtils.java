package pt.ua.scaleus.server;

import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServlet;
import java.util.EnumSet;
import java.util.Map;

/**
 * Created by eriksson on 13/10/15.
 */
public class JettyUtils {
    public static class HandlerMapping{
        private final ContextHandler handler;
        private final ServletHolder holder;

        public HandlerMapping(ContextHandler handler, ServletHolder holder) {
            this.handler = handler;
            this.holder = holder;
        }

        public ContextHandler getHandler() {
            return handler;
        }

        public ServletHolder getHolder() {
            return holder;
        }
    }
    public static HandlerMapping createServletHandler(HttpServlet servlet, String basePath, String path){
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS); // servlet with session support enabled
        handler.setContextPath(basePath);
        ServletHolder holder = new ServletHolder(servlet);
        handler.addServlet(holder, path);
        return new HandlerMapping(handler, holder);
    }

    public static HandlerMapping createServletHandler(Class<? extends HttpServlet> servlet, String basePath, String path){
        return createServletHandler(servlet, basePath,path,null);
    }

    public static HandlerMapping createServletHandler(Class<? extends HttpServlet> servlet, String basePath, String path, Map<String, String> init){
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS); // servlet with session support enabled
        handler.setContextPath(basePath);
        final ServletHolder holder = handler.addServlet(servlet, path);
        if(init!=null && !init.isEmpty()){
            for(Map.Entry<String, String> entry : init.entrySet()){
                holder.setInitParameter(entry.getKey(), entry.getValue());
            }
        }
        return new HandlerMapping(handler, holder);
    }
}
