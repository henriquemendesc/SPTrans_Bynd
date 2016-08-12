package br.com.sptrans.sptrans.persistence;

import android.support.annotation.Nullable;

/**
 * Created by Bob on 06/08/2016.
 */
public interface WAPICallback<V> {
    void onSuccess(@Nullable V result);
    //Quando ocorrer alguma falha ao adicionar o local, será chamado o método onFailure
    void onFailure(Throwable t);
}
