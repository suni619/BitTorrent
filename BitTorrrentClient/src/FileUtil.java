

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FileUtil {

	public static final int CHUNK_SIZE = 256000;

	public static void main(String[] args) {
		// createFile();

		String pathName = "seed/sample.ppt";

		// splits the file into parts and also creates a metainfo/torrent file
		createParts(pathName, "seed/");

		String torrentFilePath = "seed/sample.ppt.torrent";

		// read the meta info file
		Map<String, Object> metainfo = readMetaInfoFile(torrentFilePath);
		System.out.println("Filename: " + metainfo.get("filename"));
		System.out.println("File length: " + metainfo.get("fileLength"));
		System.out.println("Piece length: " + metainfo.get("pieceLength"));
		System.out.println("Hash: " + metainfo.get("piecesHash"));
		System.out.println("Url: " + metainfo.get("url"));

		// recreate the complete file from parts
		String recreatedFilePath = "seed/recreated_sample.ppt";
		recreateFile(recreatedFilePath);
	}

	/**
	 * Create the metainfo file in the destDir. The contents of the file will be
	 * in the terse format
	 * 
	 * @param fileName
	 * @param fileLength
	 * @param pieceLength
	 * @param piecesHash
	 * @param destDir
	 */
	public static void createMetaInfoFile(String fileName, long fileLength,
			int pieceLength, String piecesHash, String destDir) {
		/**
		 * METAINFO FILE STRUCTURE info: name: filename (string) length: length
		 * of file in bytes (integer) piece length: no. of bytes in each piece
		 * (integer) 256 kB pieces: (string) concatenation of all 20 byte SHA1
		 * hash values, one per piece announce: URL of tracker (string)
		 */
		StringBuilder content = new StringBuilder();
		String trackerUrl = "http://localhost:8080/Tracker/rest.tracker?file="
				+ fileName;
		content.append("d");
		content.append("4:info");
		content.append("l");
		content.append(fileName.length() + ":" + fileName + "i" + fileLength
				+ "e" + "i" + pieceLength + "e");
		content.append(piecesHash.length() + ":" + piecesHash);
		content.append("e");
		content.append("3:url");
		content.append(trackerUrl.length() + ":" + trackerUrl);
		content.append("e");

		System.out.println(content.toString());

		// write to a torrent file
		String torrentFilePath = destDir + fileName + ".torrent";
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(new File(torrentFilePath)));
			bw.write(content.toString());
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bw != null) {
					bw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Torrent/metainfo file created.");
	}

	/**
	 * Read the metainfo/torrent file present in the given path and return the
	 * parameters as a HashMap to the caller
	 * 
	 * @param path
	 * @return HashMap of the details
	 */
	public static Map<String, Object> readMetaInfoFile(String path) {
		BufferedReader br = null;
		File file = new File(path);
		char[] buffer = new char[(int) file.length()];
		try {
			// open the torrent file
			br = new BufferedReader(new FileReader(new File(path)));
			br.read(buffer);
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < buffer.length; i++) {
			sb.append(buffer[i]);
		}
		System.out.println("Metainfo: " + sb.toString());

		// parse the terse format
		String metaInfo = sb.toString();
		metaInfo = metaInfo.substring(8);
		String fileNameLengthString = metaInfo.substring(0,
				metaInfo.indexOf(":"));
		int fileNameLength = Integer.parseInt(fileNameLengthString);
		metaInfo = metaInfo.substring(metaInfo.indexOf(":") + 1);
		String fileName = metaInfo.substring(0, fileNameLength);
		metaInfo = metaInfo.substring(fileNameLength + 1);
		String fileLengthString = metaInfo.substring(0, metaInfo.indexOf("e"));
		int fileLength = Integer.parseInt(fileLengthString);
		metaInfo = metaInfo.substring(fileLengthString.length() + 2);
		String pieceLengthString = metaInfo.substring(0, metaInfo.indexOf("e"));
		int pieceLength = Integer.parseInt(pieceLengthString);
		metaInfo = metaInfo.substring(pieceLengthString.length() + 1);
		String piecesHashLengthString = metaInfo.substring(0,
				metaInfo.indexOf(":"));
		metaInfo = metaInfo.substring(piecesHashLengthString.length() + 1);
		String piecesHash = metaInfo
				.substring(0, metaInfo.indexOf("3:url") - 1);
		metaInfo = metaInfo.substring(metaInfo.indexOf("3:url") + 5);
		String urlLengthString = metaInfo.substring(0, metaInfo.indexOf(":"));
		metaInfo = metaInfo.substring(urlLengthString.length() + 1);
		String url = metaInfo.substring(0, metaInfo.length() - 1);

		// store the information in the terse format into a map
		Map<String, Object> metainfoMap = new HashMap<String, Object>();
		metainfoMap.put("filename", fileName);
		metainfoMap.put("fileLength", fileLength);
		metainfoMap.put("pieceLength", pieceLength);
		metainfoMap.put("piecesHash", piecesHash);
		metainfoMap.put("url", url);

		return metainfoMap;

	}

	/**
	 * Recreates the complete file from the parts in the given directory
	 */
	public static void recreateFile(String pathName) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(new File(pathName));
			File dir = new File("seed");
			File[] parts = dir.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					return name.contains(".part");
				}
			});

			byte[] buffer = new byte[parts.length * CHUNK_SIZE];
			for (int part = 0; part < parts.length; part++) {
				String partName = "seed/sample.ppt.part" + part;
				File partFile = new File(partName);
				System.out.println("From file: " + partFile.getName()
						+ "of size: " + partFile.length());
				FileInputStream fis = new FileInputStream(partFile);
				fis.read(buffer, part * CHUNK_SIZE, CHUNK_SIZE);
				fis.close();
			}
			fos.write(buffer);
			System.out.println("Recreated file successfully");
		} catch (IOException e) {
			System.out.println(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Splits the file into parts, computes the SHA-1 hash and and calls the
	 * method to create the metainfo/torrent file
	 * 
	 * @param srcFile
	 * @param destDir
	 */
	public static void createParts(String srcFile, String destDir) {

		FileInputStream fis = null;
		byte[] buffer = new byte[CHUNK_SIZE];
		StringBuilder piecesHash = new StringBuilder();
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}

		try {
			File sourceFile = new File(srcFile);
			fis = new FileInputStream(sourceFile);
			int bytesRead = fis.read(buffer, 0, buffer.length);
			System.out.println("Bytes read: " + bytesRead);

			// till last but one parts
			int part = 0;
			for (part = 0; bytesRead == CHUNK_SIZE; part++) {
				String partFileName = destDir + sourceFile.getName() + ".part"
						+ part;
				File partFile = new File(partFileName);
				FileOutputStream fos = new FileOutputStream(partFile);
				fos.write(buffer);
				fos.close();
				System.out.println("File size: " + partFile.length());

				// compute SHA-1 hash
				byte[] mdBytes = md.digest(buffer);
				for (int i = 0; i < mdBytes.length; i++) {
					piecesHash.append(Integer.toHexString(0xFF & mdBytes[i]));
				}

				System.out.println("-- New Chunk --");
				bytesRead = fis.read(buffer, 0, buffer.length);
				System.out.println("Bytes read: " + bytesRead);
			}

			// for the last part
			if (bytesRead > -1) {
				// truncate the remaining part of the buffer
				buffer = Arrays.copyOf(buffer, bytesRead);
				String partFileName = destDir + sourceFile.getName() + ".part"
						+ part;
				File partFile = new File(partFileName);
				FileOutputStream fos = new FileOutputStream(partFile);
				fos.write(buffer);
				fos.close();
				System.out.println("File size: " + partFile.length());

				// compute SHA-1 hash
				byte[] mdBytes = md.digest(buffer);
				for (int i = 0; i < mdBytes.length; i++) {
					piecesHash.append(Integer.toHexString(0xFF & mdBytes[i]));
				}

			}
			System.out.println("SHA-1 hash value: " + piecesHash.toString());

			System.out.println("Created parts successfully.");
			// create a metainfo file
			createMetaInfoFile(sourceFile.getName(), sourceFile.length(),
					CHUNK_SIZE, piecesHash.toString(), destDir);
		} catch (IOException e) {
			System.out.println(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
