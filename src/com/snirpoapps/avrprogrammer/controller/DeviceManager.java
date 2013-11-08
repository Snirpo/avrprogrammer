package com.snirpoapps.avrprogrammer.controller;

import java.io.File;
import java.util.ArrayList;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

public class DeviceManager {
	private ArrayList<Device> controllers = new ArrayList<Device>();
	private BluetoothAdapter bluetoothAdapter;
	private BluetoothBroadcastReceiver bluetoothBroadcastReceiver;
	private ArrayList<ArduinoProgrammerListener> programmerListeners = new ArrayList<ArduinoProgrammerListener>();
	private ArrayList<ConnectionListener> connectionListeners = new ArrayList<ConnectionListener>();
	private ArrayList<DiscoveryListener> discoveryListeners = new ArrayList<DiscoveryListener>();

	private Handler handler = new Handler();
	private Context context;

	public DeviceManager(Context context) {
		this.context = context;
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	}

	public void destroy() {
		stopDiscovery();
		disconnectAll();
	}

	public void startDiscovery() {
		if (bluetoothBroadcastReceiver == null) {
			bluetoothBroadcastReceiver = new BluetoothBroadcastReceiver();

			context.registerReceiver(bluetoothBroadcastReceiver,
					new IntentFilter(BluetoothDevice.ACTION_FOUND));

			context.registerReceiver(
					bluetoothBroadcastReceiver,
					new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
		}

		if (bluetoothAdapter.isDiscovering())
			bluetoothAdapter.cancelDiscovery();

		bluetoothAdapter.startDiscovery();
	}

	public void stopDiscovery() {
		if (bluetoothAdapter.isDiscovering())
			bluetoothAdapter.cancelDiscovery();

		if (bluetoothBroadcastReceiver != null) {
			context.unregisterReceiver(bluetoothBroadcastReceiver);
			bluetoothBroadcastReceiver = null;
		}
	}
	
	public boolean isDiscovering(){
		return bluetoothAdapter.isDiscovering();
	}

	public void connect(Device controller) {
		synchronized (controllers) {
			controller.connect();
			controllers.add(controller);
		}
	}

	public void connect(String macAddress) {
		if (BluetoothAdapter.checkBluetoothAddress(macAddress)) {
			Device device = new Device(
					bluetoothAdapter.getRemoteDevice(macAddress), this);
			connect(device);
		}
	}

	public void disconnectAll() {
		synchronized (controllers) {
			for (Device controller : controllers)
				controller.disconnect();
		}
	}

	public int getCount() {
		synchronized (controllers) {
			return controllers.size();
		}
	}

	public Device getController(int index) {
		synchronized (controllers) {
			if (index < controllers.size())
				return controllers.get(index);
			return null;
		}
	}

	// public void sendCommand(MWCommand command) {
	// synchronized (controllers) {
	// for (MultiWiiDevice controller : controllers)
	// controller.sendCommand(command);
	// }
	// }

	public void onAsyncConnected(final Device controller) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				for (ConnectionListener listener : connectionListeners)
					listener.onMultiWiiConnected(controller);
			}
		});
	}

	public void onAsyncUpdate(final Device controller) {
		// listener.onThreadMessage(controller, message);
		handler.post(new Runnable() {
			@Override
			public void run() {
				for (ConnectionListener listener : connectionListeners)
					listener.onMultiWiiUpdate(controller);
			}
		});
	}

	public void onAsyncError(final Device controller, final String message) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				for (ConnectionListener listener : connectionListeners)
					listener.onMultiWiiError(controller, 0);
			}
		});
	}

	public void onAsyncProgrammerError(final Device device, final int state) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				for (ArduinoProgrammerListener listener : programmerListeners)
					listener.onProgrammerError(device, state);
			}
		});
	}

	public void onAsyncProgrammerProgressUpdate(final Device device,
			final int state, final int progress) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				for (ArduinoProgrammerListener listener : programmerListeners)
					listener.onProgrammerProgressUpdate(device, state, progress);
			}
		});
	}

	public void registerProgrammerListener(ArduinoProgrammerListener listener) {
		programmerListeners.add(listener);
	}

	public void unregisterProgrammerListener(ArduinoProgrammerListener listener) {
		programmerListeners.remove(listener);
	}

	public void registerConnectionListener(ConnectionListener listener) {
		connectionListeners.add(listener);
	}

	public void unregisterConnectionListener(ConnectionListener listener) {
		connectionListeners.remove(listener);
	}

	public void registerDiscoveryListener(DiscoveryListener listener) {
		discoveryListeners.add(listener);
	}

	public void unregisterDiscoveryListener(DiscoveryListener listener) {
		discoveryListeners.remove(listener);
	}

	public interface ConnectionListener {
		public void onMultiWiiUpdate(Device controller);

		public void onMultiWiiError(Device controller, int message);

		public void onMultiWiiConnected(Device controller);
	}

	public interface DiscoveryListener {
		public void onDiscovered(Device controller);

		public void onDiscoveryFinished();
	}

	public interface ArduinoProgrammerListener {
		public void onProgrammerError(Device device, int state);

		public void onProgrammerProgressUpdate(Device device, int state,
				int progress);
	}

	public class BluetoothBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// If it's already paired, skip it, because it's been listed
				// already
				// if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
				String name = device.getName();
				if (name != null)
					for (DiscoveryListener listener : discoveryListeners)
						listener.onDiscovered(new Device(device,
								DeviceManager.this));
				// }
				// When discovery is finished, change the Activity title
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				for (DiscoveryListener listener : discoveryListeners)
					listener.onDiscoveryFinished();
				stopDiscovery();
			}
		}
	}
}
