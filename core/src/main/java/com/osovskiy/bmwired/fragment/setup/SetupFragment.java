package com.osovskiy.bmwired.fragment.setup;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.osovskiy.bmwired.R;

import java.util.ArrayList;
import java.util.List;

public class SetupFragment extends Fragment implements AdapterView.OnItemClickListener
{
  List<UsbSerialDriver> listDrivers = new ArrayList<>();
  DriversListAdapter adapter;

  @Override
  public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    setHasOptionsMenu(true);

    View v = inflater.inflate(R.layout.fragment_setup, container, false);
    ListView lv = (ListView) v.findViewById(R.id.lvDevices);
    adapter = new DriversListAdapter(getActivity(), R.layout.list_driver_item, listDrivers);
    lv.setAdapter(adapter);
    lv.setOnItemClickListener(this);

    new UsbDriverLoader(this).execute();

    return v;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
  {
    inflater.inflate(R.menu.main_activity_setup, menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    switch ( item.getItemId() )
    {
      case R.id.setup_refresh:
        new UsbDriverLoader(this).execute();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  {
    final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

    final UsbSerialDriver selectedDriver = listDrivers.get(position);

    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
    alertBuilder.setTitle("Select port");

    ArrayList<String> portStrings = new ArrayList<>();
    for (UsbSerialPort port : selectedDriver.getPorts())
    {
      portStrings.add(port.toString());
    }
    alertBuilder.setItems((String[])portStrings.toArray(), new DialogInterface.OnClickListener()
    {
      @Override
      public void onClick(DialogInterface dialog, int which)
      {
        preferences.edit().putString(getString(R.string.preference_serial_name_key), selectedDriver.getDevice().getDeviceName()).
            putString(getString(R.string.preference_serial_port_key), String.valueOf(which)).apply();
      }
    });
    alertBuilder.show();
  }

  protected void updateUsbDrivers(List<UsbSerialDriver> usbDevices)
  {
    adapter.clear();
    adapter.addAll(usbDevices);
    adapter.notifyDataSetChanged();
    Toast.makeText(getActivity(), listDrivers.size() + " devices found", Toast.LENGTH_SHORT).show();
  }
}
