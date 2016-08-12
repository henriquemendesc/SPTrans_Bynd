package br.com.sptrans.sptrans.domain;

import java.io.Serializable;

/**
 * Created by Bob on 06/08/2016.
 */
public class Parada implements Serializable {
    public int CodigoParada;
    public String Nome;
    public String Endereco;
    public double Latitude;
    public double Longitude;
    public String Horarios;
    public String Distancia;
}
