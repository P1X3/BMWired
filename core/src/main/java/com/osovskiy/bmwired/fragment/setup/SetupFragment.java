package com.osovskiy.bmwired.fragment.setup;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.osovskiy.bmwired.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 8/22/2014.
 */
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

    new LoadUSBDevices().execute();

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
      case R.id.actions_refresh:
        new LoadUSBDevices().execute();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

    UsbSerialDriver selectedDriver = listDrivers.get(position);
    preferences.edit().putString("serial_name", selectedDriver.getDevice().getDeviceName());
  }

  private class LoadUSBDevices extends AsyncTask<Void, Void, Void>
  {

    @Override
    protected Void doInBackground(Void... params)
    {
      UsbManager manager = (UsbManager) getActivity().getSystemService(Context.USB_SERVICE);
      listDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
      return null;
    }

    @Override
    protected void onPostExecute(Void aVoid)
    {
      adapter.notifyDataSetChanged();
    }
  }

  private class DriversListAdapter extends ArrayAdapter<UsbSerialDriver>
  {
    List<UsbSerialDriver> objects;
    int resourceId;
    Context context;

    public DriversListAdapter(Context context, int resource, List<UsbSerialDriver> objects)
    {
      super(context, resource, objects);
      this.resourceId = resource;
      this.objects = objects;
      this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
      if ( convertView == null )
      {
        LayoutInflater inflater = ( (Activity) context ).getLayoutInflater();
        convertView = inflater.inflate(resourceId, parent, false);
      }

      UsbDevice driver = objects.get(position).getDevice();
      ( (TextView) convertView.findViewById(R.id.textDriverName) ).setText(driver.getDeviceName());
      ( (TextView) convertView.findViewById(R.id.textVendId) ).setText(driver.getVendorId());
      ( (TextView) convertView.findViewById(R.id.textProdId) ).setText(driver.getProductId());

      return convertView;
    }
  }
}
