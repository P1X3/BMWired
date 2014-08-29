package com.osovskiy.bmwinterface.fragment.debugging;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
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

import com.osovskiy.bmwinterface.BMWiService;
import com.osovskiy.bmwinterface.R;
import com.osovskiy.bmwinterface.lib.BusMessage;
import com.osovskiy.bmwinterface.lib.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 8/27/2014.
 */
public class DebuggingFragment extends Fragment implements AdapterView.OnItemSelectedListener, View.OnClickListener
{
  List<DebugBusMessage> messages = new ArrayList<>();
  Messenger messenger;
  ListView listView;
  Button sendToBus, sendFromService;
  CheckBox sendBroadcast;

  BusMessage selectedMsg;

  @Override
  public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    messenger = getArguments().getParcelable(Messenger.class.getSimpleName());
    View v = inflater.inflate(R.layout.fragment_debugging, container, false);
    listView = (ListView)v.findViewById(R.id.lvDebuggingMessages);
    sendToBus = (Button)v.findViewById(R.id.buttonSendToBus);
    sendFromService = (Button)v.findViewById(R.id.buttonSendFromService);
    sendBroadcast = (CheckBox)v.findViewById(R.id.checkboxBroadcast);

    sendToBus.setOnClickListener(this);
    sendFromService.setOnClickListener(this);

    BusMessageAdapter adapter = new BusMessageAdapter(getActivity(), android.R.layout.simple_list_item_1, messages);
    listView.setAdapter(adapter);
    listView.setOnItemSelectedListener(this);

    DebugBusMessage debugBusMessage = new DebugBusMessage(new BusMessage(BusMessage.BusDevice.Broadcast, BusMessage.BusDevice.CD, new byte[] {}));
    debugBusMessage.setDescription("Dummy");

    messages.add(debugBusMessage);

    selectedMsg = messages.get(0);

    return v;
  }

  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
  {
    selectedMsg = messages.get(position);
  }

  @Override
  public void onNothingSelected(AdapterView<?> parent)
  {

  }

  @Override
  public void onClick(View v)
  {
    switch (v.getId())
    {
      case R.id.buttonSendToBus:
        if (sendBroadcast.isChecked())
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
            Message msg = Message.obtain(null, BMWiService.MSG_SENDTO_BUS);
            msg.getData().putParcelable(BusMessage.class.getSimpleName(), selectedMsg);
            messenger.send(msg);
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
          Message msg = Message.obtain(null, BMWiService.MSG_SENDFROM_BUS);
          msg.getData().putParcelable(BusMessage.class.getSimpleName(), selectedMsg);
          messenger.send(msg);
        }
        catch ( RemoteException e )
        {
          e.printStackTrace();
        }

        Toast.makeText(getActivity(), "Send from service", Toast.LENGTH_SHORT).show();
        break;
    }
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
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        convertView = inflater.inflate(resource, parent, false);
      }

      DebugBusMessage debugBusMessage = objects.get(position);

      (( TextView)convertView.findViewById(android.R.id.text1)).setText(objects.get(position).toString());

      return convertView;
    }
  }
}
