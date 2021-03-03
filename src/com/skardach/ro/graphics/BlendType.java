package com.skardach.ro.graphics;

import com.jogamp.opengl.GL;
/**
 * Blend type for OpenGL blend function
 * @author Stanislaw Kardach
 *
 */
public enum BlendType {
	ZERO,
	ONE,
	SRC_COLOR,
	INV_SRC_COLOR,
	SRC_ALPHA,
	INV_SRC_ALPHA,
	DEST_ALPHA,
	INV_DEST_ALPHA,
	DEST_COLOR,
	INV_DEST_COLOR,
	SRC_ALPHA_SAT,
	BOTH_SRC_ALPHA,
	BOTH_INV_SRC_ALPHA;
	/**
	 * Create enum from int
	 * @param iType blend type as read from STR file.
	 * @return Enum value based on the int
	 */
	public static BlendType fromInt(int iType) {
		switch(iType) {
		default:
		case 0:
		case 1: return ZERO;
		case 2: return ONE;
		case 3: return SRC_COLOR;
		case 4: return INV_SRC_COLOR;
		case 5: return SRC_ALPHA;
		case 6: return INV_SRC_ALPHA;
		case 7: return DEST_ALPHA;
		case 8: return INV_DEST_ALPHA;
		case 9: return DEST_COLOR;
		case 10: return INV_DEST_COLOR;
		case 11: return SRC_ALPHA_SAT;
		case 12: return BOTH_SRC_ALPHA;
		case 13: return BOTH_INV_SRC_ALPHA;
		}
	}
	/**
	 * @return Convert blend type enum to OpenGL identifier
	 */
	public int toGLValue() {
		switch(this) {
		default:
		case ZERO: return GL.GL_ZERO;
		case ONE: return GL.GL_ONE;
		case SRC_COLOR: return GL.GL_SRC_COLOR;
		case INV_SRC_COLOR: return GL.GL_ONE_MINUS_SRC_COLOR;
		case SRC_ALPHA: return GL.GL_SRC_ALPHA;
		//case INV_SRC_ALPHA: return GL.GL_DST_COLOR;
		case INV_SRC_ALPHA: return GL.GL_ONE_MINUS_SRC_ALPHA;
		case DEST_ALPHA: return GL.GL_DST_ALPHA;
		case INV_DEST_ALPHA: return GL.GL_ONE_MINUS_DST_ALPHA;
		case DEST_COLOR: return GL.GL_DST_COLOR;
		case INV_DEST_COLOR: return GL.GL_ONE_MINUS_DST_COLOR;
		case SRC_ALPHA_SAT: return GL.GL_SRC_ALPHA_SATURATE;
		case BOTH_SRC_ALPHA: return GL.GL_SRC_ALPHA; // TODO: D3DBLEND_BOTHSRCALPHA
		case BOTH_INV_SRC_ALPHA: return GL.GL_ZERO; // TODO: D3DBLEND_BOTHINVSRCALPHA
		}
	}
}
