package com.example.testforbd;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootStartUpReciever extends BroadcastReceiver {
	final String LOG_TAG = "BootStartUpReciever";

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO: This method is called when the BroadcastReceiver is receiving

		// Start Service On Boot Start Up
		Intent service = new Intent(context, MainService.class);
		context.startService(service);

		Boolean bAsReboot = MainActivity.getSP().getBoolean("chbAsReboot", false);
		if (bAsReboot) {
			// Start App On Boot Start Up
			Intent App = new Intent(context, MainFragment.class);
			App.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			context.startActivity(App);
		}
		Log.d(LOG_TAG, "onReceive chbAsReboot=" + bAsReboot);
	}
}
