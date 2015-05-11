package com.quimiagsystems.controlcompras;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    public static final String TAG = "TratamientoXML";

    Double totalCompras;
    Double totalCostProd;
    Double totalPresupuesto;
    Double saldo;

    Spinner spinnerCategoria;
    Spinner spinnerProductos;

    EditText txtCost;
    EditText txtCant;
    EditText txtTotal;
    EditText txtPresup;
    EditText txtCompras;
    EditText txtSaldo;

    Button btnIgual;
    Button btnEstad;
    Button btnRegistrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        totalCompras = 0.0;
        cargarElementos();
        cargarCombos();
        cargarBotones();
        leerXML();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.addCat) {
            return true;
        }

        if (id == R.id.addProd) {
            return true;
        }

        if (id == R.id.estadisticas) {
            return true;
        }

        if (id == R.id.quitarProd) {
            return true;
        }

        if (id == R.id.borrarDatos) {
            totalCompras = 0.0;
            totalPresupuesto = 0.0;
            saldo = 0.0;

            txtPresup.setText("");
            txtCompras.setText("");
            txtSaldo.setText("");

            borrarDatosXML();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void cargarCombos() {
        ArrayAdapter<CharSequence> adapterCategoria = ArrayAdapter.createFromResource(this, R.array.categoria, android.R.layout.simple_spinner_item);
        adapterCategoria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapterCategoria);

        final ArrayAdapter<CharSequence> adapterProductosTodos = ArrayAdapter.createFromResource(this, R.array.productos, android.R.layout.simple_spinner_item);

        spinnerCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapt, View v, int pos, long id) {
                String spinCat= (String) spinnerCategoria.getSelectedItem();
                spinnerProductos.setAdapter(obtProductos(spinCat, adapterProductosTodos));
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });
    }

    private ArrayAdapter obtProductos(String spinCat, ArrayAdapter adapterProductosTodos){
        //Inicializo array
        List productosArray= new ArrayList();

        //Cargo los productos
        for (int i = 0; i<adapterProductosTodos.getCount(); i++){
            String productoCat = (String) adapterProductosTodos.getItem(i);
            if(spinCat.length() <= productoCat.length()) {
                String categoria = productoCat.substring(0, spinCat.length());
                if(categoria.equals(spinCat)) {
                    String producto = productoCat.substring(spinCat.length()+1);
                    productosArray.add(producto);
                }
            }
        }

        ArrayAdapter<String> adapterProductos = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_spinner_item, productosArray);
        adapterProductos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        return adapterProductos;
    }

    private void botonIgual(){
        btnIgual.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                totalCostProd = Double.parseDouble(txtCost.getText().toString()) * Double.parseDouble(txtCant.getText().toString());
                txtTotal.setText(totalCostProd.toString());
            }
        });
    }

    private void botonRegistrar(){
        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                totalPresupuesto = Double.parseDouble(txtPresup.getText().toString());

                //Calculos
                totalCompras = totalCompras + totalCostProd;
                saldo = totalPresupuesto - totalCompras;

                //Asignar valores
                txtCompras.setText(totalCompras.toString());
                txtSaldo.setText(saldo.toString());

                //escribir datos
                escribirResultadoXML();
            }
        });
    }

    private void botonEstadistica(){
        btnEstad.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            }
        });
    }


    private void cargarBotones(){
        botonIgual();
        botonRegistrar();
        botonEstadistica();
    }

    private void cargarElementos(){
        txtCost = (EditText) findViewById(R.id.txtCosto);
        txtCant = (EditText) findViewById(R.id.txtCantidad);
        txtTotal = (EditText) findViewById(R.id.txtTotal);
        txtPresup = (EditText) findViewById(R.id.txtPresupuesto);
        txtCompras = (EditText) findViewById(R.id.txtCompras);
        txtSaldo = (EditText) findViewById(R.id.txtSaldo);

        spinnerCategoria = (Spinner) findViewById(R.id.spnCategoria);
        spinnerProductos = (Spinner) findViewById(R.id.spnProducto);

        btnIgual = (Button) findViewById(R.id.btnIgual);
        btnRegistrar = (Button) findViewById(R.id.btnRegistrarCompra);
        btnEstad = (Button) findViewById(R.id.btnEstadistica);
    }


    private void escribirResultadoXML() {
        FileOutputStream fout = null;

        try {
            fout = openFileOutput("Datos.xml", MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }

        XmlSerializer serializer = Xml.newSerializer();
        try {
            serializer.setOutput(fout, "UTF-8");
            serializer.startDocument(null, true);
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

            serializer.startTag(null, "proceso");
            serializer.attribute(null, "presupuesto", totalPresupuesto.toString());
            serializer.attribute(null, "compras", totalCompras.toString());
            serializer.attribute(null, "saldo", saldo.toString());
            serializer.endTag(null, "proceso");
            serializer.endDocument();
            serializer.flush();
            fout.close();

           // Toast.makeText(getApplicationContext(), "Registro guardado correctamente", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void borrarDatosXML() {
        FileOutputStream fout = null;

        try {
            fout = openFileOutput("Datos.xml", MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }

        XmlSerializer serializer = Xml.newSerializer();
        try {
            serializer.setOutput(fout, "UTF-8");
            serializer.startDocument(null, true);
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.endDocument();
            serializer.flush();
            fout.close();

            // Toast.makeText(getApplicationContext(), "Registro guardado correctamente", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void leerXML() {
        FileInputStream fin = null;

        try {
            fin = openFileInput("Datos.xml");
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(fin, "UTF-8");

            int event = parser.next();
            while(event != XmlPullParser.END_DOCUMENT) {
                if(event == XmlPullParser.START_TAG) {
                    for(int i = 0; i < parser.getAttributeCount(); i++) {
                        System.out.println(parser.getAttributeName(i).toString());
                        switch (parser.getAttributeName(i).toString()){
                            case "presupuesto":
                                totalPresupuesto =   Double.parseDouble(parser.getAttributeValue(i).toString());
                                txtPresup.setText(totalPresupuesto.toString());
                                break;

                            case "compras":
                                totalCompras =   Double.parseDouble(parser.getAttributeValue(i).toString());
                                txtCompras.setText(totalCompras.toString());
                                break;

                            case "saldo":
                                saldo =   Double.parseDouble(parser.getAttributeValue(i).toString());
                                txtSaldo.setText(saldo.toString());
                                break;
                        }

                    }
                }
                event = parser.next();
            }
            fin.close();

        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
