package com.snirpoapps.avrprogrammer.model;

public class ProgrammerNoSyncMessage extends BluetoothMessage {

	public ProgrammerNoSyncMessage() {
	}

	@Override
	public String toString() {
		String str = "";

		return str;
	}

	@Override
	public int getSize() {
		return 1;
	}

	@Override
	public void fromBytes(byte[] bytes) {
	}

}
