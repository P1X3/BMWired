package com.osovskiy.bmwinterface.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.osovskiy.bmwinterface.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 8/22/2014.
 */
public class Setup extends Fragment
{
  List<String> listDriversString = new ArrayList<>();
  List<UsbSerialDriver> listDrivers = new ArrayList<>();
  DriversListAdapter adapter;

  @Override
  public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    View v = inflater.inflate(R.layout.fragment_setup, container, false);
    ListView lv = (ListView) v.findViewById(R.id.lvDevices);
    adapter = new DriversListAdapter(getActivity(), R.layout.list_driver_item, listDrivers);
    lv.setAdapter(adapter);

    new LoadUSBDevices().execute();

    return v;
  }

  private class LoadUSBDevices extends AsyncTask<Void, Void, Void>
  {

    @Override
    protected Void doInBackground(Void... params)
    {
      UsbManager manager = (UsbManager) getActivity().getSystemService(Context.USB_SERVICE);
      listDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
      listDriversString.clear();

      for ( UsbSerialDriver driver : listDrivers )
      {
        listDriversString.add(driver.getDevice().getDeviceName());
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void aVoid)
    {
      adapter.notifyDataSetChanged();
      Toast.makeText(getActivity(), "Devices list updated. Devices: " + listDrivers.size(), Toast.LENGTH_SHORT).show();
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
