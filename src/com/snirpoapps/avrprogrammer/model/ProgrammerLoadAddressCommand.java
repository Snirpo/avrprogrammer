package com.snirpoapps.avrprogrammer.model;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ProgrammerLoadAddressCommand extends BluetoothCommand {
	private short address = 0x00;

	@Override
	public byte[] toBytes() {
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.put((byte) 0x55);
		buffer.putShort(address);
		buffer.put((byte) 0x20);
		return buffer.array();
	}
	
	public void setAddress(int address){
		this.address = (short) address;
	}
}
