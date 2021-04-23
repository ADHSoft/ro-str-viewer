package com.skardach.ro.resource.str;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import com.skardach.ro.common.LittleEndianInputStreamAdapter;
import com.skardach.ro.graphics.BlendType;
import com.skardach.ro.graphics.Color;
import com.skardach.ro.graphics.Point2D;
import com.skardach.ro.graphics.Rectangle;
import com.skardach.ro.resource.ResourceException;
import com.skardach.ro.resource.ResourceManager;
import com.skardach.ro.resource.Texture;
import com.skardach.ro.resource.TextureManager;
/**
 * Class for reading STR files from stream.
 * @author Stanislaw Kardach
 *
 */
public class StrReader {
	/**
	 * First 4 bytes of the STR file to be checked when reading it.
	 */
	private static final byte MAGIC[] = {'S', 'T', 'R', 'M'};
	private static final int SUPPORTED_VERSION = 148;
	private static final int DEFAULT_FPS = 60;
	private static final int TEXTURE_NAME_SIZE = 128;
	/*
	 * Maximum count of things. Unlike in roint implementation,
	 * I do now use malloc to reserve space so I'm not bound by
	 * size_t.
	 */
	private static final int MAX_LAYER_COUNT = Integer.MAX_VALUE;
	/**
	 * Thrown if parsing fails for some reason
	 * @author Stanislaw Kardach
	 *
	 */
	public class ParseException extends Exception {
		private static final long serialVersionUID = 7050197797878645891L;
		/**
		 * Default constructor.
		 * @param string Message
		 */
		public ParseException(String string) {
			super(string);
		}

	}
	/**
	 * Read STR file from stream using given resource manager for fetching
	 * textures.
	 * @param iResourceManager
	 * @param iStream
	 * @return Object representing STR file.
	 * @throws ParseException In case of syntax errors in the stream.
	 * @throws ResourceException Thrown when texture manager is unable to
	 * locate texture specified in the STR file or passed ResourceManager is
	 * null.
	 */
	public Str readFromStream(ResourceManager iResourceManager, InputStream iStream) throws ParseException, ResourceException {
		if(iResourceManager == null)
			throw new ResourceException("No resource manager available");
		TextureManager textureManager = iResourceManager.getTextureManager();
		if(textureManager == null)
			throw new ResourceException("No texture manager available");
		Str result = new Str();
		try {
			// Prepare a data stream to read more easily
			LittleEndianInputStreamAdapter stream = new LittleEndianInputStreamAdapter(iStream);
			// read magic and bail of wrong
			byte magic[] = new byte[4];
			int read = stream.read(magic);
			if(read <= 0)
				return null;
			if(!Arrays.equals(magic, MAGIC))
				throw new ParseException(String.format("Invalid magic: [%s], expected: [%s]", Arrays.toString(magic), Arrays.toString(MAGIC)));
			// XXX: reading of int should work because according to api docs
			// readInt() should read 4 bytes and interpret them as an int.
			result.set_version(stream.readInt());
			if(result.get_version() != SUPPORTED_VERSION)
				throw new ParseException(String.format("Unsupported version: 0x%X", result.get_version()));
			result.set_fps(stream.readInt());
			if(result.get_fps() <= 0)
				result.set_fps(DEFAULT_FPS);
			result.set_frameCount(stream.readInt());
			if(result.get_frameCount() < 1) result.set_frameCount(1);
				//throw new ParseException("Framecount less than 0");
			// read layers
			int layerCount = stream.readInt();
			if(layerCount < 0)
				throw new ParseException("Layer count < 0");
			if(layerCount > MAX_LAYER_COUNT)
				throw new ParseException("Too many layers: " + layerCount);
			read = stream.read(result.get_reserved());
			if(read != Str.RESERVED_FIELD_SIZE)
				throw new ParseException(String.format("Invalid reserved field. Size(%d) [%s]", read,  Arrays.toString(result.get_reserved())));
			for(int l = 0; l < layerCount; l++)
				result.get_layers().add(readLayer(textureManager, stream));
		} catch (IOException e) {
			throw new ParseException("Problem reading stream: " + e.getMessage());
		}
		return result;
	}
	/**
	 * Read a single layer from the stream.
	 * @param textureManager
	 * @param stream Input stream containing the layer. It is assumed that the
	 * stream is set on the beginning of the layer data.
	 * @return Object representing a layer of effect file.
	 * @throws IOException Read error on the stream
	 * @throws ParseException Syntax error.
	 * @throws ResourceException Problems with loading textures.
	 */
	private Layer readLayer(TextureManager textureManager,
			LittleEndianInputStreamAdapter stream) throws IOException, ParseException,
			ResourceException {
		Layer layer = new Layer();
		int textureCount = stream.readInt();
		if(textureCount > Layer.MAX_TEXTURE_COUNT)
			throw new ParseException("Too many textures per layer: " + textureCount);
		for(int t = 0; t < textureCount; t++)
		{
			layer.get_textures().add(readTexture(textureManager, stream));
		}
		if (textureCount == 0) {
			layer.get_textures().add(genericTexture(textureManager));
		}
		
		int keyFrameCount = stream.readInt();
		if(keyFrameCount > Layer.MAX_KEYFRAME_COUNT)
			throw new ParseException("Too many key frames: " + keyFrameCount);
		for(int kf = 0; kf < keyFrameCount; kf++) {
			layer.get_keyFrames().add(readKeyFrame(stream));
		}
		return layer;
	}
	/**
	 * Read texture data and try to obtain it via texture manager.
	 * @param textureManager Texture manager to use
	 * @param stream Stream to read texture data from
	 * @return Texture object
	 * @throws IOException Stream read error.
	 * @throws ResourceException Texture could not be opened/located.
	 */
	private Texture readTexture(TextureManager textureManager,
			LittleEndianInputStreamAdapter stream) throws IOException, ResourceException {
		byte textureNameBuffer[] = new byte[TEXTURE_NAME_SIZE];
		stream.read(textureNameBuffer);
		String textureName = // this should use UTF8
			new String(textureNameBuffer, 0, TEXTURE_NAME_SIZE).trim();
		
		Texture texture =
			textureManager.getTexture(textureName);
		if(texture == null) // if no such texture found
			throw new ResourceException(
				String.format(
					"Texture [%s] could not be located",
					textureName));
		return texture;
	}
	
	private Texture genericTexture(TextureManager textureManager) throws IOException, ResourceException {
		
		String textureName = // this should use UTF8
			new String(System.getProperty("user.home") + "\\Pictures\\placeholderstr.png").trim();
		
		Texture texture =
			textureManager.getTexture(textureName);
		if(texture == null) // if no such texture found
			throw new ResourceException(
				String.format(
					"Texture [%s] could not be located",
					textureName));
		return texture;
	}
	/**
	 * Read key frame description from stream.
	 * @param stream Input stream to read data from/
	 * @return Object describing a key frame
	 * @throws IOException Read errors on the stream.
	 */
	private KeyFrame readKeyFrame(LittleEndianInputStreamAdapter stream) throws IOException {
		KeyFrame keyFrame = new KeyFrame();
		keyFrame.set_framenum(stream.readInt());
		keyFrame.set_frameType(KeyFrameType.fromInt(stream.readInt()));
		float x = stream.readFloat();
		float y = stream.readFloat();
		keyFrame.set_position(new Point2D(x, y));
		float u = stream.readFloat();
		float v = -stream.readFloat();  //imgs were flipped vert. int271
		float us = stream.readFloat();	//"u size"
		float vs = -stream.readFloat();
		keyFrame.set_textureUVMapping(
			new Rectangle<Point2D>(
				new Point2D(u, v),
				new Point2D((u+us),v),
				new Point2D(u,(v+vs)),
				new Point2D((u+us),(v+vs))));

		u = stream.readFloat();
		v = -stream.readFloat();
		us = stream.readFloat();
		vs = -stream.readFloat();
		keyFrame.set_textureUVMapping2(
			new Rectangle<Point2D>(
				new Point2D(u, v),
				new Point2D(us,v),
				new Point2D(u,vs),
				new Point2D(us,vs)));
		// Rectangle corners' coordinates
		// x coordinates first
		float ax = stream.readFloat();
		float bx = stream.readFloat();
		float cx = stream.readFloat();
		float dx = stream.readFloat();
		// now y coordinates
		float ay = stream.readFloat();
		float by = stream.readFloat();
		float cy = stream.readFloat();
		float dy = stream.readFloat();
		keyFrame.set_drawingRectangle(
			new Rectangle<Point2D>(
				new Point2D(ax, ay),
				new Point2D(bx, by),
				new Point2D(cx, cy),
				new Point2D(dx, dy)));
		keyFrame.set_textureId(stream.readFloat());
		keyFrame.set_animationType(AnimationType.fromInt(stream.readInt()));
		keyFrame.set_animationDelta(stream.readFloat());
		keyFrame.set_rotation(stream.readFloat());	
		// Read color
		float r = stream.readFloat();
		float g = stream.readFloat();
		float b = stream.readFloat();
		float alpha = stream.readFloat();
		keyFrame.set_color(new Color(r, g, b, alpha));
		keyFrame.set_sourceBlend(BlendType.fromInt(stream.readInt()));
		keyFrame.set_destAlpha(BlendType.fromInt(stream.readInt()));
		keyFrame.set_multiTexturePreset(MultiTextureMode.fromInt(stream.readInt()));
		return keyFrame;
	}
}
