package app.arisoft_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class exialmAdapter extends BaseAdapter{

    private ArrayList<ExiAlmancen> listItem;
    private Context context;

    public exialmAdapter(ArrayList<ExiAlmancen> listItem, Context context) {
        this.listItem = listItem;
        this.context = context;
    }

    @Override
    public int getCount() {
        return listItem.size();
    }

    @Override
    public Object getItem(int position) {
        return listItem.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ExiAlmancen item=(ExiAlmancen) getItem(position);
        convertView= LayoutInflater.from(context).inflate(R.layout.lista_alm_exi_item,null);
        TextView tv_alm=convertView.findViewById(R.id.tv_almacen);
        TextView tv_exi=convertView.findViewById(R.id.tv_exi);
        tv_alm.setText(item.getIdalmacen()+" - "+item.getAlmacen());
        tv_exi.setText(item.getExistencia());

        return convertView;
    }
}
