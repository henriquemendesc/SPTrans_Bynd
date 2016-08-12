package br.com.sptrans.sptrans.activity;

import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import br.com.sptrans.sptrans.R;
import br.com.sptrans.sptrans.adapter.PrevisaoAdapter;
import br.com.sptrans.sptrans.domain.Linha;
import br.com.sptrans.sptrans.domain.ListaParadas;
import br.com.sptrans.sptrans.domain.Parada;

public class PrevisaoActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    private ListView listView;
    private Location localAtual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_previsao);

        Bundle args = getIntent().getExtras();
        ListaParadas listaParadas = (ListaParadas) args.get("paradas");
        localAtual = (Location) args.get("localAtual");
        listView = (ListView) findViewById(R.id.previsoesListview);
        listView.setOnItemClickListener(this);
        listView.setAdapter(new PrevisaoAdapter(this, listaParadas.paradas));
        listView.setEmptyView(findViewById(R.id.txtEmpty));
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, View view, final int idx, long id){
        Parada parada = (Parada) parent.getAdapter().getItem(idx);
        Intent intent = new Intent(getContext(), MapActivity.class);
        LatLng origem;
        if (localAtual != null){
            //Se local atual preenchido então mostra o mapa com a rota
            origem = new LatLng(localAtual.getLatitude(), localAtual.getLongitude());
        }else{
            //Se local atual não está preenchido então mostra o mapa com a localização da parada
            origem = null;
        }
        LatLng destino = new LatLng(parada.Latitude, parada.Longitude);
        intent.putExtra("origem", origem);
        intent.putExtra("destino", destino);
        startActivity(intent);
    }
}
