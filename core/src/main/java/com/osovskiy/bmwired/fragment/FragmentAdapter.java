package com.osovskiy.bmwired.fragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;
import java.util.Map;

public class FragmentAdapter extends FragmentPagerAdapter
{
  FragmentManager fragmentManager;
  List<Map.Entry<String, Fragment>> fragments;

  public FragmentAdapter(FragmentManager fm, List<Map.Entry<String, Fragment>> fragments)
  {
    super(fm);
    this.fragmentManager = fm;
    this.fragments = fragments;
  }

  @Override
  public Fragment getItem(int i)
  {
    return fragments.get(i).getValue();
  }

  @Override
  public int getCount()
  {
    return fragments.size();
  }

  @Override
  public CharSequence getPageTitle(int i)
  {
    return fragments.get(i).getKey();
  }
}