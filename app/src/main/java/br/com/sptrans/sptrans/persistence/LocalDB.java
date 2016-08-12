package br.com.sptrans.sptrans.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import br.com.sptrans.sptrans.domain.Linha;

/**
 * Created by Bob on 07/08/2016.
 */
public class LocalDB extends SQLiteOpenHelper {
    // Nome do banco
    public static final String NOME_BANCO = "sptrans.localDB";
    private static final String TAG = "sql";
    private static final int VERSAO_BANCO = 1;

    public LocalDB(Context context) {
        // context, nome do banco, factory, versão
        super(context, NOME_BANCO, null, VERSAO_BANCO);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Criando a Tabela linha...");

        db.execSQL("create table if not exists linha (" +
                "CodigoLinha int," +
                "Circular text," +
                "Letreiro text," +
                "Sentido int," +
                "Tipo int," +
                "DenominacaoTPTS text," +
                "DenominacaoTSTP text," +
                "Informacoes text);");
        Log.d(TAG, "Tabela linha criada com sucesso.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Caso mude a versão do banco de dados, podemos executar um SQL aqui
    }

    // Insere uma nova linha
    public void save(Linha linha) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            if (linha.Circular){
                values.put("Circular", "true");
            }else{
                values.put("Circular", "false");
            }
            values.put("CodigoLinha", linha.CodigoLinha);
            values.put("Letreiro", linha.Letreiro);
            values.put("Sentido", linha.Sentido);
            values.put("Tipo", linha.Tipo);
            values.put("DenominacaoTPTS", linha.DenominacaoTPTS);
            values.put("DenominacaoTSTP", linha.DenominacaoTSTP);
            values.put("Informacoes", linha.Informacoes);

            String codigoLinha = String.valueOf(linha.CodigoLinha);
            String[] whereArgs = new String[]{codigoLinha};
            int count = db.update("linha", values, "CodigoLinha=?", whereArgs);
            if (count == 0){
                // insert into linha values (...)
                db.insert("linha", "", values);
            }
        } finally {
            db.close();
        }
    }

    // Deleta todos as linhas
    public void deleteAll() {
        SQLiteDatabase db = getWritableDatabase();
        try {
            int count = db.delete("linha", "", null);
            Log.i(TAG, "Deletou [" + count + "] registro.");
        } finally {
            db.close();
        }
    }

    // Consulta a lista com todos as linhas
    public List<Linha> findAll() {
        SQLiteDatabase db = getWritableDatabase();
        try {
            // select * from linha
            Cursor c = db.query("linha", null, null, null, null, null, null, null);
            return toList(c);
        } finally {
            db.close();
        }
    }

    // Lê o cursor e cria a lista de países
    private ArrayList<Linha> toList(Cursor c) {
        ArrayList<Linha> linhas = new ArrayList<Linha>();
        if (c.moveToFirst()) {
            do {
                Linha linha = new Linha();
                linhas.add(linha);

                // recupera os atributos da linha
                linha.CodigoLinha = c.getInt(c.getColumnIndex("CodigoLinha"));
                linha.Circular = c.getString(c.getColumnIndex("Circular")).equals("true");
                linha.Letreiro = c.getString(c.getColumnIndex("Letreiro"));
                linha.Sentido = c.getInt(c.getColumnIndex("Sentido"));
                linha.Tipo = c.getInt(c.getColumnIndex("Tipo"));
                linha.DenominacaoTPTS = c.getString(c.getColumnIndex("DenominacaoTPTS"));
                linha.DenominacaoTSTP = c.getString(c.getColumnIndex("DenominacaoTSTP"));
                linha.Informacoes = c.getString(c.getColumnIndex("Informacoes"));
            } while (c.moveToNext());
        }
        return linhas;
    }

    // Executa um SQL
    public void execSQL(String sql) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.execSQL(sql);
        } finally {
            db.close();
        }
    }

    // Executa um SQL
    public void execSQL(String sql, Object[] args) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.execSQL(sql, args);
        } finally {
            db.close();
        }
    }

    public int getCountLinha(){
        SQLiteDatabase db = getWritableDatabase();
        try {
            // select * from linha
            Cursor c = db.query("linha", null, null, null, null, null, null, null);
            return c.getCount();
        } finally {
            db.close();
        }
    }
}
