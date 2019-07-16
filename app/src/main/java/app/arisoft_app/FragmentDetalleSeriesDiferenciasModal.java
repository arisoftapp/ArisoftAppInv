package app.arisoft_app;

import android.app.Dialog;
import android.app.DialogFragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import app.arisoft_app.Modelo.Serie;
import app.arisoft_app.Tools.Database;

public class FragmentDetalleSeriesDiferenciasModal extends DialogFragment {
    ArrayList<Serie> Series_contadas;
    ArrayList<Serie> Series_nocontadas;
    String idalmacen;
    TextView tvcodigo, tvnombre, tvexistencia, tvconteo, tvdiferencia, tvnoconteo, tvnodescartado;
    ListView lv_series_contadas, lv_series_no;
    Button btn_aceptar;
    String codigo, nombre;
    float existencia, conteo, diferencia;
    LayoutInflater inflater;
    public InventarioListaSeriesDiferenciasAdapter series_contadasAdapter, series_nocontadasAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_AppCompat_Light_Dialog);
        //setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
        Bundle bundle = getArguments();
        idalmacen = bundle.getString("idalmacen");
        codigo = bundle.getString("codigo");
        nombre = bundle.getString("desc");
        existencia = bundle.getFloat("exist");
        conteo = bundle.getFloat("cont");
        diferencia = bundle.getFloat("dif");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        this.inflater = inflater;
        View view = inflater.inflate(R.layout.dialog_detalle_series_diferencias, container, false);
        lv_series_contadas = view.findViewById(R.id.lv_series_enconteo);
        lv_series_no = view.findViewById(R.id.lv_series_noconteo);
        tvcodigo = view.findViewById(R.id.tv_codigoArt);
        tvnombre = view.findViewById(R.id.tv_nombreArt);
        tvexistencia = view.findViewById(R.id.tv_exist);
        tvconteo = view.findViewById(R.id.tv_cont);
        tvdiferencia = view.findViewById(R.id.tv_dife);
        tvnoconteo = view.findViewById(R.id.tv_nocontadas);
        tvnodescartado = view.findViewById(R.id.tv_nodescartadas);
        tvnoconteo.setVisibility(View.INVISIBLE);
        tvnoconteo.setVisibility(View.GONE);
        tvnodescartado.setVisibility(View.INVISIBLE);
        tvnodescartado.setVisibility(View.GONE);
        tvcodigo.setText(codigo);
        tvnombre.setText(nombre);
        tvexistencia.setText(Float.toString(existencia));
        tvconteo.setText(Float.toString(conteo));
        tvdiferencia.setText(Float.toString(diferencia));
        btn_aceptar = view.findViewById(R.id.btn_aceptar);
        btn_aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return view;
    }

    public void consultaInicial() {
        Series_contadas = new ArrayList<>();
        Series_nocontadas = new ArrayList<>();
        Database admin = new Database(getActivity(),null,1);
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor fila = db.rawQuery("SELECT * FROM series WHERE almacen = '"+ idalmacen + "' AND codigo = '" + codigo + "'",null);
        if(fila.moveToFirst()) {
            do{
                String id = fila.getString(0);
                String serie = fila.getString(1);
                String codigo = fila.getString(2);
                String almacen = fila.getString(3);
                String estado = fila.getString(4);
                if (estado.equalsIgnoreCase("D")){
                    Series_contadas.add(new Serie(id, serie, codigo, almacen, estado));
                }else Series_nocontadas.add(new Serie(id, serie, codigo, almacen, estado));
            }while (fila.moveToNext());
        }
        else {
            //Toast.makeText(this, "Almacen sin conteo", Toast.LENGTH_SHORT).show();
        }
        db.close();
        series_contadasAdapter = new InventarioListaSeriesDiferenciasAdapter(getActivity(), Series_contadas);
        series_nocontadasAdapter = new InventarioListaSeriesDiferenciasAdapter(getActivity(), Series_nocontadas);
        if (Series_contadas.size() <= 0){
            tvnoconteo.setVisibility(View.VISIBLE);
        }
        if (Series_nocontadas.size() <= 0){
            tvnodescartado.setVisibility(View.VISIBLE);
        }
        lv_series_contadas.setAdapter(series_contadasAdapter);
        lv_series_no.setAdapter(series_nocontadasAdapter);
        justifyListViewHeightBasedOnChildren(lv_series_no);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        consultaInicial();
        if (dialog != null) {
            //int width = ViewGroup.LayoutParams.MATCH_PARENT;
            //int height = ViewGroup.LayoutParams.MATCH_PARENT;
            //dialog.getWindow().setLayout();
        }

        //seriesAdapter.updateRecords(Series);
    }
    public void onResume() {
        // Store access variables for window and blank point
        Window window = getDialog().getWindow();
        Point size = new Point();
        // Store dimensions of the screen in `size`
        Display display = window.getWindowManager().getDefaultDisplay();
        display.getSize(size);
        // Set the width of the dialog proportional to 75% of the screen width
        window.setLayout((size.x), WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
        // Call super onResume after sizing
        super.onResume();
    }

    public static void justifyListViewHeightBasedOnChildren (ListView listView) {

        ListAdapter adapter = listView.getAdapter();

        if (adapter == null) {
            return;
        }
        ViewGroup vg = listView;
        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, vg);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams par = listView.getLayoutParams();
        par.height = totalHeight + (listView.getDividerHeight() * (adapter.getCount() - 1));
        listView.setLayoutParams(par);
        listView.requestLayout();
    }
}
