package main;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import javax.swing.JFrame;

import org.jcodec.codecs.h264.H264Decoder;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

import com.github.serezhka.jap2lib.rtsp.AudioStreamInfo;
import com.github.serezhka.jap2lib.rtsp.VideoStreamInfo;
import com.github.serezhka.jap2server.AirPlayServer;
import com.github.serezhka.jap2server.AirplayDataConsumer;

public class Main {

	public static void main(String[] args) throws Exception {
		FileChannel videoFileChannel = FileChannel.open(Paths.get("video.h264"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
		FileChannel audioFileChannel = FileChannel.open(Paths.get("audio.pcm"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
		
		final JFrame frame = new JFrame();
		frame.setSize(500, 1080);
		frame.setDefaultCloseOperation(3);
		frame.setVisible(true);
		final Renderer cPanel = new Renderer();
		cPanel.setSize(frame.getSize());
		frame.add(cPanel);
		
		AirplayDataConsumer dumper = new AirplayDataConsumer() {
			private H264Decoder decoder;
			
			public AirplayDataConsumer init() {
				decoder = new H264Decoder();
				byte[] sps = new byte[] {0x01, 0x64, (byte) 0xc0, 0x28, (byte) 0xff, (byte) 0xe1, 0x00, 0x10, 0x67, 0x64, (byte) 0xc0, 0x28, (byte) 0xac, 0x56, 0x20, 0x0d};
				decoder.addSps(List.of(ByteBuffer.wrap(sps)));
				
				return this;
			}
		    
		    @Override
		    public void onVideo(byte[] video) {
		    	System.out.println(video.length);
		        try {
					videoFileChannel.write(ByteBuffer.wrap(video));
					
					ByteBuffer bb = ByteBuffer.wrap(video);
					Picture out = Picture.create(1920, 1088, ColorSpace.YUV420);
					var real = decoder.decodeFrame(bb, out.getData());
					
					var img = AWTUtil.toBufferedImage(real);
					
					cPanel.render(img);
					cPanel.repaint();
				} catch(RuntimeException e) {
					System.out.print(".");
				} catch (IOException e) {
					e.printStackTrace();
				}
		    }
		    
		    @Override
		    public void onAudio(byte[] audio) {
		    	System.out.println("audio");
	            byte[] audioDecoded = new byte[480 * 4];
	            try {
					audioFileChannel.write(ByteBuffer.wrap(audioDecoded));
				} catch (IOException e) {
					e.printStackTrace();
				}
		    }


			@Override
			public void onVideoFormat(VideoStreamInfo videoStreamInfo) {
				System.out.println(videoStreamInfo.getStreamConnectionID());
				System.out.println(videoStreamInfo.getStreamType());
			}


			@Override
			public void onAudioFormat(AudioStreamInfo audioInfo) {
				System.out.println(audioInfo);
			}
		}.init();

		String serverName = "E";
		int airPlayPort = 15614;
		int airTunesPort = 5001;
		new AirPlayServer(serverName, airPlayPort, airTunesPort, dumper).start();
	}

}
