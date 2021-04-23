package com.skardach.ro.resource;

import java.util.HashMap;

/**
 * Simple implementation of texture manager. It only maintains a map of
 * textures indexed by their pathname. It uses {@link TextureImpl} class for
 * texture objects.
 * @author Stanislaw Kardach
 *
 */
public class SimpleTextureManager implements TextureManager {
	private String _textureBaseDir;
	private HashMap<String, Texture> _textures = new HashMap<String, Texture>();
	private boolean _convertMagenta;
	/**
	 * Creates a new instance of SimpleTextureManager. All textures are
	 * created relative to the base path given.
	 * @param iBasePath Base path to prepend to all textures retrieved from
	 * this texture manager.
	 * @param iTexturesConvertMagenta Should created textures convert the
	 * magenta color to alpha when alpha is missing.
	 */
	public SimpleTextureManager(String iBasePath, boolean iTexturesConvertMagenta) {
		//iBasePath += "\\" ;
		_textureBaseDir = iBasePath;	//int271 
		_convertMagenta = false;//iTexturesConvertMagenta;
	}

	@Override
	public Texture getTexture(String iTextureName) {
		//System.out.println(iTextureName);		//int271
		if (iTextureName.length()>0x2e) {
			iTextureName=iTextureName.substring(0, 0x30); //truncate junk
		}
		iTextureName=iTextureName.replaceAll("\0", "").replaceAll("ÿ", "");
		//System.out.println(iTextureName);
		
		Texture result = null;
		if(iTextureName != null)
		{
			result = _textures.get(iTextureName);
			if(result == null)
			{
				result = new TextureImpl(
					iTextureName,
					_textureBaseDir,
					_convertMagenta);
				_textures.put(iTextureName, result);
			}
		}
		return result;
	}
}
