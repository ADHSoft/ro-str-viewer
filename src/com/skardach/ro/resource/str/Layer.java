package com.skardach.ro.resource.str;

import java.util.LinkedList;
import java.util.List;

import com.skardach.ro.resource.Texture;

/**
 * Animation layer
 * @author Stanislaw Kardach
 *
 */
public class Layer {
	/*
	 * open-ragnarok implementation was using malloc to allocate
	 * space for array of textures and key frames. Here we don't
	 * care about memory allocation so the only limit is the
	 * max value of 32 bit unsigned integer.
	 */
	/**
	 * Maximum number of textures in a layer.
	 */
	public static final int MAX_TEXTURE_COUNT = Integer.MAX_VALUE;
	/**
	 * Maximum number of key frames in a layer.
	 */
	public static final int MAX_KEYFRAME_COUNT = Integer.MAX_VALUE;
	List<Texture> _textures = new LinkedList<Texture>();
	LinkedList<KeyFrame> _keyFrames = new LinkedList<KeyFrame>();
	/**
	 * @return List of textures used in this layer.
	 */
	public List<Texture> get_textures() {
		return _textures;
	}
	/**
	 * @return List of key frames in this layer.
	 */
	public List<KeyFrame> get_keyFrames() {
		return _keyFrames;
	}

	@Override
	public String toString() {
		return toString("");
	}
	/**
	 * String representation prepended by prefix
	 * @param iPrefix prefix to prepend
	 * @return String representation
	 */
	public String toString(String iPrefix) {
		String result=iPrefix + "<Empty Layer/>";
		if (_keyFrames.size()!=0) {
			result = iPrefix + "<layer>\n"
				+ iPrefix + "  <_textures>\n";
			for(Texture t : _textures)
				result += t.toString(iPrefix + "    ") + "\n";
	
			result += iPrefix + "  </_textures>\n"
				+ iPrefix + "  <_Frames>\n";
			for(KeyFrame kf : _keyFrames)
				result += kf.toString(iPrefix + "    ") + "\n";
	
			result += iPrefix + "  </_Frames>\n";
			result += iPrefix + "</layer>";
		}
		return result;
	}
}
