package com.gdxsoft.easyweb.utils;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.RoundRectangle2D.Float;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.ImageIcon;

import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.conf.ConfExiftool;
import com.gdxsoft.easyweb.conf.ConfImageMagick;

/**
 * The image utils
 *
 */
public class UImages {
	public static int LIMIT_WIDTH = 8000;
	public static int LIMIT_HEIGHT = 8000;
	/**
	 * Default image quality
	 */
	public static int DEFAULT_QUALITY = 70;
	private static Logger LOGGER = LoggerFactory.getLogger(UImages.class);

	public final static String RESIZED_TAG = "$resized";

	/**
	 * 转义文件路径中的 shell 特殊字符（$ → \$），防止 sh -c 展开变量
	 */
	private static String escapeShellPath(String path) {
		return path.replace("$", "\\$");
	}

	/**
	 * 分解尺寸表达式
	 * 
	 * @param sizesExp 800x600,1024x768,1000x800 ...
	 * @return
	 */
	public static Dimension[] parseSizes(String sizesExp) {
		String[] sizes = sizesExp.split("\\,");
		List<Dimension> ds = new ArrayList<Dimension>();
		for (int i = 0; i < sizes.length; i++) {
			String s = sizes[i];
			Dimension d = parseSize(s);
			ds.add(d);
		}

		Dimension[] dd = new Dimension[ds.size()];
		dd = ds.toArray(dd);

		return dd;
	}

	/**
	 * Parse the size string to the dimension, filter invalid char
	 * 
	 * @param resize 800x600
	 * @return null or dimension
	 */
	public static Dimension parseSize(String resize) {
		if (resize == null || resize.isBlank()) {
			return null;
		}

		String[] s1 = resize.toLowerCase().split("x");
		if (s1.length != 2) {
			return null;
		}
		int w = 0;
		int h = 0;
		try {
			w = Integer.parseInt(s1[0]);
			h = Integer.parseInt(s1[1]);

			if (w > LIMIT_WIDTH || h > LIMIT_HEIGHT) {
				LOGGER.warn("Skip, size too big: {} ", resize);
				return null;
			}
			Dimension d = new Dimension(w, h);

			return d;
		} catch (Exception e) {
			LOGGER.warn("Skip, size parse error: {},{}", resize, e.getMessage());
			return null;
		}
	}

	/**
	 * Add a logo in the middle of a image
	 * 
	 * @param originalImage The original image
	 * @param logo          The logo image
	 * @param logoMaxWidth  The logo max width
	 * @param logoMaxHeight The logo max height
	 * @return the image with the logo
	 * @throws IOException
	 */
	public static BufferedImage appendLogo(BufferedImage originalImage, BufferedImage logo, int logoMaxWidth,
			int logoMaxHeight) throws IOException {

		Graphics2D g = originalImage.createGraphics();

		int[] newSize = getNewSize(logo, logoMaxWidth, logoMaxHeight);

		int logo_width = newSize[0];
		int logo_height = newSize[1];

		int logo_x = (originalImage.getWidth() - logo_width) / 2;
		int logo_y = (originalImage.getHeight() - logo_height) / 2;

		// 缩放logo尺寸和二维码logo要求尺寸一致
		BufferedImage logoResized = UImages.createResizedCopy(logo, logo_width, logo_height);

		int radius = logoResized.getWidth() * 40 / 200; // 200宽度 30圆角
		int border = logoResized.getWidth() * 2 / 100;

		// 创建带圆角和描边的图片
		BufferedImage logoWidthRadius = setRadius(logoResized, radius, border, radius / 2);

		// 创建半透明的背景图
		BufferedImage logoBack = createBackground(logoResized, radius, border, radius / 2);

		int back_diff = UConvert.ToInt32(String.valueOf(logo_height * 0.05));
		// 绘制半透明背景
		g.drawImage(logoBack, logo_x + back_diff, logo_y + back_diff, logo_width, logo_height, null);
		// 开始绘制logo图片
		g.drawImage(logoWidthRadius, logo_x, logo_y, logo_width, logo_height, null);
		g.dispose();
		logo.flush();

		return originalImage;
	}

	/**
	 * Set the rounded corners of the image
	 * 
	 * @param srcImage The source image
	 * @param radius   The radius size
	 * @param border   The border width
	 * @param padding  The padding size
	 * @return The result
	 * @throws IOException
	 */
	public static BufferedImage setRadius(BufferedImage srcImage, int radius, int border, int padding)
			throws IOException {
		int width = srcImage.getWidth();
		int height = srcImage.getHeight();
		int canvasWidth = width + padding * 2;
		int canvasHeight = height + padding * 2;

		BufferedImage image = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);

		Graphics2D gs = image.createGraphics();

		gs.setComposite(AlphaComposite.Src);

		gs.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Color col = new Color(255, 255, 255, 255);
		gs.setColor(col); // Color.WHITE

		int out_radius = UConvert.ToInt32(String.valueOf(radius * canvasWidth * 1.2 / width));
		Float fill = new RoundRectangle2D.Float(0, 0, canvasWidth, canvasHeight, out_radius, out_radius);
		gs.fill(fill);
		gs.setComposite(AlphaComposite.SrcAtop);

		gs.drawImage(setClip(srcImage, radius), padding, padding, null);
		if (border != 0) {
			gs.setColor(Color.LIGHT_GRAY);
			gs.setStroke(new BasicStroke(border));
			gs.drawRoundRect(padding, padding, canvasWidth - 2 * padding, canvasHeight - 2 * padding, radius, radius);
		}
		gs.dispose();
		return image;
	}

	/**
	 * Create a background image
	 * 
	 * @param srcImage The source image
	 * @param radius   The radius size
	 * @param border   The border width
	 * @param padding  The padding size
	 * @return The result
	 */
	public static BufferedImage createBackground(BufferedImage srcImage, int radius, int border, int padding) {
		int width = srcImage.getWidth();
		int height = srcImage.getHeight();
		int canvasWidth = width + padding * 2;
		int canvasHeight = height + padding * 2;

		BufferedImage image = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);

		Graphics2D gs = image.createGraphics();

		gs.setComposite(AlphaComposite.Src);

		gs.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Color col = new Color(15, 15, 15, 67);
		gs.setColor(col); // Color.WHITE

		int out_radius = UConvert.ToInt32(String.valueOf(radius * canvasWidth * 1.2 / width));
		Float fill = new RoundRectangle2D.Float(0, 0, canvasWidth, canvasHeight, out_radius, out_radius);
		gs.fill(fill);
		gs.setComposite(AlphaComposite.SrcAtop);

		gs.drawImage(setClip(srcImage, radius), padding, padding, null);
		gs.dispose();
		return image;
	}

	/**
	 * Set image radius
	 * 
	 * @param srcImage The source image
	 * @param radius   The radius size
	 * @return the result
	 */
	public static BufferedImage setClip(BufferedImage srcImage, int radius) {
		int width = srcImage.getWidth();
		int height = srcImage.getHeight();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D gs = image.createGraphics();

		gs.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		gs.setClip(new RoundRectangle2D.Double(0, 0, width, height, radius, radius));
		gs.drawImage(srcImage, 0, 0, null);
		gs.dispose();
		return image;
	}

	/**
	 * Create the image thumbnail
	 * 
	 * @param imgPath   The image path and name
	 * @param maxwidth  The maximum width
	 * @param maxheight The maximum height
	 * @return The thumbnail image path and name
	 */
	public static String createSmallImage(String imgPath, int maxWidth, int maxHeight) {
		return createSmallImage(imgPath, maxWidth, maxHeight, "jpg", DEFAULT_QUALITY);

	}

	/**
	 * Create the image thumbnail
	 * 
	 * @param imgPath   The image path and name
	 * @param maxWidth  The maximum width
	 * @param maxHeight The maximum height
	 * @param outputExt The output image extension, e.g. jpg or png or webp ...
	 * @param quality   The image quality
	 * @return The thumbnail image path and name
	 */
	public static String createSmallImage(String imgPath, int maxWidth, int maxHeight, String outputExt, int quality) {
		Dimension[] d = new Dimension[1];
		d[0] = new Dimension();
		d[0].setSize(maxWidth, maxHeight);

		try {
			File[] fs = createResized(imgPath, d, outputExt, quality);
			if (fs.length == 0) {
				return null;
			} else {
				return fs[0].getAbsolutePath();
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return null;
		}

	}

	/**
	 * Create the image thumbnail (Using java)
	 * 
	 * @param imgPath       The image path and name
	 * @param thumbnilsSize The thumbnail sizes (800x600, 400x300)...
	 * @return The thumbnail files array
	 * @throws Exception
	 */
	public static File[] createResizedByJava(String imgPath, java.awt.Dimension[] thumbnilsSize) throws Exception {
		String[] exts = { ".jpg", ".jpeg", ".png", ".bmp", ".gif" };
		if (!UCheckerIn.endsWith(imgPath, exts, true)) {
			LOGGER.error("Invalid image ext: {}", imgPath);
			throw new Exception("Invalid image ext: " + imgPath);
		}

		File img = new File(imgPath);
		BufferedImage bufferedImage = null;
		try {
			bufferedImage = getBufferedImage(imgPath);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			throw e;
		}
		File[] names = new File[thumbnilsSize.length];
		String path = UImages.getResizedPath(img);
		File pathResized = new File(path);
		pathResized.mkdirs();

		for (int i = 0; i < thumbnilsSize.length; i++) {
			double width = thumbnilsSize[i].getWidth();
			double height = thumbnilsSize[i].getHeight();
			String name = (int) width + "x" + (int) height + ".jpg";
			File f1 = new File(pathResized.getPath() + "/" + name);

			int[] newSize = getNewSize(bufferedImage, (int) width, (int) height);

			int w = newSize[0];
			int h = newSize[1];
			try {
				BufferedImage buf = createResizedCopy(bufferedImage, w, h);
				imageSave(buf, f1);
				buf = null;
				names[i] = f1;
			} catch (Exception e) {
				LOGGER.error(e.getMessage());
				names[i] = null;
			}
		}
		if (bufferedImage != null)
			bufferedImage = null;
		return names;
	}

	/**
	 * Get the image new size according the maximum width and maximum height limits
	 * 
	 * @param bufferedImage The image
	 * @param maxWidth      Maximum width
	 * @param maxHeight     Maximum height
	 * @return size (width , height)
	 */
	public static int[] getNewSize(BufferedImage bufferedImage, int maxWidth, int maxHeight) {
		double wScale = maxWidth * 1.0 / bufferedImage.getWidth();
		double hScale = maxHeight * 1.0 / bufferedImage.getHeight();
		double width = maxWidth, height = maxHeight;
		if (wScale > hScale) {
			width = hScale * bufferedImage.getWidth();
		} else {
			height = bufferedImage.getHeight() * wScale;
		}

		int w = (int) width;
		int h = (int) height;

		int[] rets = { w, h };

		return rets;
	}

	/**
	 * Create the image thumbnails
	 * 
	 * @param imgPath       The image path and name
	 * @param thumbnilsSize The thumbnails size(800x600, 400x300)...
	 * @return The thumbnail files array
	 * @throws Exception
	 */
	public static File[] createResized(String imgPath, Dimension[] thumbnilsSize) throws Exception {
		String outputExt = "jpg";
		return createResized(imgPath, thumbnilsSize, outputExt, DEFAULT_QUALITY);
	}

	/**
	 * Create the image thumbnails
	 * 
	 * @param imgPath       The image path and name
	 * @param thumbnilsSize The thumbnails size(800x600, 400x300)...
	 * @param outputExt     output image ext, e.g. jpg or png
	 * @param quality       The image quality
	 * @return The thumbnail files array
	 * @throws Exception
	 */
	public static File[] createResized(String imgPath, Dimension[] thumbnilsSize, String outputExt, int quality)
			throws Exception {
		if (checkImageMagick()) {
			return createResizedByImageMagick(imgPath, thumbnilsSize, outputExt, quality);
		} else {
			// 利用 net.coobird.thumbnailator.Thumbnails，
			return createResizedByThumbnails(imgPath, thumbnilsSize, outputExt, quality);
		}
	}

	/**
	 * Check whether using ImageMagick to resize image
	 * 
	 * @return
	 */
	public static boolean checkImageMagick() {
		ConfImageMagick conf = ConfImageMagick.getInstance();
		if (conf == null || conf.getPath() == null) {
			return false;
		}
		File pathMagicHome = new File(conf.getPath());
		if (pathMagicHome.exists()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Get the imageMagick's magick path
	 * 
	 * @return
	 * @throws Exception
	 */
	public static String getImageMagick() throws Exception {
		ConfImageMagick conf = ConfImageMagick.getInstance();
		if (conf == null) {
			String err = "imageMagick not found in the ewa_conf.xml, e.g. 《imageMagick path=\"D:/ImageMagick/\" /》";
			LOGGER.error(err);
			throw new Exception(err);

		}
		String magicHome = conf.getPath();

		File home = new File(magicHome);
		if (home.isFile()) {
			String err = "[" + home.getAbsolutePath() + "] is a file, should be ImageMagick installed path";
			LOGGER.error(err);
			throw new Exception(err);
		}
		String command_line;
		String command_line1;
		String os = System.getProperty("os.name").toLowerCase();
		if (os.startsWith("windows")) {
			command_line = home.getAbsolutePath() + "\\magick.exe";
			command_line1 = home.getAbsolutePath() + "\\convert.exe";
		} else {
			command_line = home.getAbsolutePath() + "/magick";
			command_line1 = home.getAbsolutePath() + "/convert";
		}

		// 检查magick文件是否存在
		File f2 = new File(command_line);
		if (!f2.exists()) {
			f2 = new File(command_line1); // convert legacy
		}
		if (!f2.exists()) {
			String err = "Not found [" + f2.getAbsolutePath() + "]";
			LOGGER.error(err);
			throw new Exception(err);
		}

		return f2.getAbsolutePath();
	}

	/**
	 * Create the image thumbnails (Using the ImageMagick)
	 * 
	 * @param imgPath       the image path and name
	 * @param thumbnilsSize The thumbnails size(800x600, 400x300)...
	 * @return The thumbnail files array
	 * @throws Exception
	 */
	public static File[] createResizedByImageMagick(String imgPath, Dimension[] thumbnilsSize) throws Exception {
		return createResizedByImageMagick(imgPath, thumbnilsSize, "jpg", DEFAULT_QUALITY);

	}

	/**
	 * Get the thumbnail path (e.g. /var/temp/pic_xxx.jpg$resized)
	 * 
	 * @param img the original image
	 * @return the thumbnail path
	 */
	public static String getResizedPath(File img) {
		String path = img.getAbsolutePath() + RESIZED_TAG;
		return path;
	}

	/**
	 * Get the thumbnail name (width + "x" + height + "." + outputExt)
	 * 
	 * @param size      the thumbnail size (800x600)
	 * @param outputExt the ext(jpg, png, webp ...)
	 * @return the thumbnail name
	 */
	public static String getResizedImageName(Dimension size, String outputExt) {
		double width = size.getWidth();
		double height = size.getHeight();
		return getResizedImageName((int) width, (int) height, outputExt);
	}

	/**
	 * Get the thumbnail name (width + "x" + height + "." + outputExt)
	 * 
	 * @param width     the thumbnail width
	 * @param height    the thumbnail height
	 * @param outputExt the ext(jpg, png, webp ...)
	 * @return the thumbnail name
	 */
	public static String getResizedImageName(int width, int height, String outputExt) {
		String name = width + "x" + height + "." + outputExt;
		return name;
	}

	/**
	 * Get the thumbnail full path
	 * 
	 * @param img       the original image
	 * @param width     the thumbnail width
	 * @param height    the thumbnail height
	 * @param outputExt the ext(jpg, png, webp ...)
	 * @return the thumbnail full path
	 */
	public static String getResizedImageName(File img, int width, int height, String outputExt) {
		String resizedPath = getResizedPath(img);
		String name = getResizedImageName(width, height, outputExt);
		return resizedPath + File.separator + name;
	}

	/**
	 * Convert image format, same path
	 * 
	 * @param imgPath   the original image
	 * @param outputExt the new format (.avif, .jpg, .png, .bmp, .gif, .webp, .heic)
	 * @param quality   the new format quality (1-100)
	 * @return the new image path
	 * @throws Exception
	 */
	public static String convertSamePath(String imgPath, String outputExt, int quality) throws Exception {
		String outPath = UFile.changeFileExt(imgPath, outputExt);

		return convert(imgPath, outPath, quality);
	}

	/**
	 * Convert image format
	 * 
	 * @param imgPath    the original image
	 * @param newImgPath the new image
	 * @param quality    the new format quality (1-100)
	 * @return the new image path
	 * @throws Exception
	 */
	public static String convert(String imgPath, String newImgPath, int quality) throws Exception {
		String[] exts = { ".avif", ".jpg", ".jpeg", ".jiff", ".png", ".bmp", ".gif", ".webp", ".heic" };
		if (!UCheckerIn.endsWith(imgPath, exts, true)) {
			LOGGER.error("Invalid image ext: {}", imgPath);
			throw new Exception("Invalid image ext: " + imgPath);
		}
		String newExt = UFile.getFileExt(newImgPath);
		if (!UCheckerIn.endsWith("." + newExt, exts, true)) {
			LOGGER.error("Invalid image output ext: {}", newExt);
			throw new Exception("Invalid image output ext: " + newExt);
		}

		String command_line = getImageMagick();
		StringBuilder cmd = new StringBuilder();
		cmd.append("\"").append(command_line).append("\"");
		// -strip 删除配置文件和注释
		if (command_line.endsWith("convert") || command_line.endsWith("convert.exe")) {
			// legacy
			cmd.append(" -auto-orient -strip");
		} else {
			cmd.append(" convert -auto-orient -strip");
		}
		if (quality > 0) {
			cmd.append(" -quality " + quality + "% ");
		}
		cmd.append("\"").append(escapeShellPath(imgPath)).append("\" \"").append(escapeShellPath(newImgPath)).append("\"");

		HashMap<String, String> rst = runImageMagick(cmd.toString());
		if (rst.get("RST").equals("true")) {
			return newImgPath;
		} else {
			throw new Exception(rst.get("ERR"));
		}
	}

	/**
	 * Create the image thumbnails (Using the ImageMagick)
	 * 
	 * @param imgPath       the image path and name
	 * @param thumbnilsSize The thumbnails size(800x600, 400x300)...
	 * @param outputExt     jpg or png or webp or heic ...
	 * @param quality       jpg quality
	 * @return The thumbnail files array
	 * @throws Exception
	 */
	public static File[] createResizedByImageMagick(String imgPath, Dimension[] thumbnilsSize, String outputExt,
			int quality) throws Exception {
		String[] exts = { ".avif", ".jpg", ".jpeg", ".jiff", ".png", ".bmp", ".gif", ".webp", ".heic" };
		if (!UCheckerIn.endsWith(imgPath, exts, true)) {
			LOGGER.error("Invalid image ext: {}", imgPath);
			throw new Exception("Invalid image ext: " + imgPath);
		}

		if (!UCheckerIn.endsWith("." + outputExt, exts, true)) {
			LOGGER.error("Invalid image output ext: {}", outputExt);
			throw new Exception("Invalid image output ext: " + outputExt);
		}

		String command_line = getImageMagick();
		StringBuilder cmd = new StringBuilder();
		// -strip 删除配置文件和注释
		if (command_line.endsWith("convert") || command_line.endsWith("convert.exe")) {
			// legacy
			cmd.append(" -auto-orient -strip");
		} else {
			// magick convert -resize "100x100" -strip DSC_0963.JPG aa1.jpg
			cmd.append(" convert -auto-orient -strip");
		}
		if (quality > 0) {
			cmd.append(" -quality " + quality + "%");
		}
		cmd.append(" -resize ");

		File img = new File(imgPath);

		File[] names = new File[thumbnilsSize.length];
		String path = UImages.getResizedPath(img);
		File pathResized = new File(path);
		pathResized.mkdirs();

		for (int i = 0; i < thumbnilsSize.length; i++) {
			double width = thumbnilsSize[i].getWidth();
			double height = thumbnilsSize[i].getHeight();
			int w = (int) width;
			int h = (int) height;
			String name = getResizedImageName(thumbnilsSize[i], outputExt);
			File f1 = new File(pathResized.getPath() + "/" + name);

			StringBuilder sb = new StringBuilder();
			sb.append(command_line);
			sb.append(cmd);
			sb.append(" \"");
			sb.append(w);
			sb.append("x");
			sb.append(h);
			sb.append(">\" \"");
			sb.append(escapeShellPath(img.getAbsolutePath()));
			sb.append("\" \"");
			sb.append(escapeShellPath(f1.getPath()));
			sb.append("\"");
			String command_line1 = sb.toString();

			HashMap<String, String> rst = runImageMagick(command_line1);
			if (rst.get("RST").equals("true")) {
				names[i] = f1;
			} else {
				names[i] = null;
			}

		}

		return names;
	}

	/**
	 * Create the image thumbnails (using net.coobird.thumbnailator.Thumbnails)
	 * 
	 * @param imgPath       the image path and name
	 * @param thumbnilsSize The thumbnails size(800x600, 400x300)...
	 * @return The thumbnail files array
	 * @throws Exception
	 */
	public static File[] createResizedByThumbnails(String imgPath, Dimension[] thumbnilsSize) {

		return createResizedByThumbnails(imgPath, thumbnilsSize, "jpg", DEFAULT_QUALITY);
	}

	/**
	 * Create the image thumbnails (using net.coobird.thumbnailator.Thumbnails)
	 * 
	 * @param imgPath       the image path and name
	 * @param thumbnilsSize The thumbnails size(800x600, 400x300)...
	 * @param outputExt     output image ext, e.g. jpg or png
	 * @param quality       the image quality
	 * @return The thumbnail files array
	 * @throws Exception
	 */
	public static File[] createResizedByThumbnails(String imgPath, Dimension[] thumbnilsSize, String outputExt,
			int quality) {
		File img = new File(imgPath);
		String path = UImages.getResizedPath(img);
		File pathResized = new File(path);
		pathResized.mkdirs();

		File[] names = new File[thumbnilsSize.length];
		double quality1 = quality / 100.0;
		for (int i = 0; i < thumbnilsSize.length; i++) {
			double width = thumbnilsSize[i].getWidth();
			double height = thumbnilsSize[i].getHeight();
			int w = (int) width;
			int h = (int) height;
			String name = getResizedImageName(thumbnilsSize[i], outputExt);
			File f1 = new File(pathResized.getPath() + "/" + name);
			try {
				Thumbnails.of(img).size(w, h).outputFormat(outputExt).useExifOrientation(true).outputQuality(quality1)
						.toFile(f1);
				names[i] = f1;
			} catch (IOException e) {
				LOGGER.error(e.getMessage());
				names[i] = null;
			}
		}
		return names;
	}

	/**
	 * Get a BufferedImage from the image path and name
	 * 
	 * @param imgPath the image path and name
	 * @return BufferedImage the BufferedImage
	 * @throws IOException
	 */
	public static BufferedImage getBufferedImage(String imgPath) throws IOException {
		File img = new File(imgPath);
		// bufferedImage = ImageIO.read(img);
		// java上传图片，压缩、更改尺寸等导致变色（表层蒙上一层红色）
		// https://blog.csdn.net/qq_25446311/article/details/79140008
		Image image = Toolkit.getDefaultToolkit().getImage(img.getAbsolutePath());
		BufferedImage bufferedImage = toBufferedImage(image);
		return bufferedImage;
	}
	/**
	 * Get a BufferedImage from the image URL
	 * 
	 * @param imgUrl the image URL
	 * @return BufferedImage the BufferedImage
	 * @throws IOException
	 */
	public static BufferedImage getBufferedImage(URL imgUrl) throws IOException {
		Image image = Toolkit.getDefaultToolkit().getImage(imgUrl);
		BufferedImage bufferedImage = toBufferedImage(image);
		return bufferedImage;
	}

	/**
	 * Convert the Image to BufferedImage
	 * 
	 * @param image The source image
	 * @return BufferedImage the BufferedImage
	 */
	public static BufferedImage toBufferedImage(Image image) {
		if (image instanceof BufferedImage) {
			return (BufferedImage) image;
		}
		// This code ensures that all the pixels in the image are loaded
		image = new ImageIcon(image).getImage();
		BufferedImage bimage = null;
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		try {
			int transparency = Transparency.OPAQUE;
			GraphicsDevice gs = ge.getDefaultScreenDevice();
			GraphicsConfiguration gc = gs.getDefaultConfiguration();
			bimage = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null), transparency);
		} catch (HeadlessException e) {
			// The system does not have a screen
		}
		if (bimage == null) {
			// Create a buffered image using the default color model
			int type = BufferedImage.TYPE_INT_RGB;
			bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
		}
		// Copy image to buffered image
		Graphics g = bimage.createGraphics();
		// Paint the image onto the buffered image
		g.drawImage(image, 0, 0, null);
		g.dispose();
		return bimage;
	}

	/**
	 * Save the BufferedImage to file (JPEG, 0.8)
	 * 
	 * @param buf  the BufferedImage
	 * @param name the saved file path and name
	 * @throws IOException
	 */
	public static void imageSave(BufferedImage buf, String name) throws IOException {
		File f1 = new File(name);
		imageSave(buf, f1);
	}

	/**
	 * Save the BufferedImage to file (JPEG, 0.8)
	 * 
	 * @param buf the BufferedImage
	 * @param f1  the saved file
	 * @throws IOException
	 */
	public static void imageSave(BufferedImage buf, File f1) throws IOException {
		f1.getParentFile().mkdirs();
		byte[] bytes = getBytes(buf, "JPEG", 0.8f);
		try {
			UFile.createBinaryFile(f1.getAbsolutePath(), bytes, true);
		} catch (Exception err) {
			LOGGER.error(err.getMessage());
		}
	}

	/**
	 * Create an image according to the new size
	 * 
	 * @param originalImage The original image
	 * @param width         The new width
	 * @param height        The new height
	 * @return The new size image
	 */
	public static BufferedImage createResizedCopy(Image originalImage, int width, int height) {
		// 保持Png图片的透明背景属性
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gs = ge.getDefaultScreenDevice();
		GraphicsConfiguration gc = gs.getDefaultConfiguration();
		BufferedImage bufIma = gc.createCompatibleImage(width, height, Transparency.TRANSLUCENT);

		Graphics2D g1 = bufIma.createGraphics();
		g1.setComposite(AlphaComposite.Src);
		// 高保真缩放
		Image scaled = originalImage.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
		g1.drawImage(scaled, 0, 0, width, height, null);
		g1.dispose();
		return bufIma;
	}

	/**
	 * Crop the image
	 * 
	 * @param originalImage The original image
	 * @param left          The crop left
	 * @param top           The crop top
	 * @param right         The crop right
	 * @param bottom        The crop bottom
	 * @return new BufferedImage
	 */
	public static BufferedImage createClipCopy(Image originalImage, int left, int top, int right, int bottom) {
		int width = right - left;
		int height = bottom - top;
		ImageFilter cropFilter = new CropImageFilter(left, top, width, height);
		Image img = Toolkit.getDefaultToolkit()
				.createImage(new FilteredImageSource(originalImage.getSource(), cropFilter));
		BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics g = tag.getGraphics();
		g.drawImage(img, 0, 0, null); // 绘制小图
		g.dispose();
		return tag;
	}

	/**
	 * Convert to BufferedImge to file buffer
	 * 
	 * @param bi        The BufferedImage
	 * @param imageType The save to image type JPEG, PNG ...
	 * @return The file buffer
	 */
	public static byte[] getBytes(BufferedImage bi, String imageType, float quality) {
		ByteArrayOutputStream output = new ByteArrayOutputStream(10240);
		ImageWriter writer = null;
		String formatName = imageType;

		Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName(formatName);
		if (iter.hasNext()) {
			writer = (ImageWriter) iter.next();
		}
		if (writer == null) {
			return null;
		}

		IIOImage iioImage = new IIOImage(bi, null, null);
		ImageWriteParam param = writer.getDefaultWriteParam();
		if (formatName.equalsIgnoreCase("JPEG")) {
			param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			param.setCompressionQuality(quality);
		}

		try {
			// ImageIO.write(bi, imageType, output);
			ImageOutputStream outputStream = ImageIO.createImageOutputStream(output);
			writer.setOutput(outputStream);
			writer.write(null, iioImage, param);
			outputStream.close();
			byte[] buf = output.toByteArray();

			return buf;
		} catch (Exception err) {
			LOGGER.error(err.getMessage());
			return null;
		} finally {
			try {
				output.close();
			} catch (IOException e) {
				LOGGER.error(e.getMessage());
			}
		}
	}

	/**
	 * Execute the ImageMagick command in shell
	 *
	 * @param line The command
	 * @return the result
	 */
	private static HashMap<String, String> runImageMagick(String line) {
		HashMap<String, String> rst = new HashMap<String, String>();
		rst.put("CMD", line);

		LOGGER.info(line);
		try {
			ProcessBuilder pb = new ProcessBuilder("sh", "-c", line);
			if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
				pb = new ProcessBuilder("cmd", "/c", line);
			}
			pb.redirectErrorStream(true);
			Process process = pb.start();
			byte[] output = process.getInputStream().readAllBytes();
			boolean finished = process.waitFor(60, java.util.concurrent.TimeUnit.SECONDS);
			if (!finished) {
				process.destroyForcibly();
				rst.put("RST", "false");
				rst.put("ERR", "Command timed out after 60 seconds");
				return rst;
			}
			int exitCode = process.exitValue();
			String s = new String(output);
			if (exitCode == 0 || exitCode == 1) {
				rst.put("RST", "true");
				rst.put("MSG", s);
			} else {
				rst.put("RST", "false");
				rst.put("ERR", "Exit code: " + exitCode + ", " + s);
			}
		} catch (Exception e) {
			LOGGER.error(line);
			LOGGER.error(e.getMessage());
			rst.put("RST", "false");
			rst.put("ERR", e.getMessage());
		}
		return rst;
	}

	/**
	 * 向图片写入元数据。优先使用 exiftool（无损、支持 webp），exiftool 不可用时回退到
	 * ImageMagick。
	 * <p>
	 * 注意 ImageMagick 回退路径的固有限制：写入会重新编码图片（有损格式画质下降），
	 * 且 webp 的 EXIF 完全写不进去。生产环境建议安装 exiftool。
	 *
	 * @param imgPath  图片绝对路径（原地覆盖）
	 * @param metadata 键值对，如 {"UserComment": "提示词", "Artist": "adm=123", "Software": "DOUBAO_IMG/model"}
	 * @throws Exception 写入失败
	 */
	public static void writeMetadata(String imgPath, java.util.Map<String, String> metadata) throws Exception {
		if (metadata == null || metadata.isEmpty()) {
			return;
		}
		if (checkExiftool()) {
			writeMetadataByExiftool(imgPath, toExiftoolTags(metadata));
			return;
		}
		LOGGER.warn("exiftool not available, fall back to ImageMagick. "
				+ "The image will be re-encoded and webp EXIF can NOT be written.");
		writeMetadataByImageMagick(imgPath, metadata);
	}

	/**
	 * 把 ImageMagick 风格的键名转换为 exiftool 可写的标签名。<br>
	 * ImageMagick 的 PNG 专用前缀 tEXt/zTXt/iTXt 在 exiftool 中不可写，需要去掉前缀
	 * 交由 exiftool 按格式自动选择存储位置。
	 *
	 * @param metadata 原始键值对
	 * @return 转换后的键值对
	 */
	private static java.util.Map<String, String> toExiftoolTags(java.util.Map<String, String> metadata) {
		java.util.Map<String, String> rst = new java.util.LinkedHashMap<>();
		for (java.util.Map.Entry<String, String> e : metadata.entrySet()) {
			String key = e.getKey();
			if (key == null || key.trim().length() == 0) {
				continue;
			}
			key = key.trim();
			String lower = key.toLowerCase();
			if (lower.startsWith("text:") || lower.startsWith("ztxt:") || lower.startsWith("itxt:")) {
				key = key.substring(key.indexOf(':') + 1);
			}
			rst.put(key, e.getValue());
		}
		return rst;
	}

	/**
	 * 向图片写入元数据（EXIF/XMP/tEXt，由 ImageMagick 根据格式自动选择）。
	 * 使用 ProcessBuilder 参数数组方式执行，不经 shell，避免特殊字符转义问题。
	 * <p>
	 * 该方法会重新编码图片，有损格式（jpg/webp）画质会下降；且 ImageMagick 的 -set
	 * 对 webp 的 EXIF 无效（静默失败）。推荐使用
	 * {@link #writeMetadataByExiftool(String, java.util.Map)}。
	 *
	 * @param imgPath  图片绝对路径（原地覆盖）
	 * @param metadata 键值对，如 {"UserComment": "提示词", "Artist": "adm=123", "Software": "DOUBAO_IMG/model"}
	 * @throws Exception 写入失败
	 */
	public static void writeMetadataByImageMagick(String imgPath, java.util.Map<String, String> metadata)
			throws Exception {
		if (metadata == null || metadata.isEmpty()) return;
		String commandLine = getImageMagick();

		java.util.List<String> args = new java.util.ArrayList<>();
		args.add(commandLine);
		// IMv7 的 magick 不需要子命令；旧版 convert 需要
		if (commandLine.endsWith("convert") || commandLine.endsWith("convert.exe")) {
			// convert 模式，无需额外子命令
		} else {
			// magick 模式，需要 convert 子命令来执行图片处理
			args.add("convert");
		}
		args.add(imgPath);
		for (java.util.Map.Entry<String, String> e : metadata.entrySet()) {
			args.add("-set");
			args.add(e.getKey());
			args.add(e.getValue());
		}
		// 输出到临时文件再替换，避免读写同一文件的竞争
		String tmpOut = imgPath + ".meta_tmp";
		args.add(tmpOut);

		LOGGER.info("writeMetadataByImageMagick: {}", String.join(" ", args));
		ProcessBuilder pb = new ProcessBuilder(args);
		pb.redirectErrorStream(true);
		Process process = pb.start();
		byte[] output = process.getInputStream().readAllBytes();
		boolean finished = process.waitFor(30, java.util.concurrent.TimeUnit.SECONDS);
		if (!finished) {
			process.destroyForcibly();
			new File(tmpOut).delete();
			throw new Exception("writeMetadataByImageMagick timed out after 30 seconds");
		}
		int exitCode = process.exitValue();
		if (exitCode != 0 && exitCode != 1) {
			new File(tmpOut).delete();
			throw new Exception("writeMetadataByImageMagick failed exit=" + exitCode + ": " + new String(output));
		}
		// 替换原文件
		java.nio.file.Files.move(java.nio.file.Path.of(tmpOut), java.nio.file.Path.of(imgPath),
				java.nio.file.StandardCopyOption.REPLACE_EXISTING);
	}

	/**
	 * 向图片写入元数据（使用 exiftool），支持 webp/jpg/png/avif/heic/tiff/gif 等。
	 * <p>
	 * 相比 {@link #writeMetadataByImageMagick(String, java.util.Map)} 的优势：
	 * <ul>
	 * <li>不重新编码图片，像素完全无损（ImageMagick 方案每次写入都会有损重编码）</li>
	 * <li>webp 可以真正写入 EXIF（ImageMagick 的 -set 对 webp 完全无效）</li>
	 * <li>标签名支持 EXIF/XMP/IPTC 全命名空间</li>
	 * </ul>
	 * <p>
	 * 键名可带命名空间前缀，如 {@code EXIF:Artist}、{@code XMP:Description}、
	 * {@code IPTC:By-line}；不带前缀时由 exiftool 按格式自动选择写入位置。<br>
	 * 值为 null 或空字符串表示删除该标签。
	 *
	 * @param imgPath  图片绝对路径（原地覆盖）
	 * @param metadata 键值对，如 {"EXIF:Artist": "adm=123", "EXIF:UserComment": "提示词"}
	 * @throws Exception 写入失败
	 */
	public static void writeMetadataByExiftool(String imgPath, java.util.Map<String, String> metadata)
			throws Exception {
		if (metadata == null || metadata.isEmpty()) {
			return;
		}
		File img = new File(imgPath);
		if (!img.exists()) {
			String err = "Not found the image [" + imgPath + "]";
			LOGGER.error(err);
			throw new Exception(err);
		}

		List<String> args = new ArrayList<>();
		args.add(getExiftool());
		// 统一使用 UTF-8 处理标签值和文件名，避免中文乱码
		args.add("-charset");
		args.add("utf8");
		args.add("-charset");
		args.add("filename=utf8");
		// 原地覆盖，不生成 _original 备份，且保留原文件权限和属主
		args.add("-overwrite_original_in_place");
		for (java.util.Map.Entry<String, String> e : metadata.entrySet()) {
			String key = e.getKey();
			if (key == null || key.trim().length() == 0) {
				continue;
			}
			String val = e.getValue() == null ? "" : e.getValue();
			// -TAG=VALUE，值为空表示删除该标签
			args.add("-" + key.trim() + "=" + val);
		}
		args.add(imgPath);

		runExiftool(args);
	}

	/**
	 * 读取图片的元数据（使用 exiftool）
	 *
	 * @param imgPath 图片绝对路径
	 * @param keys    要读取的标签名，如 "EXIF:Artist"；为空则读取全部标签
	 * @return 标签名（不含命名空间前缀）到值的映射，读不到返回空 Map
	 * @throws Exception 读取失败
	 */
	public static java.util.Map<String, String> readMetadataByExiftool(String imgPath, String... keys)
			throws Exception {
		java.util.Map<String, String> rst = new java.util.LinkedHashMap<>();
		File img = new File(imgPath);
		if (!img.exists()) {
			String err = "Not found the image [" + imgPath + "]";
			LOGGER.error(err);
			throw new Exception(err);
		}

		List<String> args = new ArrayList<>();
		args.add(getExiftool());
		args.add("-charset");
		args.add("utf8");
		args.add("-charset");
		args.add("filename=utf8");
		// -s -s 输出 "TAG: VALUE" 短标签名格式，一行一个
		args.add("-s");
		args.add("-s");
		if (keys != null) {
			for (String key : keys) {
				if (key != null && key.trim().length() > 0) {
					args.add("-" + key.trim());
				}
			}
		}
		args.add(imgPath);

		String output = runExiftool(args);
		for (String line : output.split("\n")) {
			int p = line.indexOf(": ");
			if (p > 0) {
				rst.put(line.substring(0, p).trim(), line.substring(p + 2).trim());
			}
		}
		return rst;
	}

	/**
	 * Check whether exiftool is available (configured or in system PATH).
	 * <p>
	 * The result is cached and reused, and automatically invalidated when the
	 * ewa_conf.xml configuration file changes.
	 *
	 * @return true if exiftool is available
	 */
	public static boolean checkExiftool() {
		long currentPropTime = UPath.getPropTime();
		if (EXIFTOOL_AVAILABLE != null && currentPropTime == EXIFTOOL_AVAILABLE_PROP_TIME) {
			return EXIFTOOL_AVAILABLE.booleanValue();
		}
		boolean available;
		try {
			List<String> args = new ArrayList<>();
			args.add(getExiftool());
			args.add("-ver");
			runExiftool(args);
			available = true;
		} catch (Exception e) {
			LOGGER.warn("exiftool not available: {}", e.getMessage());
			available = false;
		}
		// Re-read prop time: getExiftool may trigger UPath.initPath() which
		// changes the prop time, so sync the stamp to avoid re-probing on the
		// very next call.
		EXIFTOOL_AVAILABLE_PROP_TIME = UPath.getPropTime();
		EXIFTOOL_AVAILABLE = Boolean.valueOf(available);
		return available;
	}

	/**
	 * exiftool 可用性缓存
	 */
	private static Boolean EXIFTOOL_AVAILABLE = null;
	/**
	 * 可用性缓存对应的 ewa_conf.xml 修改时间
	 */
	private static long EXIFTOOL_AVAILABLE_PROP_TIME = 0;

	/**
	 * Get the exiftool executable path.
	 * <p>
	 * Resolution order:
	 * <ol>
	 * <li>Configured path in ewa_conf.xml ({@code <exiftool path="..." />})</li>
	 * <li>{@code which} / {@code where} command lookup</li>
	 * <li>Auto-detect in common installation paths for the current OS</li>
	 * <li>System PATH (returns "exiftool")</li>
	 * </ol>
	 *
	 * @return the exiftool executable path
	 * @throws Exception if exiftool is configured but the executable is not found
	 */
	public static String getExiftool() throws Exception {
		long currentPropTime = UPath.getPropTime();
		if (currentPropTime != EXIFTOOL_CACHE_PROP_TIME) {
			EXIFTOOL_PATH = null;
			EXIFTOOL_CACHE_PROP_TIME = currentPropTime;
		}
		if (EXIFTOOL_PATH != null) {
			return EXIFTOOL_PATH;
		}

		String result = resolveExiftool();

		// Re-read prop time: resolveExiftool may trigger UPath.initPath() which
		// changes the prop time, so sync the stamp to avoid clearing the cache
		// on the very next call.
		EXIFTOOL_CACHE_PROP_TIME = UPath.getPropTime();
		EXIFTOOL_PATH = result;
		return result;
	}

	/**
	 * exiftool 可执行文件路径缓存
	 */
	private static String EXIFTOOL_PATH = null;
	/**
	 * 缓存对应的 ewa_conf.xml 修改时间
	 */
	private static long EXIFTOOL_CACHE_PROP_TIME = 0;

	/**
	 * Resolve the exiftool executable path (no caching).
	 *
	 * @return the full executable path
	 * @throws Exception if exiftool is configured but the executable is not found
	 */
	private static String resolveExiftool() throws Exception {
		String os = System.getProperty("os.name").toLowerCase();
		boolean isWindows = os.startsWith("windows");
		String exeName = isWindows ? "exiftool.exe" : "exiftool";

		// 1. Check ConfExiftool configuration
		ConfExiftool conf = ConfExiftool.getInstance();
		if (conf != null && conf.getPath() != null && conf.getPath().trim().length() > 0) {
			String confPath = conf.getPath().trim();
			File pathFile = new File(confPath);
			if (pathFile.isDirectory()) {
				File exe = new File(pathFile, exeName);
				if (!exe.exists()) {
					String err = "exiftool not found at [" + exe.getAbsolutePath() + "]";
					LOGGER.error(err);
					throw new Exception(err);
				}
				return exe.getAbsolutePath();
			} else if (pathFile.isFile()) {
				return pathFile.getAbsolutePath();
			} else {
				String err = "exiftool path not found: [" + confPath + "]";
				LOGGER.error(err);
				throw new Exception(err);
			}
		}

		// 2. Lookup via which (Unix) / where (Windows)
		String found = findExiftoolByWhich(exeName, isWindows);
		if (found != null) {
			LOGGER.info("exiftool found via which/where at: {}", found);
			return found;
		}

		// 3. Auto-detect in common installation paths
		List<String> dirs = new ArrayList<>();
		if (isWindows) {
			dirs.add("C:\\exiftool");
			dirs.add("C:\\Program Files\\exiftool");
			dirs.add("C:\\Program Files (x86)\\exiftool");
			dirs.add("C:\\ProgramData\\chocolatey\\bin");
			String userProfile = System.getenv("USERPROFILE");
			if (userProfile != null && userProfile.trim().length() > 0) {
				dirs.add(userProfile + "\\scoop\\shims");
			}
		} else if (os.contains("mac")) {
			dirs.add("/opt/homebrew/bin");
			dirs.add("/usr/local/bin");
			dirs.add("/opt/local/bin");
			dirs.add("/usr/bin");
		} else {
			dirs.add("/usr/bin");
			dirs.add("/usr/local/bin");
			dirs.add("/snap/bin");
		}
		for (String dir : dirs) {
			File exe = new File(dir, exeName);
			if (exe.exists() && exe.canExecute()) {
				LOGGER.info("exiftool auto-detected at: {}", exe.getAbsolutePath());
				return exe.getAbsolutePath();
			}
		}

		// 4. Fall back to system PATH
		LOGGER.info("exiftool not found, relying on system PATH");
		return "exiftool";
	}

	/**
	 * Use the system {@code which} (Unix) or {@code where} (Windows) command to
	 * locate exiftool in the system PATH.
	 *
	 * @param exeName   the platform-specific executable name
	 * @param isWindows whether the current OS is Windows
	 * @return the full path if found, or null
	 */
	private static String findExiftoolByWhich(String exeName, boolean isWindows) {
		String finder = isWindows ? "where" : "which";
		Process process = null;
		try {
			ProcessBuilder pb = new ProcessBuilder(finder, exeName);
			pb.redirectErrorStream(true);
			process = pb.start();
			String firstLine = null;
			try (java.io.BufferedReader reader = new java.io.BufferedReader(
					new java.io.InputStreamReader(process.getInputStream(), "UTF-8"))) {
				firstLine = reader.readLine();
			}
			int exitCode = process.waitFor();
			if (exitCode != 0 || firstLine == null || firstLine.trim().length() == 0) {
				return null;
			}
			firstLine = firstLine.trim();
			if (firstLine.startsWith("\"") && firstLine.endsWith("\"") && firstLine.length() > 1) {
				firstLine = firstLine.substring(1, firstLine.length() - 1);
			}
			File f = new File(firstLine);
			if (f.exists() && f.canExecute()) {
				return f.getAbsolutePath();
			}
		} catch (Exception e) {
			LOGGER.debug("{} {} failed: {}", finder, exeName, e.getMessage());
		} finally {
			if (process != null) {
				process.destroy();
			}
		}
		return null;
	}

	/**
	 * 执行 exiftool（参数数组方式，不经 shell，值中的特殊字符无需转义）
	 *
	 * @param args 命令及参数
	 * @return 标准输出内容
	 * @throws Exception 执行失败或超时
	 */
	private static String runExiftool(List<String> args) throws Exception {
		LOGGER.info("runExiftool: {}", String.join(" ", args));
		Process process = null;
		try {
			ProcessBuilder pb = new ProcessBuilder(args);
			pb.redirectErrorStream(true);
			process = pb.start();
			byte[] output = process.getInputStream().readAllBytes();
			boolean finished = process.waitFor(60, java.util.concurrent.TimeUnit.SECONDS);
			if (!finished) {
				process.destroyForcibly();
				throw new Exception("exiftool timed out after 60 seconds");
			}
			String out = new String(output, "UTF-8");
			if (process.exitValue() != 0) {
				String err = "exiftool failed exit=" + process.exitValue() + ": " + out.trim();
				LOGGER.error(err);
				throw new Exception(err);
			}
			return out;
		} finally {
			if (process != null) {
				process.destroy();
			}
		}
	}
}
