package x.mvmn.aircndctrl.hap.service;

import java.beans.Transient;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.hapjava.HomekitAuthInfo;
import io.github.hapjava.HomekitServer;

public class AuthInfoService implements HomekitAuthInfo {

	public static class AuthInfoData {
		protected String mac;
		protected String salt;
		protected String key;
		protected BigInteger saltBigInt;
		protected byte[] keyBytes;

		public AuthInfoData() {}

		public AuthInfoData(String mac, BigInteger salt, byte[] key) {
			this.mac = mac;
			this.salt = Base64.getEncoder().encodeToString(salt.toByteArray());
			this.key = Base64.getEncoder().encodeToString(key);
		}

		public String getSalt() {
			return salt;
		}

		public void setSalt(String salt) {
			this.salt = salt;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getMac() {
			return mac;
		}

		public void setMac(String mac) {
			this.mac = mac;
		}

		@Transient
		public BigInteger saltBigInt() {
			if (saltBigInt == null) {
				saltBigInt = new BigInteger(Base64.getDecoder().decode(salt));
			}
			return saltBigInt;
		}

		@Transient
		public byte[] keyBytes() {
			if (keyBytes == null) {
				keyBytes = Base64.getDecoder().decode(key);
			}
			return keyBytes;
		}
	}

	private final AuthInfoData authInfoData;
	private final File usersFolder;

	public AuthInfoService(File dataFolder) {
		try {
			this.usersFolder = new File(dataFolder, "users");
			if (!usersFolder.exists()) {
				usersFolder.mkdirs();
			}
			File authInfoDataFile = new File(dataFolder, "auth");
			if (!authInfoDataFile.exists()) {
				authInfoData = new AuthInfoData(HomekitServer.generateMac(), HomekitServer.generateSalt(), HomekitServer.generateKey());
				FileUtils.writeByteArrayToFile(authInfoDataFile, new ObjectMapper().writeValueAsBytes(authInfoData));
			} else {
				authInfoData = new ObjectMapper().readValue(FileUtils.readFileToByteArray(authInfoDataFile), AuthInfoData.class);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getPin() {
		return System.getProperty("achp.pin", "123-45-678");
	}

	@Override
	public String getMac() {
		return authInfoData.getMac();
	}

	@Override
	public BigInteger getSalt() {
		return authInfoData.saltBigInt();
	}

	@Override
	public byte[] getPrivateKey() {
		return authInfoData.keyBytes();
	}

	@Override
	public void createUser(String username, byte[] publicKey) {
		try {
			FileUtils.writeByteArrayToFile(new File(usersFolder, usernameToFileName(username)), publicKey, false);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void removeUser(String username) {
		new File(usersFolder, usernameToFileName(username)).delete();
	}

	@Override
	public byte[] getUserPublicKey(String username) {
		try {
			File userFile = new File(usersFolder, usernameToFileName(username));
			return userFile.exists() ? FileUtils.readFileToByteArray(userFile) : null;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected String usernameToFileName(String username) {
		try {
			return username != null ? URLEncoder.encode(username, StandardCharsets.UTF_8.name()) + ".hkuser" : null;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean hasUser() {
		return Stream.of(usersFolder.listFiles()).map(File::getName).filter(fileName -> fileName.toLowerCase().endsWith(".hkuser")).count() > 0;
	}
}
