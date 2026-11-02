package com.example;
import com.example.models.*;
import com.example.services.CompetitorService;
import java.util.*;
import javax.persistence.*;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import org.junit.*;
import static org.junit.Assert.*;

public class LaboratorioTest {
    @BeforeClass public static void database() {
        System.setProperty("javax.persistence.jdbc.driver","org.apache.derby.jdbc.EmbeddedDriver");
        System.setProperty("javax.persistence.jdbc.url","jdbc:derby:memory:lab6tests;create=true");
        PersistenceManager.getInstance().getEntityManagerFactory();
    }
    @AfterClass public static void shutdown() { PersistenceManager.getInstance().closeEntityManagerFactory(); }
    private CompetitorDTO input(String email,String surname) {
        CompetitorDTO c=new CompetitorDTO(); c.setName("Laura"); c.setSurname(surname); c.setAge(20);
        c.setAddress(email); c.setPassword("clave123"); c.setCity("Bogota"); c.setCountry("Colombia");return c;
    }
    private LoginDTO credentials(String email,String password) { LoginDTO l=new LoginDTO();l.setAddress(email);l.setPassword(password);return l; }
    @Test public void registrationLoginAndRejectedCredentials() {
        CompetitorService service=new CompetitorService();service.init();
        try {
            Response added=service.createCompetitor(input("laura@example.com","Gomez"));assertEquals(200,added.getStatus());
            assertNotNull(((Map<?,?>)added.getEntity()).get("competitor_id"));
            Competitor c=(Competitor)service.login(credentials(" LAURA@example.com ","clave123")).getEntity();
            assertNotNull(c.getId());assertNotNull(c.getCreatedAt());assertNotEquals("clave123",c.getPassword());assertTrue(Passwords.matches("clave123",c.getPassword()));
            String json=new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(c);
            assertFalse(json.contains("password"));assertFalse(json.contains("clave123"));
            for(LoginDTO login:Arrays.asList(credentials("laura@example.com","incorrecta"),credentials("nadie@example.com","clave123"),credentials("' OR 1=1 --","clave123"),credentials("laura@example.com",null))) {
                try {service.login(login);fail("Debe lanzar NotAuthorizedException");}catch(NotAuthorizedException ex){assertEquals(401,ex.getResponse().getStatus());}
            }
            try{service.createCompetitor(input("LAURA@example.com","Duplicado"));fail();}catch(ClientErrorException ex){assertEquals(409,ex.getResponse().getStatus());}
            service.createCompetitor(input("ana@example.com","Alvarez"));
            List<?> all=(List<?>)service.getAll().getEntity();assertEquals("Alvarez",((Competitor)all.get(0)).getSurname());
        } catch(java.io.IOException e) {throw new AssertionError(e);} finally {service.close();}
        CompetitorService next=new CompetitorService();next.init();
        try{assertEquals(200,next.login(credentials("laura@example.com","clave123")).getStatus());}finally{next.close();}
    }
    @Test public void relationshipForeignKeyCascadeAndTimestamps() {
        EntityManager em=PersistenceManager.getInstance().getEntityManagerFactory().createEntityManager();
        try {
            Competitor c=new Competitor("Didier","Perilla",20,"1","2","didier@example.com","Bogota","Colombia",false);c.setPassword(Passwords.hash("prueba"));
            Producto product=new Producto("Premio",15000.0,"Producto del concurso");c.addProduct(product);
            em.getTransaction().begin();em.persist(c);em.getTransaction().commit();
            Long cid=c.getId(),pid=product.getId();Calendar created=(Calendar)c.getCreatedAt().clone(),previous=(Calendar)c.getUpdatedAt().clone();
            em.clear();Producto stored=em.find(Producto.class,pid);assertEquals(cid,stored.getCompetitor().getId());assertNotNull(stored.getCreatedAt());
            Number fk=(Number)em.createNativeQuery("select COMPETITOR_ID from PRODUCTO where ID="+pid).getSingleResult();assertEquals(cid.longValue(),fk.longValue());
            c=em.find(Competitor.class,cid);assertEquals(1,c.getProducts().size());
            try{Thread.sleep(20);}catch(InterruptedException e){Thread.currentThread().interrupt();}
            em.getTransaction().begin();c.setCity("Medellin");em.getTransaction().commit();
            assertEquals(created,c.getCreatedAt());assertTrue(c.getUpdatedAt().after(previous));
        }finally{if(em.getTransaction().isActive())em.getTransaction().rollback();em.close();}
    }
}
