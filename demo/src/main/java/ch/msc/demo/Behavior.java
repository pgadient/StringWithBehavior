package ch.msc.demo;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.StringJoiner;
import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import ch.msc.demo.mail.MailAgent;

public class Behavior implements IStringBehavior {
	private SecretKey key;
	private Random rnd = new Random();
	
	private IvParameterSpec iv;

	private String[] unauthorizedPackages = { "java.net", "java.io" };
	private List<String> ioPackagesList = Arrays.asList(unauthorizedPackages);

	private String[] admins = {
		"admin1@organization.ch",
		"admin2@organization.ch",
		"admin3@organization.ch"
	};

	/**
	 * Only initialization of cryptographic tools
	 */
	public Behavior() {
		try{
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
		// Encrypt created String and store result in memory
		return encrypt(value, coder);
	}

	@Override
	public String applyOnRead(String s) {
		// Check if data is leaked
		boolean isLeak = false;
		StackTraceElement[] contexts = Thread.currentThread().getStackTrace();
		for(int i = 1; i < contexts.length; i++) {
			for(String ioPackageName: ioPackagesList) {
				if(contexts[i].getClassName().startsWith(ioPackageName)) {
					isLeak = true;
				}
			}
		}

		if(isLeak) {
			// if is leaked send mail to all admins
			// but data stays encrypted
			for(String adminToContact: admins) {
				MailAgent.sendMessage(adminToContact, "Sensitive data was leaked:\n" + stacktraceToString(contexts));
			}
		} else {
			// if no leak, decrypt data for the next operation
			return decrypt(s);
		}
		return s;
	}

	@Override
	public boolean transferToDerivative(StringTransformType stt) {
		return true;
	}

	@Override
	public boolean recordHistory() {
		return true;
	}

	@Override
	public String getDescription() {
		return "";
	}

	private String encrypt(byte[] value, byte coder) {
		try{
			Cipher enCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
			enCipher.init(Cipher.ENCRYPT_MODE, key, iv);
			byte[] encrypted = enCipher.doFinal(value);
			return new String(encrypted, StandardCharsets.ISO_8859_1);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return new String(value);
	}

	private String decrypt(String s) {
		try{
			Cipher deCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
			deCipher.init(Cipher.DECRYPT_MODE, key, iv);
			byte[] decrypted = deCipher.doFinal(s.getBytes(StandardCharsets.ISO_8859_1));
			return new String(decrypted, StandardCharsets.ISO_8859_1);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return s;
	}

	private static String stacktraceToString(StackTraceElement[] context) {
		StringJoiner sj = new StringJoiner("\n");
		for(int i = 5 ; i < context.length ; i++){
			sj.add(context[i].toString());
		}
		return sj.toString();
	}
}