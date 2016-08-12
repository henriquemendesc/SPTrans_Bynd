package br.com.sptrans.sptrans.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class BaseActivity extends AppCompatActivity {
    protected static final String TAG = "SPTrans";
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void Toast(String mensagem){
        Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();
    }

    protected Context getContext(){
        return this;
    }

    protected void showProgress(boolean show){
        if (show){
            dialog = ProgressDialog.show(this, "SPTrans", "Por favor, aguarde...", false, true);
        }else{
            dialog.dismiss();
        }
    }

    protected void startNewIntent(Class<?> cls, Boolean fechar) {
        Intent intent;
        intent = new Intent(this, cls);
        startActivity(intent);
        if (fechar) {
            finish();
        }
    }
}
