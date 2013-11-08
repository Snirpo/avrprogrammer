package com.snirpoapps.avrprogrammer.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import com.snirpoapps.avrprogrammer.model.IntelHexRecord;

public abstract class IntelHexParser {
	private static final int PAGE_SIZE = 256;
	
	private static final int DATA = 0x00;
	private static final int EOF = 0x01;
	private static final int EXT_SEG = 0x02;
	private static final int START_SEG = 0x03;
	private static final int EXT_LIN = 0x04;
	private static final int START_LIN = 0x05;
	private static final int UNKNOWN = 0xFF;
	private static final int HEX = 16;

	public static ArrayList<IntelHexRecord> parse(File file) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			ArrayList<IntelHexRecord> records = new ArrayList<IntelHexRecord>();

			String recordStr;
			IntelHexRecord record = null;

			while ((recordStr = reader.readLine()) != null) {
				if (recordStr.startsWith(":")) {

					int lineLength = recordStr.length();
					byte[] hexRecord = new byte[lineLength / 2];

					// sum of all bytes modulo 256 (including checksum) shuld be 0
					int sum = 0;
					for (int i = 0; i < hexRecord.length; i++) {
						String num = recordStr.substring(2 * i + 1, 2 * i + 3);
						hexRecord[i] = (byte) Integer.parseInt(num, HEX);
						sum += hexRecord[i] & 0xff;
					}
					sum &= 0xff;

					if (sum == 0 && hexRecord[0] + 5 == hexRecord.length) {
						int type = (hexRecord[3] & 0xFF);
						if (type == DATA) {
							if (record == null || record.length >= PAGE_SIZE){		
								record = new IntelHexRecord();
								record.address = (records.size() * PAGE_SIZE) / 2;
								record.data = new byte[PAGE_SIZE];
								record.length = 0;
								records.add(record);
								
								//record.address = ((hexRecord[1] & 0xFF) << 8)
										//+ (hexRecord[2] & 0xFF);
								//record.length = hexRecord[0];
								//record.data = new byte[record.length];
							}
							
							//System.arraycopy(hexRecord, 4, record.data, 0,
							//		record.length);
							System.arraycopy(hexRecord, 4, record.data, record.length,
									hexRecord[0]);
							record.length += hexRecord[0];
						}
						else if (type == EOF){
							break;
						}
						else {
							return null;
						}
					}
					else {
						return null;
					}
				}
				else {
					return null;
				}
			}

			return records;
		} catch (IOException e) {
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
			}
		}
		return null;
	}
}
