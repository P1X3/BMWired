package com.osovskiy.bmwinterface;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 7/22/2014.
 */
public class MsgLogActivity extends ListActivity
{
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.msglog_layout);

    final MsgLogAdapter adapter = createAdapter();

    Button updateButton = (Button)findViewById(R.id.msgLogUpdate);
    updateButton.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View view)
      {
        BusMessage.Type[] types = BusMessage.Type.values();
        BusMessage[] newMsgs = new BusMessage[randInt(5,8)];
        for (int i = 0; i < newMsgs.length; i++)
        {
          int selectedtype = randInt(0, types.length-1);
          newMsgs[i] = new BusMessage(types[selectedtype]);
        }

        synchronized (this)
        {
          adapter.addMessages(newMsgs);
        }
      }

      private int randInt(int min, int max)
      {
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
      }
    });


    setListAdapter(adapter);
  }

  private MsgLogAdapter createAdapter()
  {
    return new MsgLogAdapter();
  }

  @Override
  protected void onResume()
  {
    super.onResume();
  }

  @Override
  protected void onPause()
  {
    super.onPause();
  }

  class MsgLogAdapter extends BaseAdapter
  {
    List<BusMessage> tempMessages = new ArrayList<BusMessage>();

    public MsgLogAdapter()
    {
      super();
      tempMessages.add(new BusMessage(BusMessage.Type.MFSW_DIAL_PRESSED));
      tempMessages.add(new BusMessage(BusMessage.Type.MFSW_DIAL_1SEC));
      tempMessages.add(new BusMessage(BusMessage.Type.MFSW_DIAL_RELEASED));
    }

    @Override
    public int getCount()
    {
      return tempMessages.size();
    }

    @Override
    public Object getItem(int i)
    {
      return null;
    }

    @Override
    public long getItemId(int i)
    {
      return 0;
    }

    public void addMessages(BusMessage[] newMessages)
    {
      for (BusMessage m: newMessages)
      {
        tempMessages.add(0, m);
        if (tempMessages.size() > 10)
          tempMessages.remove(tempMessages.size() - 1);
      }
      notifyDataSetChanged();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup)
    {
      LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      view = inflater.inflate(R.layout.msglog_item, null);
      TextView msgLogText = (TextView)view.findViewById(R.id.msgLogText);
      BusMessage msg = tempMessages.get(i);
      //if (msg != null)
        msgLogText.setText(msg.toString());
      return view;
    }
  }
}
