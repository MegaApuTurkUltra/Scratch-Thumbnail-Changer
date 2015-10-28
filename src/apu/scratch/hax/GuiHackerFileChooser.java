/**
 * 
 */
package apu.scratch.hax;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import apu.scratch.hax.GuiHackerFileChooser.GuiHackerFileFilter.FileFilterType;

/**
 * File chooser
 * 
 * @author "MegaApuTurkUltra"
 */
public class GuiHackerFileChooser extends JPanel {
	private static final long serialVersionUID = 8323299434700855672L;
	JFileChooser fileChooser;

	static class GuiHackerFileFilter extends FileFilter {
		static enum FileFilterType {
			GIF(new String[] { "gif" }, "image/gif", "GIF Images"), PNG(
					new String[] { "png" }, "image/png", "PNG Images"), JPEG(
					new String[] { "jpeg", "jpg" }, "image/jpeg", "JPEG Images"), OTHER(
					null, "application/octet-stream", "Other files");
			String mime;
			String[] ext;
			String desc;

			private FileFilterType(String[] e, String m, String d) {
				ext = e;
				mime = m;
				desc = d;
			}

			public String getDesc() {
				return desc;
			}

			public String getMimeType() {
				return mime;
			}

			public boolean isFileAccepted(File f) {
				if (ext == null)
					return !f.isDirectory() && f.exists();
				for (String s : ext) {
					if (f.getName().endsWith(s) && f.exists())
						return true;
				}
				return false;
			}
		}

		FileFilterType type;

		public GuiHackerFileFilter(FileFilterType t) {
			type = t;
		}

		@Override
		public boolean accept(File f) {
			return type.isFileAccepted(f) || f.isDirectory();
		}

		@Override
		public String getDescription() {
			return type.getDesc();
		}
	}

	/**
	 * Create the panel.
	 */
	public GuiHackerFileChooser() {
		fileChooser = new JFileChooser();
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setFileHidingEnabled(false);
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		for (FileFilterType type : FileFilterType.values()) {
			fileChooser.addChoosableFileFilter(new GuiHackerFileFilter(type));
		}
		fileChooser.setApproveButtonText("Hack it!");
		setLayout(new BorderLayout(0, 0));
		add(fileChooser);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					removeCancelButton(fileChooser);
				} catch (Exception e) {
					e.printStackTrace();
					// continue and ignore. The cancel button doesn't do
					// anything anyway
				}
			}
		});

		fileChooser.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						FileFilterType type = ((GuiHackerFileFilter) fileChooser
								.getFileFilter()).type;
						File f = fileChooser.getSelectedFile();
						if (f == null || f.isDirectory() || !f.exists())
							return;
						if (type == FileFilterType.GIF) {
							if (!checkGifFrameRate(f))
								return;
							
//							try {
//								f = watermark(f);
//							} catch (Exception e) {
//								e.printStackTrace();
//								ThumbnailGuiHacker.INSTANCE.showExceptionDialog(e);
//							}
						}

						ThumbnailGuiHacker.INSTANCE.executeHack(f, type.mime);
					}
				});
			}
		});
	}

	public static void removeCancelButton(Container c) {
		for (int i = 0; i < c.getComponentCount(); i++) {
			Component comp = c.getComponent(i);

			if (comp instanceof JButton) {
				JButton b = (JButton) comp;

				if (b != null && b.getText() != null
						&& b.getText().equals("Cancel")) {
					c.remove(b);
				}

			} else if (comp instanceof Container) {
				removeCancelButton((Container) comp);
			}
		}
	}

	public boolean checkGifFrameRate(File f) {
		try {
			FileImageInputStream in = new FileImageInputStream(f);
			List<Integer> delays = getGifDelays(in);
			in.close();
			boolean shortDelay = false;
			for (Integer x : delays) {
				if (x.intValue() < 50) {
					shortDelay = true;
					break;
				}
			}
			if (shortDelay) {
				return ThumbnailGuiHacker.INSTANCE.warnAnnoyingGif();
			}
		} catch (Exception e) {
			e.printStackTrace();
			ThumbnailGuiHacker.INSTANCE.showExceptionDialog(e);
		}
		return true;
	}
	
	public static void main(String[] args) throws Exception {
		File test = new File("sample path");
		File wm = watermark(test);
		Files.copy(Paths.get(wm.toURI()), new FileOutputStream("test.gif"));
	}

	/*
	 * java, y u make dis so complicated?
	 */
	public static File watermark(File gif) throws Exception {
		ImageInputStream iin = new FileImageInputStream(gif);
		File tmp = File.createTempFile("thumb_", ".gif");
		tmp.deleteOnExit();
		ImageOutputStream iout = new FileImageOutputStream(tmp);

		InputStream wmarkin = ThumbnailGuiHacker.class
				.getResourceAsStream("/apu/scratch/hax/watermark.png");
		BufferedImage wmark = ImageIO.read(wmarkin);
		wmarkin.close();

		Iterator<ImageReader> readers = ImageIO.getImageReaders(iin);

		ImageReader reader = null;
		while (readers.hasNext()) {
			reader = readers.next();

			String metaFormat = reader.getOriginatingProvider()
					.getNativeImageMetadataFormatName();
			if ("gif".equalsIgnoreCase(reader.getFormatName())
					&& !"javax_imageio_gif_image_1.0".equals(metaFormat)) {
				continue;
			} else {
				break;
			}
		}
		if (reader == null) {
			iout.close();
			throw new IOException("Can not read image format!");
		}

		ImageWriter writer = ImageIO.getImageWriter(reader);
		writer.setOutput(iout);

		boolean isGif = reader.getFormatName().equalsIgnoreCase("gif");
		reader.setInput(iin, false, !isGif);
		writer.prepareWriteSequence(reader.getStreamMetadata());
		for (int index = 0;; index++) {
			try {
				// read a frame and its metadata
				IIOImage frame = reader.readAll(index, null);
				RenderedImage img = frame.getRenderedImage();
				if (img == null)
					throw new IllegalStateException("img == null");
				BufferedImage newImg = convertRenderedImage(img);
				newImg.getGraphics().drawImage(wmark, 0, 0, newImg.getWidth(),
						newImg.getHeight(), 0, 0, wmark.getWidth(),
						wmark.getHeight(), null);
				frame.setRenderedImage(newImg);
				writer.writeToSequence(frame, null);
			} catch (IndexOutOfBoundsException e) {
				break;
			}
		}

		// clean up
		reader.dispose();
		iout.close();
		writer.dispose();
		Files.copy(Paths.get(tmp.toURI()), new FileOutputStream("test.gif"));
		return tmp;
	}

	public static BufferedImage convertRenderedImage(RenderedImage img) {
		if (img instanceof BufferedImage) {
			return (BufferedImage) img;
		}
		ColorModel cm = img.getColorModel();
		int width = img.getWidth();
		int height = img.getHeight();
		WritableRaster raster = cm
				.createCompatibleWritableRaster(width, height);
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		Hashtable<String, Object> properties = new Hashtable<>();
		String[] keys = img.getPropertyNames();
		if (keys != null) {
			for (int i = 0; i < keys.length; i++) {
				properties.put(keys[i], img.getProperty(keys[i]));
			}
		}
		BufferedImage result = new BufferedImage(cm, raster,
				isAlphaPremultiplied, properties);
		img.copyData(raster);
		return result;
	}

	private List<Integer> getGifDelays(ImageInputStream imageStream)
			throws IOException {

		// obtain an appropriate src reader
		Iterator<ImageReader> readers = ImageIO.getImageReaders(imageStream);

		ImageReader reader = null;
		while (readers.hasNext()) {
			reader = readers.next();

			String metaFormat = reader.getOriginatingProvider()
					.getNativeImageMetadataFormatName();
			if ("gif".equalsIgnoreCase(reader.getFormatName())
					&& !"javax_imageio_gif_image_1.0".equals(metaFormat)) {
				continue;
			} else {
				break;
			}
		}
		if (reader == null) {
			throw new IOException("Can not read image format!");
		}
		boolean isGif = reader.getFormatName().equalsIgnoreCase("gif");
		reader.setInput(imageStream, false, !isGif);
		List<Integer> delays = new ArrayList<Integer>();
		boolean unkownMetaFormat = false;
		for (int index = 0;; index++) {
			try {
				// read a frame and its metadata
				IIOImage frame = reader.readAll(index, null);

				if (unkownMetaFormat)
					continue;

				// obtain src metadata
				javax.imageio.metadata.IIOMetadata meta = frame.getMetadata();

				IIOMetadataNode imgRootNode = null;
				try {
					imgRootNode = (IIOMetadataNode) meta
							.getAsTree("javax_imageio_gif_image_1.0");
				} catch (IllegalArgumentException e) {
					// unkown metadata format, can't do anyting about this
					unkownMetaFormat = true;
					continue;
				}

				IIOMetadataNode gce = (IIOMetadataNode) imgRootNode
						.getElementsByTagName("GraphicControlExtension")
						.item(0);

				delays.add(Integer.parseInt(gce.getAttribute("delayTime")));
			} catch (IndexOutOfBoundsException e) {
				break;
			}
		}

		// clean up
		reader.dispose();
		return delays;
	}
}
