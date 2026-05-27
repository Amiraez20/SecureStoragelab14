package com.example.storagesecure.model;

/**
 * Représente un apprenant inscrit.
 * Objet immuable : tous les champs sont final.
 */
public class Learner {
    public final int    identifier;
    public final String fullName;
    public final int    age;

    public Learner(int identifier, String fullName, int age) {
        this.identifier = identifier;
        this.fullName   = fullName;
        this.age        = age;
    }
}
