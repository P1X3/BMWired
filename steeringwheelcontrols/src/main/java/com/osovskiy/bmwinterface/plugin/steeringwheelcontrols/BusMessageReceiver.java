package com.osovskiy.bmwinterface.plugin.steeringwheelcontrols;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.view.KeyEvent;

import com.osovskiy.bmwinterface.lib.BusMessage;

public class BusMessageReceiver extends BroadcastReceiver
{


  @Override
  public void onReceive(Context context, Intent intent)
  {
    BusMessage busMessage = intent.getParcelableExtra(BusMessage.class.getSimpleName());

    if ( busMessage.getSource() == BusMessage.BusDevice.MFL && busMessage.getDestination() == BusMessage.BusDevice.RAD )
    {
      byte[] payload = busMessage.getPayload();

      switch ( payload[0] )
      {
        // Volume
        case 0x32:
          AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
          if ( payload[1] == 0x11 )
          {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
          }
          else if ( payload[1] == 0x10 )
          {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
          }
          break;
        // Previous/Next/RT/Dial
        case 0x3B:
          Intent mediaIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
          KeyEvent keyEvent = null;
          switch ( payload[1] )
          {
            // Next
            case 0x01: // Press
              keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT);
              break;
            case 0x11: // Hold
              break;
            case 0x21: // Release
              keyEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT);
              break;

            // Previous
            case 0x08: // Press
              keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS);
              break;
            case 0x18: // Hold
              break;
            case 0x28: // Release
              keyEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PREVIOUS);
              break;

            // Dial
            case (byte) 0x80: // Press
              break;
            case (byte) 0x90: // Hold
              break;
            case (byte) 0xA0: // Release
              break;
          }
          if ( keyEvent != null )
          {
            mediaIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
            context.sendBroadcast(mediaIntent);
          }
          break;
      }
    }
  }
}
