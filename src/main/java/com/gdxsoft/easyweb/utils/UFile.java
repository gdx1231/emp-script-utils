package com.gdxsoft.easyweb.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.gdxsoft.easyweb.utils.msnet.MStr;

public class UFile {

	/**
	 * Change the file creation time
	 * 
	 * @param filePath        the file
	 * @param creationDate
	 * @throws IOException
	 */
	public static void changeCreationTime(String filePath, Date creationTime) throws IOException {
		changeCreationAndModifiedTime(filePath, creationTime, null);
	}

	/**
	 * Change the file modification time
	 * 
	 * @param filePath        the file
	 * @param modifiedDate
	 * @throws IOException
	 */
	public static void changeModificationTime(String filePath, Date modificationTime) throws IOException {
		changeCreationAndModifiedTime(filePath, null, modificationTime);
	}

	/**
	 * Change the file creation and modification time
	 * 
	 * @param filePath         the file
	 * @param creationTime
	 * @param modificationTime
	 * @throws IOException
	 */
	public static void changeCreationAndModifiedTime(String filePath, Date creationTime, Date modificationTime)
			throws IOException {
		if (filePath == null || filePath.length() == 0) {
			return;
		}

		File f1 = new File(filePath);
		if (!f1.exists()) {
			return;
		}
		Path pathSql = Paths.get(filePath);
		if (creationTime != null) {
			Files.setAttribute(pathSql, "basic:creationTime", FileTime.fromMillis(creationTime.getTime()));
		}
		if (modificationTime != null) {
			Files.setLastModifiedTime(pathSql, FileTime.fromMillis(modificationTime.getTime()));
		}
	}

	/**
	 * Read the first entry name from a ZIP local file header (buf starts with PK\x03\x04).
	 * Used to distinguish docx/xlsx/pptx/odt from generic zip.
	 */
	private static String readZipFirstEntryName(byte[] buf) {
		if (buf.length < 30) {
			return null;
		}
		// filename length at offset 26-27 (little-endian)
		int nameLen = ((buf[27] & 0xFF) << 8) | (buf[26] & 0xFF);
		int extraLen = ((buf[29] & 0xFF) << 8) | (buf[28] & 0xFF);
		int nameStart = 30;
		int nameEnd = nameStart + nameLen;
		if (nameLen <= 0 || nameLen > 256 || buf.length < nameEnd) {
			return null;
		}
		return new String(buf, nameStart, nameLen, StandardCharsets.UTF_8);
	}

	/**
	 * Distinguish OLE2 compound document types by checking first sector (offset 512).
	 * xls: BIFF8 BOF record (09 08 ... 05 00)
	 * ppt: CurrentUserAtom (0F 00)
	 * doc/wps: Word FIB (EC A5) or fallback
	 */
	private static String detectOle2Type(byte[] buf) {
		if (buf.length < 530) {
			return "doc"; // insufficient data, default to doc
		}
		int w1 = ((buf[512] & 0xFF) | ((buf[513] & 0xFF) << 8));
		// Excel BIFF8: record type 0x0809 (BOF), sub-type 0x0005 (workbook globals)
		if (w1 == 0x0809 && buf.length >= 518 && buf[516] == 0x05 && buf[517] == 0x00) {
			return "xls";
		}
		// PowerPoint: CurrentUserAtom record type 0x000F
		if (w1 == 0x000F) {
			return "ppt";
		}
		// Word FIB: magic 0xA5EC
		if (w1 == 0xA5EC) {
			return "doc"; // also matches wps
		}
		// Default OLE2 → doc (covers wps, msi, msg, etc.)
		return "doc";
	}

	/**
	 * Get the extension from the bytes of the file
	 *
	 * @param buf the bytes of the file
	 * @return the file extension
	 */
	public static String getExtFromFileBytes(byte[] buf) {
		if (buf.length < 120) {
			return "bin";
		}
		// Use ISO-8859-1 to preserve raw byte values (no encoding corruption)
		String s = new String(buf, 0, 120, StandardCharsets.ISO_8859_1);
		char c0 = s.charAt(0);

		// SWF (CWS=compressed, FWS=uncompressed)
		if ((s.startsWith("CWS") || s.startsWith("FWS")) && s.length() > 3 && s.charAt(3) <= 20) {
			return "swf";
		}
		// ZIP / JAR / Office Open XML / OpenDocument
		if (c0 == 'P' && s.charAt(1) == 'K' && s.charAt(2) == 0x03 && s.charAt(3) == 0x04) {
			String zipFirstEntry = readZipFirstEntryName(buf);
			if (zipFirstEntry != null) {
				if (zipFirstEntry.equals("mimetype")) {
					return "odt"; // OpenDocument
				}
				if (zipFirstEntry.contains("word/")) {
					return "docx";
				}
				if (zipFirstEntry.contains("xl/")) {
					return "xlsx";
				}
				if (zipFirstEntry.contains("ppt/")) {
					return "pptx";
				}
			}
			return "zip";
		}
		// RAR
		if (s.startsWith("Rar!\u001A\u0007")) {
			return "rar";
		}
		// GIF
		if (s.startsWith("GIF8")) {
			return "gif";
		}
		// BMP
		if (s.startsWith("BM")) {
			return "bmp";
		}
		// PNG: byte0=0x89, byte1-3=PNG
		if (c0 == 0x89 && s.charAt(1) == 'P' && s.charAt(2) == 'N' && s.charAt(3) == 'G') {
			return "png";
		}
		// JPEG: FF D8 FF E0/E1 + JFIF/JFXX at offset 6
		if (c0 == 0xFF && s.charAt(1) == 0xD8 && s.charAt(2) == 0xFF) {
			// JFIF or Exif at offset 6
			if (s.length() > 10 && (s.substring(6, 10).equals("JFIF") || s.substring(6, 10).equals("Exif"))) {
				return "jpg";
			}
		}
		// RTF
		if (s.startsWith("{\\rtf")) {
			return "rtf";
		}
		// OLE2 compound document: D0 CF 11 E0 A1 B1 1A E1 (doc, xls, ppt, wps...)
		if (c0 == 0xD0 && s.charAt(1) == 0xCF && s.charAt(2) == 0x11 && s.charAt(3) == 0xE0
				&& s.charAt(4) == 0xA1 && s.charAt(5) == 0xB1 && s.charAt(6) == 0x1A && s.charAt(7) == 0xE1) {
			return detectOle2Type(buf);
		}
		// TIFF: II (little-endian) or MM (big-endian)
		if ((c0 == 'I' && s.charAt(1) == 'I') || (c0 == 'M' && s.charAt(1) == 'M')) {
			return "tif";
		}
		return "bin";
	}

	/**
	 * Delete a file
	 * 
	 * @param name The file path and name
	 * @return result
	 */
	public static boolean delete(String name) {
		if (name == null || name.trim().length() == 0) {
			return false;
		}
		File f = new File(name);
		if (!f.exists()) {
			return false;
		}
		try {
			return f.delete();
		} catch (Exception err) {
			System.err.println(err.getMessage());
			return false;
		}
	}

	/**
	 * Get the extension form the file name
	 * 
	 * @param name the file name
	 * @return the extension
	 */
	public static String getFileExt(String name) {
		if (name.endsWith(".")) {
			return "";
		}

		int m = name.lastIndexOf(".");
		if (m > 0) {
			return name.substring(m + 1);
		} else {
			return "";
		}
	}

	/**
	 * Get the file name without extension
	 * 
	 * @param name the file name
	 * @return the file name without extension
	 */
	public static String getFileNoExt(String name) {
		File f = new File(name);
		String name1 = f.getName();
		int m = name1.lastIndexOf(".");
		if (m > 0) {
			return name1.substring(0, m);
		} else {
			return name1;
		}
	}

	/**
	 * Change the file extension
	 * 
	 * @param name   the file's name and path
	 * @param newExt new extension
	 * @return new file name and path
	 */
	public static String changeFileExt(String name, String newExt) {
		File f = new File(name);
		String nameNoExt = getFileNoExt(f.getName());
		String path = (f.getParent() == null ? "" : f.getParent() + File.separator) + nameNoExt + "." + newExt;
		f = new File(path);
		return f.getAbsolutePath();
	}

	/**
	 * Get the files in the parent directory according to the filter, excluding sub
	 * directories
	 * 
	 * @param rootPath the parent directory
	 * @param filter   filter
	 * @return the files
	 */
	public static File[] getFiles(String rootPath, String[] filter) {
		File f = new File(rootPath);
		if (!f.exists() || !f.isDirectory()) {
			return null;
		}

		FilenameFilter ff = UFileFilter.getInstance(filter);
		return f.listFiles(ff);
	}

	/**
	 * Convert the file to GZIP compressed base64 string
	 * 
	 * @param path the file name and path
	 * @return the result
	 * @throws IOException
	 */
	public static String readFileGzipBase64(String path) throws IOException {
		int BUFFER = 4096;
		BufferedInputStream origin = null;
		ByteArrayOutputStream bytesStream = new ByteArrayOutputStream();
		GZIPOutputStream gzipOut;
		byte data[] = new byte[BUFFER];
		File f = new File(path);
		try {
			gzipOut = new GZIPOutputStream(bytesStream);
			FileInputStream fi = new FileInputStream(f);
			origin = new BufferedInputStream(fi, BUFFER);

			int count;

			while ((count = origin.read(data, 0, BUFFER)) != -1) {
				gzipOut.write(data, 0, count);
			}
			origin.close();
			gzipOut.close();

			String s1 = UConvert.ToBase64String(bytesStream.toByteArray());

			return s1;
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				bytesStream.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * Convert the file to base64 string
	 * 
	 * @param path the file name and path
	 * @return the result;
	 * @throws Exception
	 */
	public static String readFileBase64(String path) throws IOException {
		byte[] buf = readFileBytes(path);
		String s1 = UConvert.ToBase64String(buf);
		return s1;
	}

	/**
	 * Read the binary contents of the file
	 * 
	 * @param path the file name and path
	 * @return binary contents
	 * @throws IOException
	 */
	public static byte[] readFileBytes(String path) throws IOException {
		File file = new File(path);
		if (file.exists()) { // 按照文件读取
			return Files.readAllBytes(Paths.get(path));
		} else {
			path = path.replace("\\", "/").replace("//", "/");
			URL url = UFile.class.getResource(path);
			if (url == null) {
				url = UFile.class.getClassLoader().getResource(path);
			}
			if (url == null) {
				throw new IOException("The file " + path + " not exists in resource and file ");
			}
			// 从jar包中读取
			return url.openStream().readAllBytes();
		}

	}

	/**
	 * Read the file contents(UTF8) from file or resource
	 * 
	 * @param filePath the file name and path
	 * @return the file contents (UTF8)
	 * @throws IOException
	 */
	public static String readFileText(String filePath) throws IOException {
		File f1 = new File(filePath);
		if (f1.exists()) {
			return Files.readString(f1.toPath(), StandardCharsets.UTF_8);
		} else {
			filePath = filePath.replace("\\", "/").replace("//", "/").replace("//", "/").replace("//", "/")
					.replace("//", "/");
			URL url = UFile.class.getClassLoader().getResource(filePath);
			if (url == null) {
				throw new IOException("The file " + filePath + " not exists in resource and file ");
			}
			return new String(url.openStream().readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	/**
	 * Read the zip/jar file binary (UTF8)
	 * 
	 * @param zipFilePath   the zip or jar file
	 * @param innerFileName zip or jar inner path and name
	 * @return the content of the file in the zip/jar
	 * @throws IOException
	 */
	public static byte[] readZipBytes(String zipFilePath, String innerFileName) throws IOException {
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(zipFilePath);
			Enumeration<? extends ZipEntry> list = zipFile.entries();

			while (list.hasMoreElements()) {
				ZipEntry ze = list.nextElement();
				if (ze.getName().equals(innerFileName)) {
					InputStream inputStream = zipFile.getInputStream(ze);
					return inputStream.readAllBytes();
				}
			}
			return null;
		} catch (IOException e) {
			throw e;
		} finally {
			if (zipFile != null) {
				try { // 关闭流
					zipFile.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/**
	 * Get the zip/jar file list
	 * 
	 * @param zipFilePath the zip or jar file
	 * @return The zip file list
	 * @throws IOException
	 */
	public static List<String> getZipList(String zipFilePath) throws IOException {
		List<String> al = new ArrayList<String>();
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(zipFilePath);
			Enumeration<? extends ZipEntry> list = zipFile.entries();

			while (list.hasMoreElements()) {
				ZipEntry ze = list.nextElement();
				al.add(ze.getName());
			}
			return al;
		} catch (IOException e) {
			throw e;
		} finally {
			if (zipFile != null) {
				try { // 关闭流
					zipFile.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public static String readZipText(String zipFilePath, String innerFileName) throws IOException {
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(zipFilePath);
			Enumeration<? extends ZipEntry> list = zipFile.entries();

			while (list.hasMoreElements()) {
				ZipEntry ze = list.nextElement();
				if (ze.getName().equals(innerFileName)) {
					InputStream inputStream = zipFile.getInputStream(ze);
					return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
				}
			}
			return null;
		} catch (IOException e) {
			throw e;
		} finally {
			if (zipFile != null) {
				try { // 关闭流
					zipFile.close();
				} catch (IOException e) {
				}
			}
		}

	}

	/**
	 * Copy file
	 * 
	 * @param fileFrom from
	 * @param fileTo   to
	 * @throws IOException
	 */
	public static void copyFile(String fileFrom, String fileTo) throws IOException {
		Files.copy(Path.of(fileFrom), Path.of(fileTo), StandardCopyOption.REPLACE_EXISTING);
	}

	/**
	 * Compress the file with ZIP
	 * 
	 * @param filePath the file name and path
	 * @return the ZIP file name and path
	 * @throws IOException
	 */
	public static String zipFile(String filePath) throws IOException {
		int BUFFER = 4096;
		String zipFileName = filePath + ".zip";
		BufferedInputStream origin = null;
		FileOutputStream dest = new FileOutputStream(zipFileName);
		ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
		byte data[] = new byte[BUFFER];
		File f = new File(filePath);

		FileInputStream fi = new FileInputStream(f);
		origin = new BufferedInputStream(fi, BUFFER);
		ZipEntry entry = new ZipEntry(f.getName());
		out.putNextEntry(entry);
		int count;
		while ((count = origin.read(data, 0, BUFFER)) != -1) {
			out.write(data, 0, count);
		}
		origin.close();
		out.close();
		return zipFileName;
	}

	/**
	 * Compress the path with ZIP, exclude sub directories
	 * 
	 * @param path the path
	 * @return zipFileName the ZIP file name and path
	 * @throws IOException
	 */
	public static String zipPath(String path) throws IOException {
		File f = new File(path);
		String zipFileName = f.getPath() + ".zip";
		if (f.isFile()) {
			return zipFile(path);
		}
		zipFiles(f.listFiles(), zipFileName);
		return zipFileName;
	}

	/**
	 * Compress the files with ZIP
	 * 
	 * @param files       the file path and name array
	 * @param zipFileName the ZIP file path and name
	 * @throws IOException
	 */
	public static void zipFiles(String[] files, String zipFileName) throws IOException {
		File[] file = new File[files.length];
		for (int i = 0; i < files.length; i++) {
			file[i] = new File(files[i]);
		}
		zipFiles(file, zipFileName);
	}

	/**
	 * Compress all files in the root directory, including sub directories
	 * 
	 * @param pathRoot    the root directory
	 * @param zipFileName the compressed ZIP file path and name
	 * @throws IOException
	 */
	public static void zipPaths(String pathRoot, String zipFileName) throws IOException {
		FileOutputStream dest = new FileOutputStream(zipFileName);
		ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
		File root = new File(pathRoot);
		zipPathFiles(out, root, root.getAbsolutePath());
		out.close();
	}

	/**
	 * Compress path and files, include sub directories
	 * 
	 * @param out                ZipOutputStream
	 * @param parent             the root path
	 * @param zipReplaceRootPath replace the ZIP entry name prefix
	 * @throws IOException
	 */
	private static void zipPathFiles(ZipOutputStream out, File parent, String zipReplaceRootPath) throws IOException {
		int BUFFER = 1024 * 100;// 100k
		byte data[] = new byte[BUFFER];
		File[] files = parent.listFiles();
		for (int i = 0; i < files.length; i++) {
			File f1 = files[i];
			if (f1.isDirectory()) {
				// Recursive
				zipPathFiles(out, f1, zipReplaceRootPath);
				continue;
			}
			FileInputStream fi = new FileInputStream(f1);
			BufferedInputStream origin = new BufferedInputStream(fi, BUFFER);
			String entryName = f1.getAbsolutePath().replace(zipReplaceRootPath + File.separator, "");

			if (File.separator.equals("\\")) {
				// 替换windows目录格式为unix
				entryName = entryName.replace("\\", "/");
			}
			ZipEntry entry = new ZipEntry(entryName);
			out.putNextEntry(entry);
			int count;
			while ((count = origin.read(data, 0, BUFFER)) != -1) {
				out.write(data, 0, count);
			}
			origin.close();
		}
	}

	/**
	 * Compress files
	 * 
	 * @param files       the files array
	 * @param zipFileName the compressed ZIP file path and name
	 * @throws IOException
	 */
	public static void zipFiles(File[] files, String zipFileName) throws IOException {
		int BUFFER = 4096;
		FileOutputStream dest = new FileOutputStream(zipFileName);
		ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
		byte data[] = new byte[BUFFER];
		for (int i = 0; i < files.length; i++) {
			File f1 = files[i];
			if (f1.isDirectory()) {
				continue;
			}
			FileInputStream fi = new FileInputStream(f1);
			BufferedInputStream origin = new BufferedInputStream(fi, BUFFER);
			ZipEntry entry = new ZipEntry(f1.getName());
			out.putNextEntry(entry);
			int count;
			while ((count = origin.read(data, 0, BUFFER)) != -1) {
				out.write(data, 0, count);
			}
			origin.close();

		}
		out.close();
	}

	/**
	 * Unzip files to the zipFilePath.xxx.unzip dir
	 * 
	 * @param zipFilePath the zip file path and name
	 * @return list of unziped files
	 * @throws IOException
	 */
	public static List<String> unZipFile(String zipFilePath) throws IOException {
		String unzipPath = "";
		for (int i = 0; i < 100; i++) {
			String unPath = zipFilePath + "_" + i + ".unzip";
			File file1 = new File(unPath);
			if (file1.exists()) {
				continue;
			} else {
				if (file1.mkdir()) {
					unzipPath = unPath;
					break;
				} else {
					continue;
				}
			}
		}

		if (unzipPath.length() == 0) {
			throw new IOException("Unable to create unzip directory");
		}

		return unZipFile(zipFilePath, unzipPath);
	}

	/**
	 * Unzip files to the target dir
	 * 
	 * @param zipFilePath the zip file path and name
	 * @param targetPath  the unziped dir
	 * @return list of unziped files
	 * @throws IOException
	 */
	public static List<String> unZipFile(String zipFilePath, String targetPath) throws IOException {

		File target = new File(targetPath);
		if (!target.exists()) {
			target.mkdirs();
		}
		targetPath = target.getAbsolutePath();

		String path = targetPath + File.separator;

		Enumeration<?> entries;
		ZipFile zipFile;
		zipFile = new ZipFile(zipFilePath);
		entries = zipFile.entries();
		List<String> fileList = new ArrayList<String>();
		while (entries.hasMoreElements()) {
			ZipEntry entry = (ZipEntry) entries.nextElement();

			if (entry.isDirectory()) {
				continue;
			}
			try {
				// Prevent Zip Slip: validate the resolved path stays within targetPath
				File destFile = new File(targetPath, entry.getName());
				String canonicalTarget = new File(targetPath).getCanonicalPath();
				String canonicalDest = destFile.getCanonicalPath();
				if (!canonicalDest.startsWith(canonicalTarget + File.separator)
						&& !canonicalDest.equals(canonicalTarget)) {
					throw new IOException("Zip entry escapes target directory: " + entry.getName());
				}

				UFile.buildPaths(destFile.getParentFile().getAbsolutePath());

				copyInputStream(zipFile.getInputStream(entry),
						new BufferedOutputStream(new FileOutputStream(destFile)));
				fileList.add(destFile.getAbsolutePath());
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
		zipFile.close();
		return fileList;
	}

	private static final void copyInputStream(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[8192];
		int len;

		while ((len = in.read(buffer)) >= 0)
			out.write(buffer, 0, len);

		in.close();
		out.close();
	}

	/**
	 * Rename the file
	 *
	 * @param sourcePathAndName the source file name and path
	 * @param newName           new file name (exclude path)
	 */
	public static void renameFile(String sourcePathAndName, String newName) {
		File fFrom = new File(sourcePathAndName.replace("|", "/"));
		if (!fFrom.isAbsolute()) {
			fFrom = new File(UPath.getScriptPath(), sourcePathAndName.replace("|", "/"));
		}
		String to = fFrom.getParent() + "/" + newName;
		File fTo = new File(to);
		fFrom.renameTo(fTo);
	}

	/**
	 * Create a hash file based on the content
	 * 
	 * @param content     the content
	 * @param ext         the hash file extension
	 * @param path        the directory where the hash file is saved
	 * @param isOverWrite whether to overwrite the hash file
	 * @return the hash file name, exclude path
	 * @throws Exception
	 */
	public static String createHashTextFile(String content, String ext, String path, boolean isOverWrite)
			throws Exception {
		String hash = "t_" + Utils.md5(content); // MD5 avoids hashCode collisions
		path = path.trim() + "/";

		if (!buildPaths(path)) {
			throw new Exception("Can't create the directory (" + path + ")");
		}

		String fileName = hash + "." + ext.trim().toLowerCase();
		String filePath = path + fileName;
		File img = new File(filePath);
		if (isOverWrite || (!isOverWrite && !img.exists())) {
			createNewTextFile(filePath, content);
		}
		return fileName;
	}

	/**
	 * Create a new text file
	 * 
	 * @param fileName the saved file name and directory
	 * @param content  the saved text content (UTF8)
	 * @throws IOException
	 */
	public static void createNewTextFile(String fileName, String content) throws IOException {
		File file = new File(fileName);
		if (!file.getParentFile().exists()) {
			UFile.buildPaths(file.getParent());
		}
		Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);

	}

	/**
	 * Create a binary file based on the MD5 of the binary content, and the saved
	 * file name is MD5 + extension
	 * 
	 * @param bytes       The binary content
	 * @param ext         The saved file extension
	 * @param path        The saved directory
	 * @param isOverWrite Whether to overwrite
	 * @return The saved file name (MD5 + extension), exclude directory
	 * @throws Exception
	 */
	public static String createMd5File(byte[] bytes, String ext, String path, boolean isOverWrite) throws Exception {
		String md5 = Utils.md5(bytes);
		String s1 = createMd5File(bytes, md5, ext, path, isOverWrite);
		return s1;
	}

	/**
	 * Create a binary file based on the MD5 of the binary content, and the saved
	 * file name is MD5 + extension
	 * 
	 * @param bytes       The binary content
	 * @param md5         The md5
	 * @param ext         The saved file extension
	 * @param path        The saved directory
	 * @param isOverWrite Whether to overwrite
	 * @return The saved file name (MD5 + extension), exclude directory
	 * @throws Exception
	 */
	public static String createMd5File(byte[] bytes, String md5, String ext, String path, boolean isOverWrite)
			throws Exception {

		path = path.trim() + "/";

		if (!buildPaths(path)) {
			throw new Exception("Can't create the directory (" + path + ")");
		}

		String fileName = md5 + "." + ext.trim().toLowerCase();
		String filePath = path + fileName;
		createBinaryFile(filePath, bytes, isOverWrite);
		return fileName;
	}

	/**
	 * Get the MD5 of the file
	 * 
	 * @param file the file
	 * @return the MD5 of the file (hex)
	 */
	public static String createMd5(File file) {
//		try {
//			byte[] bytes = readFileBytes(file.getPath());
//			return Utils.md5(bytes);
//		} catch (Exception e) {
//			return e.getMessage();
//		}
//		
		// 避免内存溢出，按照流进行md5
		return md5(file);
	}

	/**
	 * Get the MD5 of the file
	 * 
	 * @param file the file
	 * @return the MD5 of the file (hex)
	 */
	public static String md5(File file) {
		return digestFile(file, "MD5");

	}

	/**
	 * Get the MD5 of the file
	 * 
	 * @param file the file
	 * @return the MD5 of the file (hex)
	 */
	public static String sha1(File file) {
		return digestFile(file, "SHA1");
	}

	/**
	 * Get the MD5 of the file
	 * 
	 * @param file the file
	 * @return the MD5 of the file (hex)
	 */
	public static String sha256(File file) {
		return digestFile(file, "SHA-256");
	}

	/**
	 * Get the digest of the file
	 * 
	 * @param file       the file
	 * @param digestName the digest name (MD2, MD5, SHA-1, SHA-224, SHA-256,
	 *                   SHA-384, SHA-512)
	 * @return the digest (hex)
	 */
	public static String digestFile(File file, String digestName) {

		FileInputStream fileInputStream = null;
		try {
			MessageDigest md5 = MessageDigest.getInstance(digestName);
			fileInputStream = new FileInputStream(file);
			byte[] buffer = new byte[1024 * 64]; // 64k
			int length;
			while ((length = fileInputStream.read(buffer)) != -1) {
				md5.update(buffer, 0, length);
			}
			return Utils.bytes2hex(md5.digest());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (fileInputStream != null) {
					fileInputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Create a binary file
	 * 
	 * @param path        The file name and path to be created
	 * @param bytes       The saved binary
	 * @param isOverWrite Whether to overwrite
	 * @throws Exception
	 */
	public static void createBinaryFile(String path, byte[] bytes, boolean isOverWrite) throws Exception {
		File img = new File(path);
		UFile.buildPaths(img.getParent());
		if (isOverWrite || (!isOverWrite && !img.exists())) {
			Files.write(img.toPath(), bytes);
		}
	}

	/**
	 * Convert GZIP compressed BASE64 encoded string into a file
	 * 
	 * @param base64String the BASE64 encoded string
	 * @param ext          The file extension
	 * @param path         The directory of the file is saved
	 * @param isOverWrite  Whether to overwrite
	 * @return the create file name, exclude path
	 * @throws Exception
	 */
	public static String createUnGZipHashFile(String base64String, String ext, String path, boolean isOverWrite)
			throws Exception {
		path = path.trim() + "/";
		String hash = base64String.hashCode() + "";
		String fileName = hash + "." + ext.toLowerCase();
		String filePath = path.trim() + "/" + fileName;
		File f = new File(filePath);
		if (f.exists()) {
			return fileName;
		}

		byte[] bytes = UConvert.FromBase64String(base64String);

		ByteArrayInputStream in0 = new ByteArrayInputStream(bytes);
		GZIPInputStream inGZip = new GZIPInputStream(in0);

		int BUFFER = 4096;

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		byte[] data = new byte[BUFFER];
		int count;
		while ((count = inGZip.read(data, 0, BUFFER)) != -1) {
			out.write(data, 0, count);
		}

		inGZip.close();

		createBinaryFile(filePath, out.toByteArray(), isOverWrite);
		return fileName;
	}

	/**
	 * Split file name or directory name based on the name
	 * 
	 * @param name name
	 * @param len  split length
	 * @return the result
	 */
	public static String createSplitDirPath(String name, int len) {
		if (name == null || name.length() <= len) {
			return name;
		}
		if (len <= 0) {
			len = 2;
		}
		String name1 = name.trim().replace(" ", "_");
		name1 = name1.replace("?", "_");
		name1 = name1.replace("|", "_");
		name1 = name1.replace("*", "_");
		name1 = name1.replace("/", "_");
		name1 = name1.replace("\\", "_");
		name1 = name1.replace(".", "gdx");
		MStr s = new MStr();
		while (name1.length() > 0) {
			String tmp = name1.substring(0, len);
			name1 = name1.substring(len);
			s.a(tmp + "/");
			if (name1.length() <= len) {
				s.a(name1 + "/");
				break;
			}
		}
		return s.toString();
	}

	/**
	 * Create the paths
	 * 
	 * @param path the path
	 * @return result
	 */
	public static boolean buildPaths(String path) {
		File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return dir.exists();
	}
}
