package com.snirpoapps.avrprogrammer.controller;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class ConnectThread extends Thread {
	// Unique UUID for this application
	private static final UUID MY_UUID_SECURE = UUID
			.fromString("00001101-0000-1000-8000-00805f9b34fb");
	private static final UUID MY_UUID_INSECURE = UUID
			.fromString("00001101-0000-1000-8000-00805f9b34fb");

	private BluetoothDevice device;
	private BluetoothSocket socket;
	private boolean secure = false;
	private Device controller;
	private boolean active = true;
	private int timeout = 1000;

	public ConnectThread(BluetoothDevice device, boolean secure,
			Device controller) {
		this.device = device;
		this.secure = secure;
		this.controller = controller;
	}

	@Override
	public void run() {
		while (active) {
			Log.v("ControllerConnectThread", "Connecting");

			try {
				if (secure) {
					socket = device
							.createRfcommSocketToServiceRecord(MY_UUID_SECURE);
				} else {
					socket = device
							.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
				}

				socket.connect();
				controller.onConnected(socket);
				return;
			} catch (Exception e) {
				Log.e("ControllerConnectThread", e.getMessage());
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e1) {
					}
				}
			}

			Log.v("ControllerConnectThread",
					"Connection Failed, reconnecting in "
							+ Integer.toString(timeout) + " ms");
			try {
				Thread.sleep(timeout);
			} catch (InterruptedException e) {
			}
			timeout *= 2;
		}
	}

	public void destroy() {
		active = false;
		interrupt();
	}
}
