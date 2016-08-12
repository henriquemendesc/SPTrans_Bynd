package br.com.sptrans.sptrans.persistence;

import android.content.Context;
import android.location.Location;
import android.support.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.sptrans.sptrans.domain.*;

/**
 * Created by Bob on 06/08/2016.
 */
public class WAPIService {
    private static WAPIService instance;
    private static final String TAG = "SPTrans";
    private static String url = "http://api.olhovivo.sptrans.com.br/v0";
    private static String token = "8df589e9e07718378c20b325236c03ad717d9e16a7bed958da40bc71755e5776";
    private RequestQueue queue;

    public static void initInstance(Context context)
    {
        if (instance == null)
        {
            // Create the instance
            instance = new WAPIService(context);
        }
    }

    public static void finalizeInstance(){

    }

    public static WAPIService getInstance()
    {
        // Return the instance
        return instance;
    }

    public WAPIService (Context context){
        queue = Volley.newRequestQueue(context);
        CookieManager manager = new CookieManager();
        CookieHandler.setDefault(manager);
    }

    public void login(final WAPICallback<String> callback){

        String loginURL = url + "/Login/Autenticar?token=" + token;

        //Cria a requisição
        StringRequest req = new StringRequest(Request.Method.POST, loginURL,
                //Método que será chamado caso o login dê certo
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equals("true")){
                            callback.onSuccess("Login realizado com sucesso.");
                        }else{
                            callback.onFailure(new Throwable("Erro ao realizar login com servidor."));
                        }
                    }
                },
                //Método que será chamado caso dê algum erro no login
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onFailure(error);
                    }
                }){
        };

        queue.add(req);
    }

    public void buscarLinha(String linha, final WAPICallback<List<Linha>> callback){

        final String linhaURL = url + "/Linha/Buscar?termosBusca=" + linha;

        //Realiza o login antes de fazer a requisição
        login(new WAPICallback<String>() {

            @Override
            public void onSuccess(@Nullable String result) {
                //Cria a requisição
                JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, linhaURL, new JSONArray(),
                    //Método que será chamado caso dê certo
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            List<Linha> linhas = new ArrayList<Linha>();
                            Linha linha;
                            JSONObject linhaJson;
                            for (int i = 0; i < response.length(); i++) {
                                try {
                                    linhaJson = response.getJSONObject(i);
                                    linha = new Linha();
                                    linha.CodigoLinha = linhaJson.getInt("CodigoLinha");
                                    linha.Circular = linhaJson.getBoolean("Circular");
                                    linha.Letreiro = linhaJson.getString("Letreiro");
                                    linha.Sentido = linhaJson.getInt("Sentido");
                                    linha.Tipo = linhaJson.getInt("Tipo");
                                    linha.DenominacaoTPTS = linhaJson.getString("DenominacaoTPTS");
                                    linha.DenominacaoTSTP = linhaJson.getString("DenominacaoTSTP");
                                    linha.Informacoes = linhaJson.getString("Informacoes");
                                    linhas.add(linha);
                                } catch (JSONException e) {
                                    //não faz nada
                                }
                            }
                            callback.onSuccess(linhas);
                        }
                    },
                    //Método que será chamado caso dê algum erro
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            callback.onFailure(error);
                        }
                    }){
                };
                queue.add(req);
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onFailure(t);
            }
        });
    }

    private String getHorarios(JSONArray ja){
        String horarios = "";
        String sep = "";
        JSONObject horarioJson;
        for (int i = 0; i < ja.length(); i++) {
            try {
                horarioJson = ja.getJSONObject(i);
                horarios = horarios + sep + horarioJson.getString("t");
                sep = ", ";
            } catch (JSONException e) {
                //não faz nada
            }
        }
        return horarios;
    }

    private String getEnderecoParada(JSONArray paradas, int codigoParada){
        JSONObject paradaJson;
        for (int i = 0; i < paradas.length(); i++) {
            try {
                paradaJson = paradas.getJSONObject(i);
                if (paradaJson.getInt("CodigoParada")  == codigoParada) {
                    return paradaJson.getString("Endereco");
                }
            } catch (JSONException e) {
                //Não faz nada
            }
        }
        return "";
    }

    private String getDistanciaParada(Location localAtual, Double latitudeParada, Double longitudeParada){
        if (localAtual != null){

            LatLng posicaoInicial = new LatLng(localAtual.getLatitude(),localAtual.getLongitude());
            LatLng posicaiFinal = new LatLng(latitudeParada,longitudeParada);

            double distance = SphericalUtil.computeDistanceBetween(posicaoInicial, posicaiFinal);
            String unit = "m";
            if (distance >= 1000) {
                distance /= 1000;
                unit = "km";
            }

            return String.format("%4.3f%s", distance, unit);
        }
        return "";
    }

    public void buscarParada(int codigoLinha, final WAPICallback<JSONArray> callback){

        final String paradaURL = url + "/Parada/BuscarParadasPorLinha?codigoLinha=" + String.valueOf(codigoLinha);

        //Realiza o login antes de fazer a requisição
        login(new WAPICallback<String>() {

            @Override
            public void onSuccess(@Nullable String result) {
                //Cria a requisição
                JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, paradaURL, new JSONArray(),
                        //Método que será chamado caso dê certo
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                callback.onSuccess(response);
                            }
                        },
                        //Método que será chamado caso dê algum erro
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                callback.onFailure(error);
                            }
                        }){
                };
                queue.add(req);
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onFailure(t);
            }
        });
    }

    public void buscarPrevisaoParada(int codigoLinha, final Location localAtual, final WAPICallback<List<Parada>> callback){

        final String paradaURL = url + "/Previsao/Linha?codigoLinha=" + String.valueOf(codigoLinha);

        buscarParada(codigoLinha, new WAPICallback<JSONArray>() {

            @Override
            public void onSuccess(final JSONArray paradasEndereco) {
                //Cria a requisição
                JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, paradaURL, new JSONObject(),
                        //Método que será chamado caso dê certo
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                List<Parada> paradas = new ArrayList<Parada>();
                                Parada parada;
                                JSONArray listaParadaJson;
                                JSONObject paradaJson;
                                String horarios;
                                try {
                                    listaParadaJson = response.getJSONArray("ps");
                                } catch (JSONException e) {
                                    listaParadaJson = null;
                                }
                                if (listaParadaJson != null) {
                                    for (int i = 0; i < listaParadaJson.length(); i++) {
                                        try {
                                            paradaJson = listaParadaJson.getJSONObject(i);
                                            parada = new Parada();
                                            parada.CodigoParada = paradaJson.getInt("cp");
                                            parada.Nome = paradaJson.getString("np");
                                            parada.Endereco = getEnderecoParada(paradasEndereco, parada.CodigoParada);
                                            parada.Latitude = paradaJson.getDouble("py");
                                            parada.Longitude = paradaJson.getDouble("px");
                                            parada.Distancia = getDistanciaParada(localAtual, parada.Latitude, parada.Longitude);
                                            horarios = getHorarios(paradaJson.getJSONArray("vs"));
                                            if (!horarios.isEmpty()) {
                                                parada.Horarios = horarios;
                                                paradas.add(parada);
                                            }
                                        } catch (JSONException e) {
                                            //não faz nada
                                        }
                                    }
                                }
                                callback.onSuccess(paradas);
                            }
                        },
                        //Método que será chamado caso dê algum erro
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                callback.onFailure(error);
                            }
                        }){
                };
                queue.add(req);
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onFailure(t);
            }
        });
    }
}
