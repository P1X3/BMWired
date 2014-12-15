package com.osovskiy.bmwired.fragment.setup;

import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.osovskiy.bmwired.R;

import java.util.List;

class DriversListAdapter extends ArrayAdapter<UsbSerialDriver>
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
