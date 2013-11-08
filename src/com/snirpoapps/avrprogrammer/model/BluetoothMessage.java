package com.snirpoapps.avrprogrammer.model;

public abstract class BluetoothMessage {
	public abstract void fromBytes(byte[] bytes);
	public abstract int getSize();
}
