package com.osovskiy.bmwired.fragment.setup;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.util.List;

class UsbDriverLoader extends AsyncTask<Void, Void, List<UsbSerialDriver>>
{
  private SetupFragment setupFragment;

  public UsbDriverLoader(SetupFragment setupFragment)
  {
    this.setupFragment = setupFragment;
  }

  @Override
  protected List<UsbSerialDriver> doInBackground(Void... params)
  {
    UsbManager manager = (UsbManager) setupFragment.getActivity().getSystemService(Context.USB_SERVICE);
    return UsbSerialProber.getDefaultProber().findAllDrivers(manager);
  }

  @Override
  protected void onPostExecute(final List<UsbSerialDriver> drivers)
  {
    setupFragment.updateUsbDrivers(drivers);
  }
}
