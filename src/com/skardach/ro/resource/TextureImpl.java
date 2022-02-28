package com.skardach.ro.resource;


import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
/**
 * Class describing a texture. Uses JOGL TextureIO toolkit to make things
 * easier.
 * @author Stanislaw Kardach
 *
 */
public class TextureImpl implements Texture {
	String _name;
	String _path;
	TextureData _data;
	com.jogamp.opengl.util.texture.Texture _joglTexture;
	Integer _GLName = null;
	private boolean _convertMagenta=false;
	/**
	 * Construct the texture with given relative path (name) and base path
	 * @param iName Name of the texture, being the relative path to the
	 * texture file.
	 * @param iBasePath The base path to the texture. Along with texture name
	 * it makes the full path to the texture file.
	 * @param iConvertMagenta If true then upon loading the colors with
	 * magenta part larger than 0.7 will be converted to alpha if the texture
	 * color model is RGB or BGR (without alpha). Currently only byte sized
	 * color elements are supported. The conversion process will only occur if
	 * the first pixel of the texture is full magenta ([1,0,1] in RGB).
	 */
	public TextureImpl(
			String iName,
			String iBasePath,
			boolean iConvertMagenta) {// changed
		//iTextureName=iTextureName.replaceAll("\0", "").replaceAll("ÿ", "").replaceAll(".bmp", "");
		_name = iName.replaceAll("\0", "").replaceAll("ÿ", "");
		_path = iBasePath;
		_convertMagenta = iConvertMagenta;
	}

	@Override
	public String toString() {
		return toString("");
	}

	public String toString(String iPrefix) {
		return
			iPrefix + "<texture"
			+ " _name=\"" + _name + "\""
			+ " _path=\"" + _path + "\""
			+ " _GLName=\"" + _GLName + "\""
			+ " />";
	}

	@Override
	public String getName() {
		return _path;
	}

	@Override
	public synchronized Buffer getData() throws ResourceException {
		return isLoaded() ? _data.getBuffer() : null;
	}

	@Override
	public synchronized int getWidth() {
		return _data != null ? _data.getWidth() : -1;
	}

	@Override
	public synchronized int getHeight() {
		return _data != null ? _data.getHeight() : -1;
	}

	@Override
	public synchronized void load(GL2 iGLContext) throws ResourceException {
		if(isLoaded())
			return;
		int idx = _name.lastIndexOf('.');
		if(idx < 0)
			throw new ResourceException(
				"Unsupported texture extension: " + _name);
		String extension = _name.substring(idx);
		try {
			// Read the image
			File textureFile = new File(_path, _name);
			if(!textureFile.exists()) { 
				String path = System.getProperty("user.home") + "\\Pictures\\placeholderstr.png";
				textureFile = new File(path);
			}
			_data = TextureIO.newTextureData(
				iGLContext.getGLProfile(),
				textureFile,
				//GL.GL_,GL.GL_UNSIGNED_INT,
				false,
				extension);
			if(_data == null)
				throw new ResourceException(
					"Could not read texture " + _name);
			if(_convertMagenta)
				;//convertMagentaToAlphaIfNeeded(); changed
			// create the texture
			_joglTexture = TextureIO.newTexture(_data);
			// set some parameters
			_joglTexture.setTexParameterf(
				iGLContext,
				GL.GL_TEXTURE_WRAP_S,
				GL.GL_REPEAT);
			_joglTexture.setTexParameterf(
				iGLContext,
				GL.GL_TEXTURE_WRAP_T,
				GL.GL_REPEAT);
			_joglTexture.setTexParameterf(
				iGLContext,
				GL.GL_TEXTURE_MAG_FILTER,
				GL.GL_LINEAR);
			//_joglTexture.setTexParameterf(iGLContext, GL.GL_BLEND,1.f);
			iGLContext.glTexEnvf(
				GL2.GL_TEXTURE_ENV,
				GL2.GL_TEXTURE_ENV_MODE,
				GL2.GL_MODULATE);

			iGLContext.glColor3f(0, 1, 1);
		} catch (IOException e) {
			throw new ResourceException(
				"Error loading texture from file: "
				+ _path
				+ File.pathSeparator
				+ _name
				+ ". Reason: "
				+ e);
		}
	}

	/**
	 * This method will convert all the colors close to magenta (with magenta
	 * component larger than 0.7) to alpha depending on their distance from
	 * magenta (alpha = 1.0-magenta).
	 * Current implementation supports only byte sized color components in
	 * both RGB and BGR (since the order makes no difference for magenta
	 * component extraction as it depends on green color).
	 * @throws ResourceException If there was a problem with reading the
	 * texture data or creating the converted data.
	 */
	private void convertMagentaToAlphaIfNeeded()
			throws ResourceException {
		int pixelType = _data.getPixelType();
		int pixelFormat = _data.getPixelFormat();
		if((pixelFormat == GL.GL_RGB // only support conversion from RGB
				|| pixelFormat == GL2.GL_BGR) // and BGR format now
			&& pixelType == GL.GL_UNSIGNED_BYTE
			&& _data.getBuffer() instanceof ByteBuffer) {
			ByteBuffer old = (ByteBuffer) _data.getBuffer();
			byte pixel[] = new byte[4];
			try {
				old.get(pixel, 0, 3);
				// normalize rgb parts
				float r = (0xFF & pixel[0])/255f;
				float g = (0xFF & pixel[1])/255f;
				float b = (0xFF & pixel[2])/255f;
				// get magenta color (pink)
				float k = 1f - Math.max(Math.max(r, g), b);
				float m = (1-g-k)/(1-k);
				if(pixel[0] == -1
					&& pixel[1] == 0
					&& pixel[2] == -1) { // pink on first pixel so convert
					ByteBuffer converted =
						ByteBuffer.allocate(
							_data.getWidth()
							* _data.getHeight() // number of pixels
							* 4); // and 4 components per pixel (RGBA/BGRA)
					pixel[3] = 0;
					converted.put(pixel);
					while(old.position() < old.capacity()) {
						old.get(pixel, 0, 3);
						// normalize rgb parts
						r = (0xFF & pixel[0])/255f;
						g = (0xFF & pixel[1])/255f;
						b = (0xFF & pixel[2])/255f;
						// get magenta color (pink)
						k = 1f - Math.max(Math.max(r, g), b);
						m = (1-g-k)/(1-k);
						if(m > 0.75) {
							pixel[3] = (byte) (-1*(1-m));
						} else { // max alpha otherwise
							pixel[3] = -1;
						}
						converted.put(pixel);
					}
					converted.rewind();
					_data = new TextureData(
						_data.getGLProfile(),
						4,
						_data.getWidth(),
						_data.getHeight(),
						_data.getBorder(),
						pixelFormat == GL.GL_RGB ? GL.GL_RGBA : GL2.GL_BGRA,
						_data.getPixelType(),
						_data.getMipmap(),
						_data.isDataCompressed(),
						_data.getMustFlipVertically(),
						converted, null);
				} else {
					old.rewind();
				}
			} catch(IllegalArgumentException e) {
				throw new ResourceException(
					"Could not add alpha channel to texture, error creating updated texture");
			}
			catch(BufferUnderflowException e) {
				throw new ResourceException(
					"Could not add alpha channel to texture, error reading texture buffer");
			}
		}
	}

	@Override
	public synchronized void unload(GL2 iGLContext) {
		if(!isLoaded())
			return;
		_joglTexture.destroy(iGLContext);
		_data.destroy();
		_joglTexture = null;
		_data = null;
	}

	@Override
	public synchronized boolean isLoaded() {
		return _joglTexture != null;
	}

	@Override
	public boolean bind(GL2 iGLContext) {
		if(!isLoaded())
			return false;
		_joglTexture.bind(iGLContext);
		return true;
	}
}
