package com.skardach.ro.resource.str;

/**
 * Describes key frame type. Can be either basic frame or animation (morph)
 * frame.
 * @author Stanislaw Kardach
 *
 */
public enum KeyFrameType {
	/**
	 * Basic key frame, contains frame rectangle position, dimensions, color
	 * and texture data.
	 */
	BASIC,
	/**
	 * Animation frame, morphs the basic frames. Adds rotation, position
	 * translation, rectangle deformation/shift and color transformations.
	 */
	MORPH;
	/**
	 * Convert from integer.
	 * @param iType Integer to create KeyFrameType from.
	 * @return returns MORPH for input 1 and BASIC for everything else
	 */
	public static KeyFrameType fromInt(int iType)
	{
		if( ( iType % 2) == 1 ) //int271
			return MORPH;
		else 
			return BASIC;
	}
}
