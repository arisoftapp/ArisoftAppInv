package app.arisoft_app;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import app.arisoft_app.Tools.ISelectedData;

public class FragmentOpcionesDiferenciasDialog extends DialogFragment {
    RadioGroup radioGroup, cont_opc, alm_opc;
    RadioButton solo_cont, cont_todo, cont_dif, todos_alm, alm_todo, alm_dif;
    LayoutInflater inflater;
    Button btn_aceptar;
    private ISelectedData mCallback;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        this.inflater = inflater;
        View v = inflater.inflate(R.layout.dialog_opc_diferencias, container, false);
        radioGroup = v.findViewById(R.id.opc_group);
        cont_opc = v.findViewById(R.id.conteo_opc);
        alm_opc = v.findViewById(R.id.alm_opc);
        solo_cont = v.findViewById(R.id.rb_solo_cont);
        cont_todo = v.findViewById(R.id.cont_todo);
        cont_dif = v.findViewById(R.id.cont_dif);
        todos_alm = v.findViewById(R.id.rb_todos_alm);
        alm_todo = v.findViewById(R.id.alm_todo);
        alm_dif = v.findViewById(R.id.alm_dif);
        alm_todo.setEnabled(false);
        alm_dif.setEnabled(false);
        solo_cont.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    cont_todo.setEnabled(isChecked);
                    cont_dif.setEnabled(isChecked);
                    alm_todo.setEnabled(!isChecked);
                    alm_dif.setEnabled(!isChecked);
                }
            }
        });

        btn_aceptar = v.findViewById(R.id.btn_aceptar);
        btn_aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //dismiss();
                String params = getValues();
                mCallback.onSelectedData(params);
                dismiss();
            }
        });

        todos_alm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    cont_todo.setEnabled(!isChecked);
                    cont_dif.setEnabled(!isChecked);
                    alm_todo.setEnabled(isChecked);
                    alm_dif.setEnabled(isChecked);
                }
            }
        });

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        if (dialog != null) {
            //int width = ViewGroup.LayoutParams.MATCH_PARENT;
            //int height = ViewGroup.LayoutParams.MATCH_PARENT;
            //dialog.getWindow().setLayout();
        }
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

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (ISelectedData) activity;
        }
        catch (ClassCastException e) {
            Log.d("MyDialog", "Activity doesn't implement the ISelectedData interface");
        }
    }

    public String getValues(){
        String param = "";
        if (solo_cont.isChecked()){
            if (cont_todo.isChecked()){
                param = "00";
            }else param = "01";
        }else{
            if (alm_todo.isChecked()){
                param = "10";
            }else param = "11";
        }
        Toast.makeText(getActivity(), "PARAM" + param, Toast.LENGTH_SHORT).show();
        return param;
    }


}
