package com.example.models;
import java.io.Serializable;
import java.util.Calendar;
import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Producto implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    @Column(name="create_at", nullable=false, updatable=false) @Temporal(TemporalType.TIMESTAMP)
    private Calendar createdAt;
    @Column(name="updated_at", nullable=false) @Temporal(TemporalType.TIMESTAMP)
    private Calendar updatedAt;
    private String name;
    private Double price;
    private String description;
    @ManyToOne(cascade=CascadeType.ALL)
    @JoinColumn(name="COMPETITOR_ID")
    private Competitor competitor;
    public Producto() { }
    public Producto(String name, Double price, String description) { this.name=name; this.price=price; this.description=description; }
    @PrePersist private void creationTimestamp() { createdAt=Calendar.getInstance(); updatedAt=(Calendar) createdAt.clone(); }
    @PreUpdate private void updateTimestamp() { updatedAt=Calendar.getInstance(); }
    public Long getId() { return id; }
    public void setId(Long id) { this.id=id; }
    public Calendar getCreatedAt() { return createdAt; }
    public Calendar getUpdatedAt() { return updatedAt; }
    public String getName() { return name; }
    public void setName(String name) { this.name=name; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price=price; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description=description; }
    @JsonIgnore public Competitor getCompetitor() { return competitor; }
    public void setCompetitor(Competitor competitor) { this.competitor=competitor; }
}
