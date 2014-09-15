package com.osovskiy.bmwired.fragment.debugging;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.osovskiy.bmwired.R;
import com.osovskiy.bmwired.lib.BusMessage;
import com.osovskiy.bmwired.lib.IBMWiService;
import com.osovskiy.bmwired.lib.IBMWiServiceCallback;
import com.osovskiy.bmwired.lib.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DebuggingFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener
{
  private static String TAG = DebuggingFragment.class.getSimpleName();
  List<DebugBusMessage> messages = new ArrayList<>();
  BusMessageAdapter adapter;

  ListView listView;
  Button sendToBus, sendFromService, sendOpenBusInterface, sendCloseBusInterface;
  CheckBox sendBroadcast;
  BusMessage selectedMsg;
  private IBMWiService service = null;

  private IBMWiServiceCallback.Stub serviceCallback = new IBMWiServiceCallback.Stub()
  {
    @Override
    public void newMessageFromBus(BusMessage msg) throws RemoteException
    {
      // TODO: Handle new message received from the bus, hop the thread!!!
    }
  };

  @Override
  public void onStart()
  {
    super.onStart();
    getActivity().bindService(new Intent(getActivity(), IBMWiService.class), serviceConnection, 0);
  }

  @Override
  public void onStop()
  {
    try
    {
      service.unregisterCallback(serviceCallback);
    }
    catch ( RemoteException e )
    {
      e.printStackTrace();
    }
    getActivity().unbindService(serviceConnection);
  }

  private ServiceConnection serviceConnection = new ServiceConnection()
  {
    @Override
    public void onServiceConnected(ComponentName name, IBinder s)
    {
      Log.d(TAG, "Connected to the service");
      service = IBMWiService.Stub.asInterface(s);
      try
      {
        service.registerCallback(serviceCallback);
      }
      catch ( RemoteException e )
      {
        e.printStackTrace();
      }
    }

    @Override
    public void onServiceDisconnected(ComponentName name)
    {
      Log.d(TAG, "Disconnected from the service");
      service = null;
    }
  };

  @Override
  public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    View v = inflater.inflate(R.layout.fragment_debugging, container, false);
    listView = (ListView) v.findViewById(R.id.lvDebuggingMessages);
    sendToBus = (Button) v.findViewById(R.id.buttonSendToBus);
    sendFromService = (Button) v.findViewById(R.id.buttonSendFromService);
    sendBroadcast = (CheckBox) v.findViewById(R.id.checkboxBroadcast);
    sendOpenBusInterface = (Button) v.findViewById(R.id.buttonSendOpenBusInterface);
    sendCloseBusInterface = (Button) v.findViewById(R.id.buttonSendCloseBusInterface);

    sendToBus.setOnClickListener(this);
    sendFromService.setOnClickListener(this);
    sendOpenBusInterface.setOnClickListener(this);
    sendCloseBusInterface.setOnClickListener(this);

    adapter = new BusMessageAdapter(getActivity(), android.R.layout.simple_list_item_1, messages);
    listView.setAdapter(adapter);
    listView.setOnItemClickListener(this);

    new LoadDebugMessages().execute();

    return v;
  }

  @Override
  public void onClick(View v)
  {
    switch ( v.getId() )
    {
      case R.id.buttonSendToBus:
        if ( sendBroadcast.isChecked() )
        {
          Intent intent = new Intent(Utils.ACTION_SEND_BUS_MESSAGE);
          intent.putExtra(BusMessage.class.getSimpleName(), selectedMsg);

          getActivity().sendBroadcast(selectedMsg.getIntent(Utils.ACTION_SEND_BUS_MESSAGE));
          //getActivity().sendBroadcast(intent, Utils.PERMISSION_SEND_MESSAGE);
          Toast.makeText(getActivity(), "Send to bus using broadcast", Toast.LENGTH_SHORT).show();
        }
        else
        {
          try
          {
            service.sendMessageToBus(selectedMsg);
          }
          catch ( RemoteException e )
          {
            e.printStackTrace();
          }
          Toast.makeText(getActivity(), "Send to bus using bind", Toast.LENGTH_SHORT).show();
        }
        break;
      case R.id.buttonSendFromService:
        try
        {
          service.sendMessageFromBus(selectedMsg);
        }
        catch ( RemoteException e )
        {
          e.printStackTrace();
        }

        Toast.makeText(getActivity(), "Send from service", Toast.LENGTH_SHORT).show();
        break;
    }
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  {
    selectedMsg = messages.get(position);
  }

  private class BusMessageAdapter extends ArrayAdapter<DebugBusMessage>
  {
    private Context context;
    private int resource;
    private List<DebugBusMessage> objects;

    public BusMessageAdapter(Context context, int resource, List<DebugBusMessage> objects)
    {
      super(context, resource, objects);
      this.context = context;
      this.resource = resource;
      this.objects = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
      if ( convertView == null )
      {
        LayoutInflater inflater = ( (Activity) context ).getLayoutInflater();
        convertView = inflater.inflate(resource, parent, false);
      }

      DebugBusMessage debugBusMessage = objects.get(position);

      ( (TextView) convertView.findViewById(android.R.id.text1) ).setText(debugBusMessage.toString());

      return convertView;
    }
  }

  private class LoadDebugMessages extends AsyncTask<Void, Void, Void>
  {

    @Override
    protected Void doInBackground(Void... params)
    {
      AssetManager assetManager = getActivity().getAssets();
      try
      {
        BufferedReader br = new BufferedReader(new InputStreamReader(assetManager.open("debug_messages.txt")));

        String line;
        while ( ( line = br.readLine() ) != null )
        {
          if ( line.startsWith("#") )
            continue;

          String[] cols = line.split(":");

          BusMessage.BusDevice source = BusMessage.BusDevice.tryParse((byte) ( Integer.parseInt(cols[1], 16) ));
          BusMessage.BusDevice destination = BusMessage.BusDevice.tryParse((byte) ( Integer.parseInt(cols[2], 16) ));
          byte[] payload = Utils.hexStringToByteArray(cols[3]);

          DebugBusMessage debugBusMessage = new DebugBusMessage(source, destination, payload);
          debugBusMessage.setDescription(cols[0]);

          messages.add(debugBusMessage);
        }
      }
      catch ( IOException e )
      {
        e.printStackTrace();
      }

      return null;
    }

    @Override
    protected void onPostExecute(Void aVoid)
    {
      super.onPostExecute(aVoid);
      adapter.notifyDataSetChanged();
    }
  }
}
