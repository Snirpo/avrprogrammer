package com.snirpoapps.avrprogrammer.controller;

import java.io.InputStream;

import com.snirpoapps.avrprogrammer.model.BluetoothMessage;

import android.util.Log;

public class ReceiveThread extends Thread {
	private boolean active = true;
	private InputStream inputStream;
	private Device controller;

	public ReceiveThread(InputStream inputStream, Device controller) {
		this.inputStream = inputStream;
		this.controller = controller;
	}

	@Override
	public void run() {
		Log.v("Thread", "ReceiveThread Started");

		BluetoothMessage message = null;
		byte[] buffer = new byte[256];

		try {
			while (active) {
				if ((inputStream.read(buffer, 0, 1)) != -1) {
					message = controller.onCreateMessage(buffer[0]);

					if (message != null) {
						int size = message.getSize();
						int total = 0;
						int count = 0;
						while ((count = inputStream.read(buffer, total, size
								- total)) != -1) {
							total += count;
							if (total == size) {
								message.fromBytes(buffer);
								controller.onMessage(message);
								// Log.v("MESSAGE", message.toString());
								break;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			Log.v("RECEIVE", e.toString());
			controller.onError(e.getMessage());
		}

		Log.v("Thread", "ReceiveThread Stopped");
	}

	public void destroy() {
		active = false;
	}

}
