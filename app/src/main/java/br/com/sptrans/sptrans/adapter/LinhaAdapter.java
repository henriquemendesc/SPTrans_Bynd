package br.com.sptrans.sptrans.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import br.com.sptrans.sptrans.R;
import br.com.sptrans.sptrans.domain.Linha;

/**
 * Created by Bob on 06/08/2016.
 */
public class LinhaAdapter extends BaseAdapter {
    private final Context context;
    private final List<Linha> linhas;

    public LinhaAdapter(Context context, List<Linha> linhas){
        super();
        this.context = context;
        this.linhas = linhas;
    }

    @Override
    public int getCount(){
        return linhas != null ? linhas.size() : 0;
    }

    @Override
    public Object getItem(int position){
        return linhas.get(position);
    }

    public void removeItem(int position){
        linhas.remove(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_linha, parent, false);
        TextView titulo = (TextView) view.findViewById(R.id.txtTitulo);
        TextView subtitulo = (TextView) view.findViewById(R.id.txtSubtitulo);

        Linha linha = linhas.get(position);
        titulo.setText(linha.Letreiro);
        if (linha.Sentido == 1){ /*Terminal principal para terminal secundário*/
            subtitulo.setText(linha.DenominacaoTPTS);
        }else{ /*Terminal secundário para terminal primário*/
            subtitulo.setText(linha.DenominacaoTSTP);
        }

        return view;
    }
}
