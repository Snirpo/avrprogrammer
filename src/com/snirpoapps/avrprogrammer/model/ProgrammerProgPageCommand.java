package com.snirpoapps.avrprogrammer.model;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ProgrammerProgPageCommand extends BluetoothCommand {
	private byte[] data;

	@Override
	public byte[] toBytes() {
		ByteBuffer buffer = ByteBuffer.allocate(data.length + 5);
		buffer.order(ByteOrder.BIG_ENDIAN);
		buffer.put((byte) 0x64);
		buffer.putShort((short) data.length);
		buffer.put((byte) 0x46);
		buffer.put(data);
		buffer.put((byte) 0x20);
		return buffer.array();
	}

	public void setData(byte[] data) {
		this.data = data;
	}
}
