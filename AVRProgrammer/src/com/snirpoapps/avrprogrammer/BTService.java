package com.snirpoapps.avrprogrammer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.snirpoapps.avrprogrammer.controller.DeviceManager;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class BTService extends Service {
	private DeviceManager deviceManager;
	private ServiceBinder binder = new ServiceBinder();

	@Override
	public void onCreate() {
		Log.v("ConnectionService", "Created");
		deviceManager = new DeviceManager(this);
	}

	public DeviceManager getDeviceManager() {
		return deviceManager;
	}

	@Override
	public void onDestroy() {
		deviceManager.destroy();
		Log.v("ConnectionService", "Destroyed");
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	public class ServiceBinder extends Binder {
		public BTService getService() {
			return BTService.this;
		}
	}
}
