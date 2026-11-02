package com.example;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
public class Main {
    public static void main(String[] args) throws Exception {
        String port=System.getenv("PORT");
        final Server server=new Server(port==null || port.isEmpty()?8080:Integer.parseInt(port));
        WebAppContext root=new WebAppContext();
        root.setContextPath("/"); root.setDescriptor("src/main/webapp/WEB-INF/web.xml");
        root.setResourceBase("src/main/webapp/"); root.setParentLoaderPriority(true);
        PersistenceManager.getInstance().getEntityManagerFactory();
        server.setHandler(root);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { server.stop(); } catch(Exception e) { e.printStackTrace(); }
            PersistenceManager.getInstance().closeEntityManagerFactory();
        }));
        try { server.start(); server.join(); }
        finally { PersistenceManager.getInstance().closeEntityManagerFactory(); }
    }
}
