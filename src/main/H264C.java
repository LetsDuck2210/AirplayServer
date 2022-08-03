package main;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import org.jcodec.codecs.h264.H264Decoder;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

public class H264C {
	private H264Decoder decoder = new H264Decoder();
	private ByteBuffer bb = null;
	private Picture out = Picture.create(1920, 1080, ColorSpace.YUV420);
	
	public BufferedImage decode(byte[] data) {
	    bb = ByteBuffer.wrap(data);
	    Picture real = decoder.decodeFrame(bb, out.getData());
	    BufferedImage bi = AWTUtil.toBufferedImage(real);
	    return bi;
	}
}
