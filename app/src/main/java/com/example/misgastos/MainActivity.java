package com.example.misgastos;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    // Símbolo de moneda que se muestra junto a los montos. Cámbialo si quieres (por ejemplo "Bs ").
    private static final String MONEDA = "$";

    // Nombres usados para guardar los datos en el teléfono.
    private static final String ARCHIVO = "mis_gastos";
    private static final String CLAVE_LISTA = "lista_de_gastos";

    // Un gasto: descripción + monto.
    private static class Gasto {
        String descripcion;
        double monto;

        Gasto(String descripcion, double monto) {
            this.descripcion = descripcion;
            this.monto = monto;
        }
    }

    private final ArrayList<Gasto> gastos = new ArrayList<>();
    private GastoAdapter adapter;

    private EditText campoDescripcion;
    private EditText campoMonto;
    private TextView textoTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Conectar las vistas del diseño con el código.
        campoDescripcion = findViewById(R.id.campoDescripcion);
        campoMonto = findViewById(R.id.campoMonto);
        textoTotal = findViewById(R.id.textoTotal);
        Button botonAgregar = findViewById(R.id.botonAgregar);
        ListView listaGastos = findViewById(R.id.listaGastos);
        TextView textoVacio = findViewById(R.id.textoVacio);

        // Leer los gastos guardados y mostrarlos.
        cargarGastos();
        adapter = new GastoAdapter();
        listaGastos.setAdapter(adapter);
        listaGastos.setEmptyView(textoVacio);
        actualizarTotal();

        botonAgregar.setOnClickListener(v -> agregarGasto());
    }

    // ---------- Agregar ----------

    private void agregarGasto() {
        String descripcion = campoDescripcion.getText().toString().trim();
        String textoMonto = campoMonto.getText().toString().trim().replace(',', '.');

        if (descripcion.isEmpty()) {
            campoDescripcion.setError("Escribe una descripción");
            return;
        }

        double monto;
        try {
            monto = Double.parseDouble(textoMonto);
        } catch (NumberFormatException e) {
            campoMonto.setError("Escribe un monto válido");
            return;
        }
        if (monto <= 0) {
            campoMonto.setError("El monto debe ser mayor a 0");
            return;
        }

        gastos.add(0, new Gasto(descripcion, monto)); // el más nuevo aparece primero
        guardarGastos();
        adapter.notifyDataSetChanged();
        actualizarTotal();

        // Limpiar el formulario y ocultar el teclado.
        campoDescripcion.setText("");
        campoMonto.setText("");
        campoDescripcion.requestFocus();
        InputMethodManager teclado = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        teclado.hideSoftInputFromWindow(campoMonto.getWindowToken(), 0);
    }

    // ---------- Eliminar ----------

    private void eliminarGasto(int posicion) {
        gastos.remove(posicion);
        guardarGastos();
        adapter.notifyDataSetChanged();
        actualizarTotal();
    }

    // ---------- Total ----------

    private void actualizarTotal() {
        double total = 0;
        for (Gasto gasto : gastos) {
            total += gasto.monto;
        }
        textoTotal.setText(formatearMonto(total));
    }

    private String formatearMonto(double monto) {
        return MONEDA + String.format(Locale.getDefault(), "%.2f", monto);
    }

    // ---------- Guardar y cargar (almacenamiento local) ----------

    private void guardarGastos() {
        JSONArray lista = new JSONArray();
        try {
            for (Gasto gasto : gastos) {
                JSONObject objeto = new JSONObject();
                objeto.put("descripcion", gasto.descripcion);
                objeto.put("monto", gasto.monto);
                lista.put(objeto);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SharedPreferences datos = getSharedPreferences(ARCHIVO, MODE_PRIVATE);
        datos.edit().putString(CLAVE_LISTA, lista.toString()).apply();
    }

    private void cargarGastos() {
        SharedPreferences datos = getSharedPreferences(ARCHIVO, MODE_PRIVATE);
        String texto = datos.getString(CLAVE_LISTA, "[]");
        try {
            JSONArray lista = new JSONArray(texto);
            for (int i = 0; i < lista.length(); i++) {
                JSONObject objeto = lista.getJSONObject(i);
                gastos.add(new Gasto(objeto.getString("descripcion"), objeto.getDouble("monto")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // ---------- Adaptador: dibuja cada fila de la lista ----------

    private class GastoAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return gastos.size();
        }

        @Override
        public Object getItem(int posicion) {
            return gastos.get(posicion);
        }

        @Override
        public long getItemId(int posicion) {
            return posicion;
        }

        @Override
        public View getView(final int posicion, View vista, ViewGroup padre) {
            if (vista == null) {
                vista = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_gasto, padre, false);
            }

            Gasto gasto = gastos.get(posicion);
            ((TextView) vista.findViewById(R.id.textoDescripcion)).setText(gasto.descripcion);
            ((TextView) vista.findViewById(R.id.textoMonto)).setText(formatearMonto(gasto.monto));

            vista.findViewById(R.id.botonEliminar).setOnClickListener(v -> eliminarGasto(posicion));
            return vista;
        }
    }
}
