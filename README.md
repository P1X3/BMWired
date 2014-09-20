## About
BMWired is core application that acts as a gateway between BMW K/I-Bus and plugins. Plugins that satisfy requirments listed below can receive and send messages to/from the bus.

## Plugin Implementation
All plugins must include library module as a dependency for receiving and/or sending messages.
Add ```<meta-data android:name="plugin_author" android:value="John Doe"/>``` to `AndroidManifest.xml`

### Receiving
1. Add permission to AndroidManifest.xml
```
<uses-permission android:name="com.osovskiy.bmwinterface.permission.RECEIVE_MSG_PERMISSION" />
```
2. Create `BroadcastReceiver` with intent-filter for action `<action android:name="com.osovskiy.bmwinterface.ACTION_NEW_BUS_MESSAGE" />`
3. In BroadcastReceiver retreive BusMessage from extras using key `BusMessage.class.getSimpleName()`
```
public void onReceive(Context context, Intent intent)
{
  BusMessage msg = intent.getParcelableExtra(BusMessage.class.getSimpleName());
  Toast.makeText(context, "New message received: " + msg.toString(), Toast.LENGTH_SHORT).show();
}
```

### Sending
1. Add permission to AndroidManifest.xml
```
<uses-permission android:name="com.osovskiy.bmwinterface.permission.SEND_MSG_PERMISSION" />
```
2. Create a valid `BusMessage`
```
BusMessage msg = new BusMessage(BusMessage.BusDevice.Broadcast, BusMessage.BusDevice.CD, new byte[] { 0x00, 0x0C, (byte)0xFB, 0x01 });
```
3. Send broadcast
```
sendBroadcast(msg.getIntent(Utils.ACTION_SEND_BUS_MESSAGE), Utils.PERMISSION_SEND_MESSAGE);
```

## Building
Add [usb-serial-for-android](https://github.com/mik3y/usb-serial-for-android) to project dependency list. Build. (maven coming soon)

