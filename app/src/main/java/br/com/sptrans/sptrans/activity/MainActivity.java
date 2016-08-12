package br.com.sptrans.sptrans.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.List;

import br.com.sptrans.sptrans.R;
import br.com.sptrans.sptrans.adapter.LinhaAdapter;
import br.com.sptrans.sptrans.domain.Linha;
import br.com.sptrans.sptrans.domain.ListaParadas;
import br.com.sptrans.sptrans.domain.Parada;
import br.com.sptrans.sptrans.persistence.LocalDB;
import br.com.sptrans.sptrans.persistence.WAPICallback;
import br.com.sptrans.sptrans.persistence.WAPIService;
import br.com.sptrans.sptrans.util.GPSUtils;
import br.com.sptrans.sptrans.util.PermissionUtils;

public class MainActivity extends BaseActivity implements AdapterView.OnItemClickListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener {
    private ListView listView;
    protected GoogleApiClient mGoogleApiClient;
    private Location localAtual = null;
    private LocalDB localDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Solicita as permissões
        String[] permissoes = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.INTERNET,
                Manifest.permission.WAKE_LOCK
        };
        PermissionUtils.validate(this, 0, permissoes);

        //Criando objeto para conexão com google play services
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        WAPIService.initInstance(this);
        GPSUtils.initInstance(this, mGoogleApiClient, (LocationManager) getSystemService(Context.LOCATION_SERVICE));

        ImageButton btnBuscar = (ImageButton) findViewById(R.id.btnBuscar);
        btnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                buscar();
            }
        });

        Button btnApagarHist = (Button) findViewById(R.id.btnApagar);
        btnApagarHist.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                localDB.deleteAll();
                listView.setAdapter(new LinhaAdapter(getContext(), localDB.findAll()));
            }
        });

        localDB = new LocalDB(this);

        listView = (ListView) findViewById(R.id.linhasListview);
        listView.setOnItemClickListener(this);
        listView.setAdapter(new LinhaAdapter(this, localDB.findAll()));
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Conecta no Google Play Services
        if (!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        // Desconecta do Google Play Services
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, View view, final int idx, long id){
        Linha linha = (Linha) parent.getAdapter().getItem(idx);
        localDB.save(linha);
        showProgress(true);
        WAPIService.getInstance().buscarPrevisaoParada(linha.CodigoLinha, localAtual, new WAPICallback<List<Parada>>() {
            @Override
            public void onSuccess(@Nullable List<Parada> result) {
                ListaParadas paradas = new ListaParadas();
                paradas.paradas = result;
                Intent intent = new Intent(getContext(), PrevisaoActivity.class);
                intent.putExtra("paradas", paradas);
                intent.putExtra("localAtual", localAtual);
                showProgress(false);
                startActivity(intent);
            }

            @Override
            public void onFailure(Throwable t) {
                showProgress(false);
                Toast("Não foi possível conectar com o servidor. Tente novamente mais tarde.");
            }
        });
    }

    //==================Métodos de controle do Google Play Services (GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks) ======================//
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
        showAlertOk("Não foi possível conectar na Google API Client: Error " + connectionResult.getErrorCode());
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.e(TAG, "Conectado no Google Play Services!");
        if (GPSUtils.getInstance().GPSAtivo()) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.e(TAG, "Conexão interrompida.");
    }

    //==================Métodos de controle do LocationListener======================//
    @Override
    public void onLocationChanged(Location location) {
        //
        if (location != null) {
            localAtual = location;
        }
        stopLocationUpdates();
    }

    //==================Métodos auxiliares======================//

    private void buscar(){
        EditText edtBuscar = (EditText)findViewById(R.id.edtBuscar);

        showProgress(true);
        WAPIService.getInstance().buscarLinha(edtBuscar.getText().toString(), new WAPICallback<List<Linha>>() {
            @Override
            public void onSuccess(List<Linha> result) {
                showProgress(false);
                listView.setAdapter(new LinhaAdapter(getContext(), result));
            }

            @Override
            public void onFailure(Throwable throwable) {
                showProgress(false);
                Toast("Não foi possível conectar com o servidor. Tente novamente mais tarde.");
            }
        });
    }

    private void showAlertOk(String mensagem){
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(TAG);
        alertDialog.setMessage(mensagem);
        alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) { }
        });
        alertDialog.show();
    }

    private void startLocationUpdates(){
        //Inicia GPS para buscar o local atual
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //Possui permissão para acessar o GPS
            LocationRequest mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(2000);
            mLocationRequest.setFastestInterval(1000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            showProgress(true);
        }
    }

    protected void stopLocationUpdates(){
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        showProgress(false);
    }
}
