package com.skardach.ro.resource.str;
import com.skardach.ro.graphics.BlendType;
import com.skardach.ro.graphics.Color;
import com.skardach.ro.graphics.Point2D;
import com.skardach.ro.graphics.Rectangle;

/**
 * Describes Key frame of an effect animation
 * @author Stanislaw Kardach
 */
public class KeyFrame {
	int _framenum;
	KeyFrameType _frameType; // subclass maybe? Take a look at roengine processing
	Point2D _position;
	/**
	 * Texture mapping.
	 * Vertices go like this:
	 * a--b
	 * |  |
	 * c--d
	 */
	Rectangle<Point2D> _textureUVMapping;
	/**
	 * Second texture mapping. Not sure yet what's it for...
	 * Vertices go like this:
	 * a--b
	 * |  |
	 * c--d
	 */
	Rectangle<Point2D> _textureUVMapping2;
	/**
	 * Drawing rectangle for OpenGL.
	 * Vertices go like this:
	 * d--c
	 * |  |
	 * a--b
	 */
	Rectangle<Point2D> _drawingRectangle;
	float _textureId;
	AnimationType _animationType;
	float _animationDelta; ///< Texture animation delta.
	float _rotation; //< Rotation [0,1024[ is equivalent to [0,360[ degrees
	Color _color;
	BlendType _sourceBlend; //< Source blend mode
	BlendType _destBlend; //< Destination blend mode
	MultiTextureMode _multiTexturePreset;
	/**
	 * @return Frame number.
	 */
	public final int get_framenum() {
		return _framenum;
	}
	/**
	 * @return Type of the frame.
	 */
	public final KeyFrameType get_frameType() {
		return _frameType;
	}
	/**
	 * @return position of the frame rectangle or final translation of that
	 * rectangle in case of animation frame.
	 */
	public final Point2D get_position() {
		return _position;
	}
	/**
	 * @return UV Mapping of the texture.
	 */
	public final Rectangle<Point2D> get_textureUVMapping() {
		return _textureUVMapping;
	}
	/**
	 * @return Unknown...
	 */
	public final Rectangle<Point2D> get_textureUVMapping2() {
		return _textureUVMapping2;
	}
	/**
	 * @return Rectangle for the texture.
	 */
	public final Rectangle<Point2D> get_drawingRectangle() {
		return _drawingRectangle;
	}
	/**
	 * @return Index of the texture in the layer that this key frame uses
	 */
	public final float get_textureId() {
		return _textureId;
	}
	/**
	 * @return Animation type. Relevant for animation frames
	 */
	public final AnimationType get_animationType() {
		return _animationType;
	}
	/**
	 * @return Animation delta by which animation is applied. Relevant only to
	 * animation frames.
	 */
	public final float get_animationDelta() {
		return _animationDelta;
	}
	/**
	 * @return Rotation of the frame rectangle.
	 */
	public final float get_rotation() {
		return _rotation;
	}
	/**
	 * @return Base color for the key frame rectangle.
	 */
	public final Color get_color() {
		return _color;
	}
	/**
	 * @return Source blend for the blending function
	 */
	public final BlendType get_sourceBlend() {
		return _sourceBlend;
	}
	/**
	 * @return Destination blend for the blending function.
	 */
	public final BlendType get_destBlend() {
		return _destBlend;
	}
	/**
	 * @return Multi-texture handling preset.
	 */
	public final MultiTextureMode get_multiTexturePreset() {
		return _multiTexturePreset;
	}
	/**
	 * Set the frame number
	 */
	public final void set_framenum(int _framenum) {
		this._framenum = _framenum;
	}
	/**
	 * Set frame type
	 */
	public final void set_frameType(KeyFrameType _frameType) {
		this._frameType = _frameType;
	}
	/**
	 * Set frame position
	 */
	public final void set_position(Point2D _position) {
		this._position = _position;
	}
	/**
	 * Set texture mapping
	 */
	public final void set_textureUVMapping(Rectangle<Point2D> iMapping) {
		assert(iMapping != null);
		this._textureUVMapping = iMapping;
	}
	/**
	 * Set secondary texture mapping. This is not taken into account.
	 */
	public final void set_textureUVMapping2(Rectangle<Point2D> iMapping) {
		assert(iMapping != null);
		this._textureUVMapping2 = iMapping;
	}
	/**
	 * Set drawing rectangle
	 */
	public final void set_drawingRectangle(Rectangle<Point2D> _rectangle) {
		this._drawingRectangle = _rectangle;
	}
	/**
	 * Set texture id
	 */
	public final void set_textureId(float _textureId) {
		this._textureId = _textureId;
	}
	/**
	 * Set animation type
	 */
	public final void set_animationType(AnimationType _animationType) {
		this._animationType = _animationType;
	}
	/**
	 * Set animation delta
	 */
	public final void set_animationDelta(float _animationDelta) {
		this._animationDelta = _animationDelta;
	}
	/**
	 * Set rotation
	 */
	public final void set_rotation(float _rz) {
		this._rotation = _rz;
	}
	/**
	 * Set primary color
	 */
	public final void set_color(Color _color) {
		this._color = _color;
	}
	/**
	 * Set source blend for blending function
	 */
	public final void set_sourceBlend(BlendType iSourceBlend) {
		this._sourceBlend = iSourceBlend;
	}
	/**
	 * Set destination blend for blending function
	 */
	public final void set_destAlpha(BlendType iDestAlpha) {
		this._destBlend = iDestAlpha;
	}
	/**
	 * Set multi texture handling preset
	 */
	public final void set_multiTexturePreset(MultiTextureMode iMultiTexturePreset) {
		this._multiTexturePreset = iMultiTexturePreset;
	}

	@Override
	public String toString() {
		return toString("");
	}
	/**
	 * Return string representation prepended with given prefix on each line.
	 * @param iPrefix Prefix to prepend
	 * @return String representation
	 */
	public String toString(String iPrefix) {
		return
			iPrefix + (_frameType== _frameType.BASIC ? "<keyFrame ":"<morphFr. ")  
			//+ " Type=\"" + _frameType + "\""
			+ " Fr#=\"" + _framenum + "\""			
			+ " tex#=\"" + _textureId + "\""
			+ (_animationType!= _animationType.NO_CHANGE ? 
					(" aniType=\"" + _animationType + "\" aniDelta=\"" + _animationDelta + "\"" ):"")
			+ " pos=\"" + _position + "\""
			+ " texUV=\"" + _textureUVMapping.get_a() + _textureUVMapping.get_d() + "\""
			+ " texUV2=\"" + _textureUVMapping2.get_a() + _textureUVMapping2.get_d() + "\""
			+ " Rectangle=\"" + _drawingRectangle.get_a() + _drawingRectangle.get_d() + "\""
			+ " rotation=\"" + _rotation + "\""
			+ " color=\"" + _color + "\""
			+ " sourceBlend=\"" + _sourceBlend + "\""
			+ " destBlend=\"" + _destBlend + "\""
			+ (_multiTexturePreset!= _multiTexturePreset.NO_CHANGE ? (" multiTexturePreset=" + _multiTexturePreset ):"")
			+ " />";
	}

}
