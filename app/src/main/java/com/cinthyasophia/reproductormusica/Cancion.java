package com.cinthyasophia.reproductormusica;

import java.io.Serializable;

public class Cancion implements Serializable {
    private int id;
    private String nombre;
    private int duracion;

    public Cancion(int id, String nombre, int duracion) {
        this.id = id;
        this.nombre = nombre;
        this.duracion = duracion;
    }

    public Cancion() {
    }

    public int getId() {
        return id;
    }


    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getDuracion() {
        return duracion;
    }

    public void setDuracion(int duracion) {
        this.duracion = duracion;
    }
}
