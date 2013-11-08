package com.snirpoapps.avrprogrammer;

import java.util.ArrayList;

import com.snirpoapps.avrprogrammer.BTService.ServiceBinder;
import com.snirpoapps.avrprogrammer.controller.Device;
import com.snirpoapps.avrprogrammer.controller.DeviceManager;
import com.snirpoapps.avrprogrammer.controller.DeviceManager.ArduinoProgrammerListener;
import com.snirpoapps.avrprogrammer.controller.DeviceManager.ConnectionListener;
import com.snirpoapps.avrprogrammer.controller.DeviceManager.DiscoveryListener;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RecoverySystem.ProgressListener;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Activity implements ServiceConnection,
		ArduinoProgrammerListener, DiscoveryListener {
	private BTService btService;
	private Button discoverButton;
	private ListView deviceListView;
	private DeviceAdapter deviceAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		discoverButton = (Button) findViewById(R.id.discoverButton);
		discoverButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (btService != null) {
					if (!btService.getDeviceManager().isDiscovering()) {
						btService.getDeviceManager().startDiscovery();
						deviceAdapter.clear();
						discoverButton.setText("Stop discovery");
					} else {
						btService.getDeviceManager().stopDiscovery();
						discoverButton.setText("Start discovery");
					}
				}
			}
		});

		deviceAdapter = new DeviceAdapter();
		deviceListView = (ListView) findViewById(R.id.deviceListView);
		deviceListView.setAdapter(deviceAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onStart() {
		Intent intent = new Intent(this, BTService.class);
		bindService(intent, this, Context.BIND_AUTO_CREATE);
		super.onStart();
	}

	@Override
	protected void onStop() {
		if (btService != null) {
			DeviceManager manager = btService.getDeviceManager();
			manager.unregisterProgrammerListener(this);
			manager.unregisterDiscoveryListener(this);
			btService = null;
		}
		super.onStop();
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder binder) {
		btService = ((ServiceBinder) binder).getService();
		DeviceManager manager = btService.getDeviceManager();
		manager.registerProgrammerListener(this);
		manager.registerDiscoveryListener(this);
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {

	}

	@Override
	public void onProgrammerError(Device device, int state) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProgrammerProgressUpdate(Device device, int state,
			int progress) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDiscovered(Device controller) {
		deviceAdapter.add(controller);
	}

	@Override
	public void onDiscoveryFinished() {
		discoverButton.setText("Start discovery");
	}

	public class DeviceAdapter extends BaseAdapter {
		private ArrayList<Device> devices = new ArrayList<Device>();

		public void clear() {
			devices.clear();
			notifyDataSetChanged();
		}

		public void add(Device device) {
			devices.add(device);
			notifyDataSetChanged();
		}

		public void remove(Device device) {
			devices.remove(device);
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return devices.size();
		}

		@Override
		public Object getItem(int position) {
			return devices.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView v = (TextView) convertView;
			if (v == null)
				v = new TextView(MainActivity.this);
			Device d = (Device) getItem(position);
			v.setText(d.getName() + " " + d.getMacAddress());
			return v;
		}

	}

}
