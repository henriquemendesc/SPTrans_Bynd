package br.com.sptrans.sptrans.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.BaseAdapter;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import br.com.sptrans.sptrans.R;
import br.com.sptrans.sptrans.util.GPSUtils;
import br.com.sptrans.sptrans.util.RotaListener;

public class MapActivity extends BaseActivity implements RotaListener, OnMapReadyCallback {
    private SupportMapFragment mapFragment;
    protected GoogleMap map;
    private PolylineOptions rota = null;
    private LatLng origem;
    private LatLng destino;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Bundle args = getIntent().getExtras();
        origem = (LatLng) args.get("origem");
        destino = (LatLng) args.get("destino");
        if (origem != null) {
            showProgress(true);
            GPSUtils.getInstance().calcularRota(origem, destino, "walking", this);
        }

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    //==========RotaListener===========//
    @Override
    public void onInstrucoesRotaChanged(PolylineOptions instrucoesRota){
        rota = instrucoesRota;
        updateMap();
        showProgress(false);
    }

    @Override
    public void onFailure(Throwable t) {
        showProgress(false);
        Toast("Erro ao traçar rota. Tente novamente mais tarde.");
        finish();
    }

    //=========OnMapReadyCallback=============//
    @Override
    public void onMapReady(GoogleMap map) {
        Log.d(TAG, "onMapReady: " + map);
        this.map = map;

        // Configura o tipo do mapa
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast("Malalai não tem permissão para utilizar o GPS.");
            finish();
            return;
        }
        map.setMyLocationEnabled(true);
        updateMap();
    }

    //==========Métodos auxiliares da MapActivity===========//
    private void updateMap(){
        if (map == null){
            return;
        }
        map.clear();

        if (origem != null) {
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(origem, 15);
            map.animateCamera(update);
        }else if (destino != null){
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(destino, 15);
            map.animateCamera(update);
        }

        if (rota != null){
            map.addPolyline(rota);

            MarkerOptions options = new MarkerOptions();
            //Adicionando ponto inicial
            options.position(origem);
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            map.addMarker(options);

            //Adicionando ponto inicial e final da rota
            options.position(destino);
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            map.addMarker(options);
        }else if (destino != null){
            MarkerOptions options = new MarkerOptions();
            //Adicionando ponto final da rota
            options.position(destino);
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            map.addMarker(options);
        }
    }
}
