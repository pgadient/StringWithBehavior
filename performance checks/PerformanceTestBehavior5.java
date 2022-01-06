import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.Random;
import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class PerformanceTestBehavior5 {
	private static final int n = 1000000;
	private static String[] strings;

	public static void main(String[] args) throws Exception {
		strings = new String[n];

		FileInputStream fstream = new FileInputStream("strings.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

		for(int i = 0 ; i < n ; i++) {
			String strLine = br.readLine();
			if(strLine == null){
				strings[i] = "";
			} else {
				strings[i] = strLine;
			}
		}
		
		//Close the input stream
		fstream.close();

		System.setOut(new PrintStream(new OutputStream() { @Override public void write(int arg0) { } }));

		initTest();
		printTest();
		inheritanceTest();
	}
	
	private static void appendToFile(String s) {
		try{
			FileWriter fw = new FileWriter("log\\performanceResultsBehavior5.txt", true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(s);
			bw.newLine();
			bw.close();
		}catch(Exception e){
		}		
	}
	
	private static void initTest() {
		long startTime = new Date().getTime();
		
		for(int i = 0; i < n ; i++){
			new String(strings[i]).setLogic(new Behavior5());
		}
		
		long endTime = new Date().getTime();
		appendToFile("Init," + (endTime - startTime) + "," + n);
	}
	
	private static void printTest() {
		long startTime = new Date().getTime();
		
		for(int i = 0; i < n ; i++){
			String s = new String(strings[i]);
			s.setLogic(new Behavior5());
			System.out.println(s);
		}
		
		long endTime = new Date().getTime();
		appendToFile("Read," + (endTime - startTime) + "," + n);
	}

	private static void inheritanceTest() {
		String s = new String(strings[0]);

		long startTime = new Date().getTime();
		
		s.setLogic(new Behavior5());

		for(int i = 0; i < n ; i++){
			s = new String(s);
		}
		
		long endTime = new Date().getTime();
		appendToFile("Inheritance," + (endTime - startTime) + "," + n);
	}

	private static class Behavior5 implements IStringLogic {
		// private Cipher enCipher;
    	// private Cipher deCipher;
		// private SecretKeySpec key;
		// private String keyString = "someKeyString";
		private SecretKey key;
		private Random rnd = new Random();
		
		private IvParameterSpec iv;
		private byte[] encrypted;

		public Behavior5() {
			try{
				// DESKeySpec dkey = new DESKeySpec(keyString.getBytes());
				// key = new SecretKeySpec(dkey.getKey(), "DES");
				key = KeyGenerator.getInstance("DES").generateKey();
				byte[] ivBytes = new byte[8];
				rnd.nextBytes(ivBytes);
				iv = new IvParameterSpec(ivBytes);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public String applyOnCreation(byte[] value, byte coder) {
			try{
				Cipher enCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
				enCipher.init(Cipher.ENCRYPT_MODE, key, iv);
				encrypted = enCipher.doFinal(value);
				return new String(encrypted, StandardCharsets.ISO_8859_1);
			} catch(Exception e) {
				e.printStackTrace();
			}
			return new String(value);
		}

		@Override
		public String applyOnRead(String s) {
			try{
				Cipher deCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
				deCipher.init(Cipher.DECRYPT_MODE, key, iv);
				byte[] decrypted = deCipher.doFinal(encrypted);
				return new String(decrypted, StandardCharsets.ISO_8859_1);
			} catch(Exception e) {
				e.printStackTrace();
			}
			return s;
		}
		
		@Override
		public boolean inheritToChild(StringTransformType stt) {
			return true;
		}

		@Override
		public boolean recordHistory() {
			return false;
		}

		@Override
		public String getDescription() {
			return "";
		}
	}
}