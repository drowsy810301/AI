package ai;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainClass {
	protected final static int NET_SOURCE = 0, FILE_SOURCE = 1;
	
	public static String ReadFile(String fileName){
		StringBuilder sb = new StringBuilder();
		int count;
		byte[] buff = new byte[4096];
		
		try {
			FileInputStream fin = new FileInputStream(fileName);
			BufferedInputStream bin = new BufferedInputStream(fin);
			while ((count = bin.read(buff)) != -1){
				sb.append(new String(buff,0,count));
			}
			bin.close();
			fin.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sb.toString();
	}
	public static void WriteToFile(String fileName, String content){
		
		try {
			FileOutputStream fout = new FileOutputStream(fileName);
			BufferedOutputStream buf = new BufferedOutputStream(fout);
			buf.write(content.getBytes());
			buf.close();
			fout.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
