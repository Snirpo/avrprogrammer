package com.snirpoapps.avrprogrammer.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

import com.snirpoapps.avrprogrammer.model.BluetoothMessage;
import com.snirpoapps.avrprogrammer.model.IntelHexRecord;
import com.snirpoapps.avrprogrammer.model.ProgrammerEnterProgmodeCommand;
import com.snirpoapps.avrprogrammer.model.ProgrammerGetSyncCommand;
import com.snirpoapps.avrprogrammer.model.ProgrammerInSyncMessage;
import com.snirpoapps.avrprogrammer.model.ProgrammerLeaveProgmodeCommand;
import com.snirpoapps.avrprogrammer.model.ProgrammerLoadAddressCommand;
import com.snirpoapps.avrprogrammer.model.ProgrammerNoSyncMessage;
import com.snirpoapps.avrprogrammer.model.ProgrammerProgPageCommand;

public class Programmer extends Thread {
	public static int LOADING_HEX_FILE = 0;
	public static int RESETTING = 1;
	public static int GETTING_IN_SYNC = 2;
	public static int ENTERING_PROGRAMMING_MODE = 3;
	public static int PROGRAMMING = 4;
	public static int LEAVING_PROGRAMMING_MODE = 5;

	private File hexFile;
	private Device device;
	private ArrayList<IntelHexRecord> records;
	private int state = 0;
	private int recordIndex = 0;
	private LinkedList<BluetoothMessage> messages = new LinkedList<BluetoothMessage>();
	private boolean active = true;

	public Programmer(File hexFile, Device device) {
		this.hexFile = hexFile;
		this.device = device;
	}

	@Override
	public void run() {
		device.onProgrammerProgressUpdate(LOADING_HEX_FILE, 0);
		//File file = new File(Environment.getExternalStorageDirectory(),
		//		"/GPSPlayController.hex");
		//Log.v("DIR", file.toString());

		records = IntelHexParser.parse(hexFile);

		device.onProgrammerProgressUpdate(RESETTING, 0);
//		ModeCommand c = new ModeCommand();
//		c.setMode(ModeCommand.MODE_PROGRAMMER);
//		device.sendCommand(c);

		try {
			Thread.sleep(500);

			device.onProgrammerProgressUpdate(GETTING_IN_SYNC, 0);
			device.send(new ProgrammerGetSyncCommand());

			// int n = 0;
			// while (true) {
			// BluetoothMessage message = null;
			// synchronized (messages) {
			// while (messages.isEmpty()) {
			// messages.wait();
			// if (!active)
			// return;
			// }
			// message = messages.removeFirst();
			// }
			// if (message != null
			// && message instanceof ProgrammerInSyncMessage)
			// break;
			// else if (n == 4)
			// return;
			//
			// n++;
			// }

			while (true) {
				BluetoothMessage message = null;
				synchronized (messages) {
					while (messages.isEmpty()) {
						messages.wait();
						if (!active)
							return;
					}
					message = messages.removeFirst();
				}

				if (message instanceof ProgrammerInSyncMessage) {
					if (state == 0) {
						state++;
						device.onProgrammerProgressUpdate(
								ENTERING_PROGRAMMING_MODE, 0);
						device.send(new ProgrammerEnterProgmodeCommand());
						// statusTextView.append("Entering programming mode\n");
						recordIndex = 0;
					} else if (state == 1) {
						state++;
						device.onProgrammerProgressUpdate(PROGRAMMING,
								(recordIndex / records.size()) * 100);
						ProgrammerLoadAddressCommand cmd = new ProgrammerLoadAddressCommand();
						IntelHexRecord r = records.get(recordIndex);
						cmd.setAddress(r.address);
						device.send(cmd);
					} else if (state == 2) {
						ProgrammerProgPageCommand cmd = new ProgrammerProgPageCommand();
						IntelHexRecord r = records.get(recordIndex);
						cmd.setData(r.data);
						device.send(cmd);

						recordIndex++;
						if (recordIndex >= records.size())
							state = 3;
						else
							state = 1;
					} else if (state == 3) {
						device.onProgrammerProgressUpdate(
								LEAVING_PROGRAMMING_MODE, 100);
						device.send(new ProgrammerLeaveProgmodeCommand());
						return;
					}
				} else if (message instanceof ProgrammerNoSyncMessage) {
					device.onProgrammerError(state);
					return;
				}
			}
		} catch (InterruptedException e) {
			device.onProgrammerError(state);
		}
	}

	public void cancel() {
		active = false;
		synchronized (messages) {
			messages.notify();
		}
	}

	public void addMessage(BluetoothMessage message) {
		messages.add(message);
		synchronized (messages) {
			messages.notify();
		}
	}
	
	public BluetoothMessage getMessageType(int type){
		if (type == 0x14)
			return new ProgrammerInSyncMessage();
		else if (type == 0x15)
			return new ProgrammerNoSyncMessage();
		return null;
	}
}
