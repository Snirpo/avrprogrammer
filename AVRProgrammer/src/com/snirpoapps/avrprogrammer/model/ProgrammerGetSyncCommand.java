package com.snirpoapps.avrprogrammer.model;

public class ProgrammerGetSyncCommand extends BluetoothCommand {
	@Override
	public byte[] toBytes() {
		byte[] bytes = new byte[2];
		bytes[0] = 0x30;
		bytes[1] = 0x20;
		return bytes;
	}
}
