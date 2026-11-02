package com.example;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/** Una sola fábrica por aplicación; cada petición utiliza su propio EntityManager. */
public final class PersistenceManager {
    private static final PersistenceManager singleton = new PersistenceManager();
    private EntityManagerFactory emf;
    private PersistenceManager() { }
    public static PersistenceManager getInstance() { return singleton; }
    public synchronized EntityManagerFactory getEntityManagerFactory() {
        if (emf == null || !emf.isOpen()) {
            emf=Persistence.createEntityManagerFactory("CompetitorsPU", System.getProperties());
            System.out.println("Persistence started at " + new java.util.Date());
        }
        return emf;
    }
    public synchronized void closeEntityManagerFactory() {
        if (emf != null) { if (emf.isOpen()) emf.close(); emf=null; }
    }
}
