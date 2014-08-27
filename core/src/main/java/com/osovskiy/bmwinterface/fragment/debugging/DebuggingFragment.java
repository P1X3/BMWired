package com.osovskiy.bmwinterface.fragment.debugging;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.osovskiy.bmwinterface.R;

/**
 * Created by Administrator on 8/27/2014.
 */
public class DebuggingFragment extends Fragment
{
  @Override
  public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    View v = inflater.inflate(R.layout.fragment_debugging, container, false);
    return v;
  }
}
