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

		SourcesToOneFile t = new SourcesToOneFile(args[0], args[1].split(","), args[2]);
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
		File[] sources = parent.listFiles(ff);
		if (sources != null) {
			int prefixLength = fRoot.getAbsolutePath().length();
			for (int i = 0; i < sources.length; i++) {
				File source = sources[i];
				if (!source.isFile()) {
					continue;
				}

				String name = source.getAbsolutePath().substring(prefixLength);
				text.append("文件：").append(name).append("\n");
				String sourceContent = UFile.readFileText(source.getAbsolutePath());
				text.append(sourceContent);
				System.out.println("写入文件：" + name);
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

}
