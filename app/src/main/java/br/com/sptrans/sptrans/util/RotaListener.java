package br.com.sptrans.sptrans.util;

import com.google.android.gms.maps.model.PolylineOptions;

/**
 * Created by Bob on 06/08/2016.
 */
public interface RotaListener {
    void onInstrucoesRotaChanged(PolylineOptions instrucoesRota);
    void onFailure(Throwable t);
}
