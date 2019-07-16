package app.arisoft_app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Vector;

public class SelectExistenciaActivity extends AppCompatActivity {
  private ListView lv1;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_select_existencia);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    if (getSupportActionBar() != null){
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    Vector<String> select = new Vector<>();
    select.addElement("Existencia general");//0
    select.addElement("Existencia por almacen");//1
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, select);
    lv1 =(ListView)findViewById(R.id.lv_select_exis);
    lv1.setAdapter(adapter);

    lv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView adapterView, View view, int i, long l) {
        int selected = i;
        if (i == 0) {
          //IR A ACTIVITY EXISTENCIA GENERAL
          Intent intent = new Intent(getApplicationContext(),ArticulosExistenciaGeneralActivity.class);
          startActivity(intent);
        } else {
          //IR A ARTICULOSEXIETNCIAACTIVITY
          Intent intent = new Intent(getApplicationContext(),ArticulosExistenciaAlmacenActivity.class);
          startActivity(intent);
        }
      }
    });
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // handle arrow click here
    if (item.getItemId() == android.R.id.home) {
      finish(); // close this activity and return to preview activity (if there is any)
    }

    return super.onOptionsItemSelected(item);
  }
}
