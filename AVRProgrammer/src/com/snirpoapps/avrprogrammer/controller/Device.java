package com.snirpoapps.avrprogrammer.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.snirpoapps.avrprogrammer.controller.DeviceManager.ArduinoProgrammerListener;
import com.snirpoapps.avrprogrammer.model.BluetoothCommand;
import com.snirpoapps.avrprogrammer.model.BluetoothMessage;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

public class Device {
	private static final int STATE_IDLE = 0;
	private static final int STATE_CONNECTING = 1;
	private static final int STATE_CONNECTED = 2;

	private ConnectThread connectThread;
	private ReceiveThread receiveThread;
	private SendThread sendThread;
	private BluetoothSocket socket;
	private BluetoothDevice device;
	private DeviceManager service;
	private int state = STATE_IDLE;
	private Programmer programmer;

	public Device(BluetoothDevice device, DeviceManager service) {
		this.device = device;
		this.service = service;
	}

	public String getName() {
		if (device != null)
			return device.getName();
		return null;
	}

	public String getMacAddress() {
		if (device != null)
			return device.getAddress();
		return null;
	}

	public synchronized void connect() {
		// if (socket == null) {
		connectThread = new ConnectThread(device, true, this);
		connectThread.start();
		state = STATE_CONNECTING;
		// }
	}

	public synchronized void disconnect() {
		try {
			if (socket != null)
				socket.close();
			if (connectThread != null)
				connectThread.destroy();
			if (receiveThread != null)
				receiveThread.destroy();
			if (sendThread != null)
				sendThread.destroy();
			socket = null;
		} catch (IOException e) {
		}
		state = STATE_IDLE;
	}

	public void onConnected(BluetoothSocket socket) {
		connectThread = null;
		this.socket = socket;
		Log.v("Thread", "NEW SOCKET");

		try {
			InputStream inputStream = socket.getInputStream();
			OutputStream outputStream = socket.getOutputStream();

			receiveThread = new ReceiveThread(inputStream, this);
			receiveThread.start();
			sendThread = new SendThread(outputStream, this);
			sendThread.start();

			state = STATE_CONNECTED;
			service.onAsyncConnected(this);
		} catch (Exception e) {
			socket = null;
			connect();
		}
	}

	public synchronized void onError(String message) {
		if (state == STATE_CONNECTED) {
			disconnect();
			connect();
			service.onAsyncError(this, message);
		}
	}

	public void program(File hexFile) {
		programmer = new Programmer(hexFile, this);
		programmer.start();
	}

	public boolean isConnected() {
		return socket != null;
	}

	public void onMessage(BluetoothMessage message) {
		if (programmer != null)
			programmer.addMessage(message);
	}

	public void send(BluetoothCommand command) {
		sendThread.send(command);
	}

	public BluetoothMessage onCreateMessage(byte b) {
		if (programmer != null)
			programmer.getMessageType(b);
		return null;
	}

	public void onProgrammerError(int state) {
		service.onAsyncProgrammerError(this, state);
	}

	public void onProgrammerProgressUpdate(int state, int progress) {
		service.onAsyncProgrammerProgressUpdate(this, state, progress);
	}
}
