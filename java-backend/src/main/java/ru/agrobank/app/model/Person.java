package ru.agrobank.app.model;

public class Person {
    protected String name;
    protected String inn;

    public Person(String name, String inn) {
        this.name = name;
        this.inn = inn;
    }

    public String getName() { return name; }
    public String getInn() { return inn; }
}