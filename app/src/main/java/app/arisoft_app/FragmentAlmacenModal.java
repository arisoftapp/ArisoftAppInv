package app.arisoft_app;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import java.util.Vector;

import app.arisoft_app.Modelo.Existencia;
import app.arisoft_app.Tools.Database;

public class FragmentAlmacenModal extends BottomSheetDialogFragment {
    private BottomSheetListener mListener;
    Existencia articulo;
    String obtenido;
    Spinner spinner;
    ArrayAdapter<String> comboAdapterSql;
    Vector<String> codigo = new Vector<String>();
    Vector<String> datos = new Vector<String>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_sheet_almacen, container, false);
        spinner = v.findViewById(R.id.spinner_alm2);
        spinner.setAdapter(getAlmacenes());

        try{
            Bundle bundle = getArguments();
            articulo = bundle.getParcelable("codigoProducto");
            obtenido = bundle.getString("codigoProducto");

        }catch (Exception e){
            e.printStackTrace();
        }

        Button btn_consultar = v.findViewById(R.id.btn_consultar);
        btn_consultar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(),ArticuloExistenciaAlmDetallesActivity.class);
                intent.putExtra("codigoProducto", obtenido);
                int pos = spinner.getSelectedItemPosition();
                intent.putExtra("idalmacen", codigo.get(pos).toString());
                startActivity(intent);
                dismiss();
            }
        });
        return v;
    }


    public void onStart(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    public ArrayAdapter<String> getAlmacenes (){
        Database admin = new Database(getContext(),null,1);
        String respuesta="";
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor fila = db.rawQuery("SELECT * FROM almacenes",null);
        if(fila.moveToFirst())
        {
            do{
                codigo.add(fila.getString(0));
                datos.add(fila.getString(0)+" - "+fila.getString(1));
            }while (fila.moveToNext());
            comboAdapterSql = new ArrayAdapter<>(getContext(), R.layout.textspinner_alm, datos);
            spinner.setAdapter(comboAdapterSql);
        }
        else
        {
            Toast.makeText(getContext(), "No existen artículos", Toast.LENGTH_SHORT).show();
        }
        db.close();
        return comboAdapterSql;
    }

    public interface BottomSheetListener {
        void onButtonClicked(String text);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (BottomSheetListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement BottomSheetListener");
        }
    }
}
