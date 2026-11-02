/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.example.models;
import javax.persistence.*;

/**
 *
 * @author Mauricio
 */
@Entity
@Table(name = "COMPETITOR")
public class Competitor implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name="create_at", nullable=false, updatable=false) @Temporal(TemporalType.TIMESTAMP)
    private java.util.Calendar createdAt;
    @Column(name="updated_at", nullable=false) @Temporal(TemporalType.TIMESTAMP)
    private java.util.Calendar updatedAt;
    @Column(nullable=false, length=256)
    private String password;
    @OneToMany(cascade=CascadeType.ALL, mappedBy="competitor")
    private java.util.Set<Producto> products = new java.util.LinkedHashSet<Producto>();
    @PrePersist private void creationTimestamp() { createdAt = java.util.Calendar.getInstance(); updatedAt = (java.util.Calendar) createdAt.clone(); }
    @PreUpdate private void updateTimestamp() { updatedAt = java.util.Calendar.getInstance(); }
    public Long getId() { return id; }
    public void setId(Long id) { this.id=id; }
    public java.util.Calendar getCreatedAt() { return createdAt; }
    public java.util.Calendar getUpdatedAt() { return updatedAt; }
    @com.fasterxml.jackson.annotation.JsonIgnore
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password=password; }
    public java.util.Set<Producto> getProducts() { return products; }
    public void addProduct(Producto product) { products.add(product); product.setCompetitor(this); }

     
    
    private String name;
    
    private String surname;
    
    private int age;
    
    private String telephone;
    
    private String cellphone;
    
    @Column(nullable=false, unique=true)
    private String address;
    
    private String city;
    
    private String country;
    
    private boolean winner;

    
    public Competitor(){
        
    }
    
    public Competitor(String nameN, String surnameN, int ageN,String telephoneN, String cellphoneN, String addressN, String  cityN, String countryN,boolean winnerN){
        name=nameN;
        surname=surnameN;
        age=ageN;
        telephone=telephoneN;
        cellphone=cellphoneN;
        address=addressN;
        city=cityN;
        country=countryN;
        winner=winnerN;
    }
    
   
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getCellphone() {
        return cellphone;
    }

    public void setCellphone(String cellphone) {
        this.cellphone = cellphone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public boolean isWinner() {
        return winner;
    }

    public void setWinner(boolean winner) {
        this.winner = winner;
    }
    
}
