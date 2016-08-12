package br.com.sptrans.sptrans.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import br.com.sptrans.sptrans.R;
import br.com.sptrans.sptrans.domain.Parada;

/**
 * Created by Bob on 07/08/2016.
 */
public class PrevisaoAdapter extends BaseAdapter {
    private final Context context;
    private final List<Parada> paradas;

    public PrevisaoAdapter(Context context, List<Parada> paradas){
        super();
        this.context = context;
        this.paradas = paradas;
    }

    @Override
    public int getCount(){
        return paradas != null ? paradas.size() : 0;
    }

    @Override
    public Object getItem(int position){
        return paradas.get(position);
    }

    public void removeItem(int position){
        paradas.remove(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_previsao, parent, false);
        TextView txtNome = (TextView) view.findViewById(R.id.txtNome);
        TextView txtEndereco = (TextView) view.findViewById(R.id.txtEndereco);
        TextView txtHorarios = (TextView) view.findViewById(R.id.txtHorarios);

        Parada parada = paradas.get(position);
        String nome = parada.Nome;
        if (!parada.Distancia.isEmpty()){
            nome = nome + " (" + parada.Distancia + ")";
        }
        txtNome.setText(nome);
        txtEndereco.setText(parada.Endereco);
        txtHorarios.setText(parada.Horarios);

        return view;
    }
}
