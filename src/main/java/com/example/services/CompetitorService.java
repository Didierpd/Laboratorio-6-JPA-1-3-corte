package com.example.services;
import com.example.PersistenceManager;
import com.example.Passwords;
import com.example.models.*;
import java.util.*;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.persistence.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Path("/competitors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CompetitorService {
    @PersistenceContext(unitName="CompetitorsPU")
    private EntityManager entityManager;
    @PostConstruct public void init() {
        entityManager=PersistenceManager.getInstance().getEntityManagerFactory().createEntityManager();
    }
    @PreDestroy public void close() {
        if (entityManager != null && entityManager.isOpen()) {
            if (entityManager.getTransaction().isActive()) entityManager.getTransaction().rollback();
            entityManager.close();
        }
    }
    @GET public Response list() { return getAll(); }
    @GET @Path("/get") public Response getAll() {
        List<Competitor> result=entityManager.createQuery("select u from Competitor u order by u.surname ASC",Competitor.class).getResultList();
        for (Competitor competitor : result) competitor.getProducts().size();
        return Response.ok(result).build();
    }
    @POST @Path("/add") public Response createCompetitor(CompetitorDTO input) {
        if (input==null || blank(input.getName()) || blank(input.getSurname()) || blank(input.getAddress()) || !input.getAddress().contains("@") || blank(input.getPassword()) || input.getAge()<0)
            throw new BadRequestException("Nombre, apellido, correo y contraseña son obligatorios; la edad no puede ser negativa.");
        String email=normalize(input.getAddress());
        if (!entityManager.createQuery("select c from Competitor c where c.address=:email",Competitor.class).setParameter("email",email).getResultList().isEmpty())
            throw new ClientErrorException("El correo ya está registrado.",409);
        Competitor competitor=new Competitor(input.getName(),input.getSurname(),input.getAge(),input.getTelephone(),input.getCellphone(),email,input.getCity(),input.getCountry(),false);
        competitor.setPassword(Passwords.hash(input.getPassword()));
        EntityTransaction tx=entityManager.getTransaction();
        try { tx.begin(); entityManager.persist(competitor); tx.commit(); entityManager.refresh(competitor); }
        catch (RuntimeException e) { if(tx.isActive()) tx.rollback(); throw e; }
        return Response.ok(Collections.singletonMap("competitor_id",competitor.getId())).build();
    }
    @POST @Path("/login") public Response login(LoginDTO input) {
        if(input==null || blank(input.getAddress()) || blank(input.getPassword())) throw unauthorized();
        List<Competitor> result=entityManager.createQuery("select c from Competitor c where c.address=:email",Competitor.class).setParameter("email",normalize(input.getAddress())).getResultList();
        if(result.isEmpty() || !Passwords.matches(input.getPassword(),result.get(0).getPassword())) throw unauthorized();
        Competitor competitor=result.get(0); competitor.getProducts().size();
        return Response.ok(competitor).build();
    }
    private NotAuthorizedException unauthorized() {
        return new NotAuthorizedException(Response.status(401).header("WWW-Authenticate","Login realm=\"Competitors\"").entity(Collections.singletonMap("error","Correo o contraseña incorrectos.")).build());
    }
    private static boolean blank(String s) { return s==null || s.trim().isEmpty(); }
    private static String normalize(String s) { return s.trim().toLowerCase(Locale.ROOT); }
}
