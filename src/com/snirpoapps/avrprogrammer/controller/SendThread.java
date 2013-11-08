package com.snirpoapps.avrprogrammer.controller;

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;

import com.snirpoapps.avrprogrammer.model.BluetoothCommand;

public class SendThread extends Thread {
	private boolean active = true;
	private OutputStream outputStream;
	private LinkedList<BluetoothCommand> commands = new LinkedList<BluetoothCommand>();
	private Device controller;

	public SendThread(OutputStream outputStream,
			Device controller) {
		this.outputStream = outputStream;
		this.controller = controller;
	}

	@Override
	public void run() {
		byte[] buffer = new byte[256];
		System.out.println("SendThread Started");
		
		try {
			while (true) {
				BluetoothCommand c = null;
				ByteBuffer writeBuffer = null;
				synchronized (commands) {
					while (commands.isEmpty()){
						commands.wait();
						if (!active){
							System.out.println("SendThread Stopped");
							return;
						}
					}
					//System.out.println("SEND");
					c = commands.removeFirst();
				}
				byte[] bytes = c.toBytes();
//				String tmp = "Send: ";
//				for (int i = 0; i < totalLength; i++)
//					tmp += String.format("%02X", bytes[i]) + ":";
//				System.out.println(tmp);
				outputStream.write(bytes);
				outputStream.flush();
				//sleep(2);
				//sleep(20);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			controller.onError("Send error");
		}
	}

	public void send(BluetoothCommand command) {
		synchronized (commands) {
			//System.out.println(commands.size());
			commands.add(command);
			commands.notify();
		}
	}

	public void destroy() {
		active = false;
		synchronized (commands) {
			commands.notify();
		}
	}

}
