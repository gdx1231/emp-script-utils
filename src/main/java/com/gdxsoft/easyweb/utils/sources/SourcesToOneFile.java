/**
 * 
 */
package com.gdxsoft.easyweb.utils.sources;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UFileFilter;

/**
 * @author admin
 *
 */
public class SourcesToOneFile {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String root = args[0];
		String[] exts = args[1].split(",");
		String outFilePath = args[2];

		SourcesToOneFile t = new SourcesToOneFile(root, exts, outFilePath);
		if (args.length == 5) {
			int pageSize = Integer.parseInt(args[3]);
			int pages = Integer.parseInt(args[4]);

			t.setPages(pages); // 共多少页
			t.setPageSize(pageSize); // 每页的行数

		}
		try {
			t.start();
		} catch (IOException e) {
			System.out.println(e.getLocalizedMessage());
		}
	}

	private String root;
	private File fRoot;
	private String[] exts = {};
	private FilenameFilter ff;
	private String oneFilePath;
	private int pages = -1;
	private int pageSize = -1;

	private int totalLines = 0;

	private StringBuilder text = new StringBuilder();

	public SourcesToOneFile(String root, String[] exts, String oneFilePath) {
		this.exts = exts;
		this.ff = UFileFilter.getInstance(this.exts);
		this.oneFilePath = oneFilePath;
		this.root = root;
		this.fRoot = new File(this.root);
	}

	public void start() throws IOException {
		System.out.println("开始合并：" + fRoot);
		this.walkFiles(fRoot);
		UFile.createNewTextFile(oneFilePath, text.toString());
		System.out.println("开始完毕：" + fRoot);
	}

	public void walkFiles(File parent) throws IOException {
		int needlines = this.pages * this.pageSize;
		if (needlines > 0 && this.totalLines > needlines) {
			return;
		}
		File[] sources = parent.listFiles(ff);
		if (sources != null) {
			int prefixLength = fRoot.getAbsolutePath().length();
			for (int i = 0; i < sources.length; i++) {
				File source = sources[i];
				if (!source.isFile()) {
					continue;
				}

				String name = source.getAbsolutePath().substring(prefixLength);
				text.append("\n文件：").append(name).append("\n");
				this.totalLines++;
				
				String sourceContent = UFile.readFileText(source.getAbsolutePath());
				sourceContent = removeBlankLines(sourceContent);
				text.append(sourceContent);
				System.out.println("写入文件：" + name+", "+this.totalLines);

				if (needlines > 0 && this.totalLines > needlines) {
					return;
				}
			}
		}
		File[] files = parent.listFiles();
		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			if (f.isDirectory()) {
				walkFiles(f);
			}
		}

	}

	private String removeBlankLines(String sourceContent) {
		StringBuilder sb1 = new StringBuilder();
		sourceContent = sourceContent.replace("\r\n", "\n");
		String[] lines = sourceContent.split("\n");
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			if (line.trim().length() == 0) {
				continue;
			}
			sb1.append(line).append("\n");
			totalLines++;
		}

		return sb1.toString().trim();
	}

	public String getRoot() {
		return root;
	}

	public void setRoot(String root) {
		this.root = root;
	}

	public File getfRoot() {
		return fRoot;
	}

	public void setfRoot(File fRoot) {
		this.fRoot = fRoot;
	}

	public String[] getExts() {
		return exts;
	}

	public void setExts(String[] exts) {
		this.exts = exts;
	}

	public String getOneFilePath() {
		return oneFilePath;
	}

	public void setOneFilePath(String oneFilePath) {
		this.oneFilePath = oneFilePath;
	}

	public int getPages() {
		return pages;
	}

	public void setPages(int pages) {
		this.pages = pages;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

}
