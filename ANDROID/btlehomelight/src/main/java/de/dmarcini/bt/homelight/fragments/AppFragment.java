package de.dmarcini.bt.homelight.fragments;

import android.support.v4.app.Fragment;

/**
 * Created by dmarc on 24.12.2015.
 */
public class AppFragment extends Fragment
{
  private boolean shouldNewCreated = false;

  public boolean isShouldNewCreated()
  {
    return shouldNewCreated;
  }

  public void setShouldNewCreated(boolean shouldNewCreated)
  {
    this.shouldNewCreated = shouldNewCreated;
  }


}
