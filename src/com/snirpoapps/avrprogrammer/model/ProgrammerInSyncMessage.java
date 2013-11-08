package com.snirpoapps.avrprogrammer.model;

import android.util.Log;

public class ProgrammerInSyncMessage extends BluetoothMessage {
	private boolean ok = false;

	public ProgrammerInSyncMessage() {
	}

	@Override
	public String toString() {
		String str = "";
		return str;
	}

	@Override
	public int getSize() {
		return 2;
	}

	@Override
	public void fromBytes(byte[] bytes) {
		ok = (bytes[1] == 0x10);
		Log.v("BYTE", Integer.toBinaryString(bytes[0]) + " : " + Integer.toBinaryString(bytes[1]));
	}

	public boolean isOk() {
		return ok;
	}
}
