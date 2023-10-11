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
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.ImageIcon;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.conf.ConfImageMagick;

/**
 * The image utils
 *
 */
public class UImages {
	/**
	 * Default image quality
	 */
	public static int DEFAULT_QUALITY = 70;
	private static Logger LOGGER = LoggerFactory.getLogger(UImages.class);

	public final static String RESIZED_TAG = "$resized";

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
		int logo_height = newSize[0];

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
		if (conf == null) {
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
		return resizedPath + File.pathSeparatorChar + name;
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
		cmd.append("\"").append(imgPath).append("\" \"").append(newImgPath).append("\"");

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
			sb.append(img.getAbsolutePath());
			sb.append("\" \"");
			sb.append(f1);
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
		BufferedImage scaledBI = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = scaledBI.createGraphics();

		// 保持Png图片的透明背景属性
		BufferedImage bufIma = g.getDeviceConfiguration().createCompatibleImage(width, height,
				Transparency.TRANSLUCENT);
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
			bi = null;
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

		DefaultExecutor executor = new DefaultExecutor();
		int[] exitValues = { 0, 1 };

		executor.setExitValues(exitValues);

		ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
		executor.setWatchdog(watchdog);

		/*
		 * File dir = new File(UPath.getCVT_IMAGEMAGICK_HOME());
		 * executor.setWorkingDirectory(dir);
		 */
		String line1 = line;
		try {
			line1 = new String(line.getBytes(), "gbk");
		} catch (UnsupportedEncodingException e1) {
			LOGGER.error(e1.getMessage());
		}
		CommandLine commandLine = CommandLine.parse(line1);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
		executor.setStreamHandler(streamHandler);

		LOGGER.info(line);
		try {
			executor.execute(commandLine);
			String s = outputStream.toString();
			outputStream.close();
			rst.put("RST", "true");
			rst.put("MSG", s);
		} catch (ExecuteException e) {
			LOGGER.error(line);
			LOGGER.error(e.getMessage());
			rst.put("RST", "false");
			rst.put("ERR", e.getMessage());
		} catch (IOException e) {
			LOGGER.error(line);
			LOGGER.error(e.getMessage());
			rst.put("RST", "false");
			rst.put("ERR", e.getMessage());
		}
		return rst;
	}
}
