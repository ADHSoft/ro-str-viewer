package com.skardach.ro.resource.str;

/**
 * Texture animation type.
 * Determines how aniframe morphs per frame.
 * @author Stanislaw Kardach
 *
 */
public enum AnimationType {
	/**
	 * Something unsupported
	 */
	UNKNOWN,
    /**
     * - no change
     */
	NO_CHANGE,
	/**
	 *  ]-inf,inf[ <pre>
	 *  aniframe += keyframe.aniframe; // add aniframe</pre>
	 */
	TYPE_1,
    /**
     * ]-inf,ROStrLayer.texturecount[ <pre>
     * aniframe += keyframe.anidelta; // add anidelta
     * if (aniframe >= ROStrLayer.texturecount) // stop on limit
     *   aniframe = ROStrLayer.texturecount - 1.0f;</pre>
     */
	TYPE_2,
    /**
     * [0.0,ROStrLayer.texturecount[ <pre>
     * aniframe += keyframe.anidelta; // add anidelta
     * if (aniframe >= ROStrLayer.texturecount) // loop when over
     *   aniframe -= (float)(int)(aniframe / ROStrLayer.texturecount) * ROStrLayer.texturecount;</pre>
     */
	TYPE_3,
	/**
	 * [0.0,ROStrLayer.texturecount[  WARNING broken on 2004 client <pre>
	 * aniframe -= keyframe.anidelta; // subtract anidelta
	 * if (aniframe < 0.0f) { // loop when under
	 *   aniframe -= (float)(int)(aniframe / ROStrLayer.texturecount) * ROStrLayer.texturecount;
	 *   if (aniframe < 0.0f)
	 *     aniframe += ROStrLayer.texturecount;
	 * }</pre>
	 */
	TYPE_4,
	/**
	 * [0,ROStrLayer.texturecount - 1]  randomize with anidelta seed??? (TODO interpret) <pre>
	 * int value = (int)((curframe - keyframe.framenum) * keyframe.anidelta + aniframe);
	 * int lasttex = ROStrLayer.texturecount - 1;
	 * int n = value / lasttex;
	 * if (n & 1)
	 *   aniframe = lasttex * (n + 1) - value;
	 * else
	 *   aniframe = value - lasttex * n;</pre>
	 */
	TYPE_5;
	
	static AnimationType fromInt(int iMode)
	{
		switch(iMode) {
		case 0: return NO_CHANGE;
		case 1: return TYPE_1;
		case 2: return TYPE_2;
		case 3: return TYPE_3;
		case 4: return TYPE_4;
		case 5: return TYPE_5;
		default:
			System.err.println("Unknown AnimationType: " + iMode);
			return UNKNOWN;
		}
	}


	@Override
	public String toString()
	{
		switch(this) {
		case NO_CHANGE: return "Static";
		case TYPE_1: return "Static";
		case TYPE_2: return "Play Once";
		case TYPE_3: return "Loop";
		case TYPE_4: return "Loop backwards";
		case TYPE_5: return "Loop pingPong";
		default: return "?(see Log)";
		}
	}
}