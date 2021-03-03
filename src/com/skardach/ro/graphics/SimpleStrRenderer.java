package com.skardach.ro.graphics;

import java.util.List;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.glu.GLU;

import com.skardach.ro.common.ObjectHolder;
import com.skardach.ro.resource.ResourceException;
import com.skardach.ro.resource.Texture;
import com.skardach.ro.resource.str.KeyFrame;
import com.skardach.ro.resource.str.KeyFrameType;
import com.skardach.ro.resource.str.Layer;
import com.skardach.ro.resource.str.Str;
import com.skardach.ro.resource.str.AnimationType;

/**
 * Simple implementation of rendering STR files. Most of the credit goes to
 * open-ragnarok project whose implementation of rendering was a guideline to
 * this one.
 * @author Stanislaw Kardach
 *
 */
public class SimpleStrRenderer implements Renderer {
	private static final float STR_ANGLE_TO_DEGREES =1f; //= 2.8444f;

	// OpenGL utilities
	GLU _glu = new GLU();
	// Object rendered
	Str _effect;
	// Rendering parameters
	boolean _preloadTextures;
	Point3D _renderPosition;
	float _xRotation;
	float _yRotation;
	float _zRotation;
	float _xScale;
	float _yScale;
	float _zScale;
	// rendering helper objects and variables
	protected FrameAdvanceCalculator _frameAdvanceCalculator = null;
	protected int _lastRenderedFrame = FrameAdvanceCalculator.NO_FRAME;
	/**
	 * Keeps track of which base frame should be a base for rendering for given
	 * layer. i.e. If current frame is 35 and on layer x there is a base frame
	 * of number 35, then _currentBaseFrameOnLayer[x] == 35. Later if current
	 * frame is 40 and there were no base frames on layer x with numbers
	 * between 36-40 then still _currentBaseFrameOnLayer[x] == 35.
	 */
	protected int _currentBaseFrameOnLayer[];
	/**
	 * Keeps track of which animation frame should be applied when rendering
	 * given layer. The idea behind this table is the same as with
	 * _currentBaseFrameOnLayer but instead of base frames, it keeps track of
	 * animation frames.
	 */
	protected int _currentAnimationFrameOnLayer[];
	/**
	 * Create a simple implementation of Effect renderer. It is based on
	 * open-raganrok implementation
	 * @param iEffect Effect to render
	 * @param iFrameAdvanceCalculator Object calculating which frame to
	 * calculate next. Depending on implementation this can allow to have a
	 * frame skip mechanism on the renderer level. Note that due to the STR
	 * file format skipping too many frames may result in skipping some base
	 * frames and hence miss parts of animation.
	 * @param iPreloadTextures If true then renderer will call
	 * {@link Texture#load(GL2)} on all textures used in all layers in
	 * {@link #initialize(GLAutoDrawable)} method to have them loaded before
	 * rendering begins. Naturally if textures are managed by an external
	 * texture manager, this may not be required.
	 * @param iRenderPosition Where the effect should be rendered.
	 * @param iXRotation Additional rotation if required.
	 * @param iYRotation Additional rotation if required.
	 * @param iZRotation Additional rotation if required.
	 * @param iXScale How much to scale.
	 * @param iYScale How much to scale.
	 * @param iZScale How much to scale.
	 * @throws RenderException If iEffect is null.
	 */
	public SimpleStrRenderer(
			Str iEffect,
			FrameAdvanceCalculator iFrameAdvanceCalculator,
			boolean iPreloadTextures,
			Point3D iRenderPosition,
			float iXRotation,
			float iYRotation,
			float iZRotation,
			float iXScale,
			float iYScale,
			float iZScale) throws RenderException {
		assert(iEffect != null);
		if(iEffect == null)
			throw new RenderException("Effect cannot be null");
		_frameAdvanceCalculator = iFrameAdvanceCalculator;
		_effect = iEffect;
		_preloadTextures = iPreloadTextures;
		_renderPosition = iRenderPosition;
		_xRotation = iXRotation;
		_yRotation = iYRotation;
		_zRotation = iZRotation;
		_xScale = iXScale;
		_yScale = iYScale;
		_zScale = iZScale;
	}
	@Override
	public void reset() {
		resetCurrentFrameTables();
		_lastRenderedFrame = FrameAdvanceCalculator.NO_FRAME;
	}
	/**
	 * Render a single frame. Preserves current matrix from being overwritten.
	 * @param ioCanvas OpenGL context
	 * @param iDelaySinceLastInvoke Delay in ms since last invoke.
	 */
	@Override
	public void renderFrame(
			GLAutoDrawable ioCanvas,
			long iDelaySinceLastInvoke) throws RenderException {
		GL2 gl = ioCanvas.getGL().getGL2();
		beforeRender(gl);
		render(gl, iDelaySinceLastInvoke);
		afterRender(gl);
	}
	/**
	 * Preserve current processing matrix and move rendering according to
	 * renderer settings.
	 * @param iGL GL context
	 */
	protected void beforeRender(GL2 iGL) {
		iGL.glPushMatrix();
		iGL.glTranslatef(
			_renderPosition._x,
			_renderPosition._y,
			_renderPosition._z);
		iGL.glRotatef(_xRotation, 1, 0, 0);
		iGL.glRotatef(_zRotation, 0, 0, 1);
		iGL.glRotatef(_yRotation, 0, 1, 0);
		iGL.glScalef(_xScale, _yScale, _zScale);
		iGL.glEnable(GL.GL_TEXTURE_2D);
	}
	/**
	 * Restore original matrix.
	 * @param iGL GL context
	 */
	protected void afterRender(GL2 iGL) {
		iGL.glDisable(GL.GL_TEXTURE_2D);
		iGL.glPopMatrix();
	}
	/**
	 * Main rendering method. Iterates through each layer and renders it.
	 * @param iGL
	 * @param iDelaySinceLastInvoke Time (in ms) since this method was last
	 * invoked. Required for calculating animation deltas and potential new
	 * key frames to apply
	 * @throws RenderException If something goes wrong with rendering (GL
	 * error, improper texture, etc.).
	 */
	protected void render(
			GL2 iGL,
			long iDelaySinceLastInvoke) throws RenderException {
		// few assertion to be sure we're sane
		assert(_effect != null);
		// Check which frame should we render
		int frameToRender =
			_frameAdvanceCalculator.calculateFrameToRender(
				iDelaySinceLastInvoke,
				_lastRenderedFrame);
		if(frameToRender >= _effect.get_frameCount()) {
			// Since we've made an animation loop first reset layer counters
			resetCurrentFrameTables();
			frameToRender %= _effect.get_frameCount();
		}
		int i = 0;
		for(Layer l : _effect.get_layers()) {
			renderLayer(
				i,
				l,
				frameToRender,
				iGL);
			i++;
		}
		_lastRenderedFrame = frameToRender;
	}
	/**
	 * Render a single layer. Texture and location data are taken from a current
	 * base key frame and then a current animation frame transformations are
	 * applied (given that animation frame has been reached).
	 * @param iLayerNumber Layer number in the effect stack (decides ordering
	 * on Z axis)
	 * @param iLayer layer object
	 * @param iFrameToRender which frame should be rendered (used to calculate
	 * current base and animation frames to use).
	 * @param iGL GL context
	 * @throws RenderException If anything goes wrong with rendering.
	 */
	private void renderLayer(
			int iLayerNumber,
			Layer iLayer,
			int iFrameToRender,
			GL2 iGL) throws RenderException {
		updateProcessedKeyFrames(
			iLayerNumber,
			iLayer.get_keyFrames(),
			iFrameToRender);

		if (_currentBaseFrameOnLayer[iLayerNumber]
			!= FrameAdvanceCalculator.NO_FRAME) {
			//We have a base frame to work on...
			float currentcolor[] = new float[4];
			iGL.glGetFloatv(GL2.GL_CURRENT_COLOR, currentcolor, 0);
			KeyFrame baseFrame =
				iLayer.get_keyFrames().get(
					_currentBaseFrameOnLayer[iLayerNumber]);
			if ( true ) {
				Color finalColor = new Color(baseFrame.get_color());
				Point2D finalPosition =
						new Point2D( // translate by character size
							baseFrame.get_position()._x - 320,
							baseFrame.get_position()._y - 290);
				ObjectHolder<Float> finalRotation =
					new ObjectHolder<Float>(
						baseFrame.get_rotation() / STR_ANGLE_TO_DEGREES);
				Rectangle<Point2D> finalRectangle =
					new Rectangle<Point2D>(
						new Point2D(baseFrame.get_drawingRectangle().get_a()),
						new Point2D(baseFrame.get_drawingRectangle().get_b()),
						new Point2D(baseFrame.get_drawingRectangle().get_c()),
						new Point2D(baseFrame.get_drawingRectangle().get_d()));
				Rectangle<Point2D> finalTextureMapping =
					new Rectangle<Point2D>(
						new Point2D(baseFrame.get_textureUVMapping().get_a()),
						new Point2D(baseFrame.get_textureUVMapping().get_b()),
						new Point2D(baseFrame.get_textureUVMapping().get_c()),
						new Point2D(baseFrame.get_textureUVMapping().get_d()));
				
				float finalTextureId =baseFrame.get_textureId();


				if (_currentAnimationFrameOnLayer[iLayerNumber]
					!= FrameAdvanceCalculator.NO_FRAME) {
					KeyFrame animationFrame =
						iLayer.get_keyFrames().get(
							_currentAnimationFrameOnLayer[iLayerNumber]);
					applyAnimationFrame(animationFrame, iFrameToRender,
							finalColor, finalPosition, finalRotation,
							finalRectangle, finalTextureMapping , 0);
										
				}
				
				//finalTextureId=(float)Math.random()*10;
				
				//prevent out of bounds
				if (finalTextureId<0) {
					finalTextureId*=-1;
				}
				
				if ( baseFrame.get_animationType() != AnimationType.NO_CHANGE ) {
					finalTextureId += 
							(baseFrame.get_animationDelta() +0) * 2 *
							(iFrameToRender - baseFrame.get_framenum() +0);
					
					//System.out.print(finalTextureId + " ");
				}
				
				if ( baseFrame.get_animationType() == AnimationType.TYPE_2 && ((int)finalTextureId >= iLayer.get_textures().size()) ) {
					finalTextureId=(float)(iLayer.get_textures().size()-1);		
				
				}
				
				finalTextureId%=iLayer.get_textures().size();
				
					
				Texture texture =
						iLayer.get_textures().get((int)finalTextureId);		
				
				if(!texture.isLoaded())
					try {
						texture.load(iGL);
					} catch (ResourceException e) {
						throw new RenderException(
							"Could not load texture: "
							+ texture
							+ ". Reason: "
							+ e);
					}
				
				if (iLayerNumber==0) {
					iGL.glDisable(GL2.GL_TEXTURE_2D);
					
					finalRectangle._a._x = -400;
					finalRectangle._a._y = 300;
					finalRectangle._b._x = 400;
					finalRectangle._b._y = 300;
					finalRectangle._d._x = -400;
					finalRectangle._d._y = -300;
					finalRectangle._c._x = 400;
					finalRectangle._c._y = -300;
					
					iGL.glBlendFunc( GL2.GL_ONE,GL2.GL_ZERO);			

					
				} else {
					iGL.glEnable(GL2.GL_TEXTURE_2D);

					
					// linear filter
					iGL.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR); // or NEAREST
					
				}
				
				texture.bind(iGL);				
				iGL.glPushMatrix();

				Billboard(iGL);
				iGL.glColor4ub(
					(byte)finalColor._r,
					(byte)finalColor._g,
					(byte)finalColor._b,
					(byte)finalColor._alpha);
				
				iGL.glTranslatef(
					finalPosition._x,
					finalPosition._y,
					0f);
				iGL.glRotatef(finalRotation.getObject(), 0, 0, 1);

				iGL.glEnable(GL.GL_BLEND);

				iGL.glBlendFunc( baseFrame.get_sourceBlend().toGLValue(),
				baseFrame.get_destBlend().toGLValue());			

				
				iGL.glColorMask(true, true, true, false);
;
				iGL.glBegin(GL2.GL_QUADS);				//BEGIN ----------------
				
				iGL.glTexCoord2f(
					finalTextureMapping._d._x,
					finalTextureMapping._d._y);
				iGL.glVertex3f(
					finalRectangle._c._x,
					finalRectangle._c._y,
					0.02f * iLayerNumber);

				iGL.glTexCoord2f(
					finalTextureMapping._c._x,
					finalTextureMapping._c._y);
				iGL.glVertex3f(
					finalRectangle._d._x,
					finalRectangle._d._y,
					0.02f * iLayerNumber);

				iGL.glTexCoord2f(
					finalTextureMapping._a._x,
					finalTextureMapping._a._y);
				iGL.glVertex3f(
					finalRectangle._a._x,
					finalRectangle._a._y,
					0.02f * iLayerNumber);

				iGL.glTexCoord2f(
					finalTextureMapping._b._x,
					finalTextureMapping._b._y);
				iGL.glVertex3f(
					finalRectangle._b._x,
					finalRectangle._b._y,
					0.02f * iLayerNumber);
				iGL.glEnd();

				iGL.glColorMask(true, true, true, true);
				iGL.glDisable(GL.GL_BLEND);
				iGL.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
				iGL.glColor4f(
					currentcolor[0],
					currentcolor[1],
					currentcolor[2],
					currentcolor[3]);
				iGL.glPopMatrix();
			}
		}
	}
	/**
	 * Make the effect face us.
	 * @param iGL GL context
	 */
	private void Billboard(GL2 iGL) {
		// Cheat Cylindrical. Credits:
		// http://www.lighthouse3d.com/opengl/billboarding/index.php3?billCheat1
		float modelview[] = new float[16];
		iGL.glGetFloatv(GL2.GL_MODELVIEW_MATRIX , modelview, 0);
		float v;
		for( int i=0; i<3; i+=2 )
		    for( int j=0; j<3; j++ ) {
				if ( i==j ) {
					if (i == 0)
						v = _xScale;
					else if (i == 1)
						v = _yScale;
					else
						v = 1.0f;
					modelview[i*4+j] = v;
				}
				else {
					modelview[i*4+j] = 0.0f;
				}
		    }
		iGL.glLoadMatrixf(modelview,0);
	}
	/**
	 * Applies animation frame modifications unto given set of drawing
	 * parameters.
	 * @param iAnimationFrame Animation frame to apply
	 * @param iFrameToRender current rendered frame (used to calculate
	 * animation intensity)
	 * @param ioFinalColor Base color to modify
	 * @param ioFinalPosition Position to modify
	 * @param ioFinalRotation Rotation of the texture to modify
	 * @param ioFinalRectangle Texture rectangle to modify
	 * @param ioFinalTextureMapping Texture mapping to modify
	 * @param ioFinalTextureMapping Texture mapping to modify
	 */
	private void applyAnimationFrame(KeyFrame iAnimationFrame,
			int iFrameToRender, Color ioFinalColor, Point2D ioFinalPosition,
			ObjectHolder<Float> ioFinalRotation, Rectangle<Point2D> ioFinalRectangle,
			Rectangle<Point2D> ioFinalTextureMapping, float ioFinalTextureId) {
		int anifactor =
			iFrameToRender - iAnimationFrame.get_framenum();
		
			
		ioFinalColor._r += iAnimationFrame.get_color()._r * anifactor;
		ioFinalColor._g += iAnimationFrame.get_color()._g * anifactor;
		ioFinalColor._b += iAnimationFrame.get_color()._b * anifactor;
		ioFinalColor._alpha += 
			iAnimationFrame.get_color()._alpha * anifactor;
		ioFinalPosition._x +=
			iAnimationFrame.get_position()._x * anifactor;
		ioFinalPosition._y +=
				iAnimationFrame.get_position()._y * anifactor;
		ioFinalRotation.setObject(ioFinalRotation.getObject()
			+ ((iAnimationFrame.get_rotation() / STR_ANGLE_TO_DEGREES)
				* anifactor));
		ioFinalRectangle._a._x +=
				iAnimationFrame.get_drawingRectangle()._a._x
				* anifactor;
		ioFinalRectangle._a._y +=
				iAnimationFrame.get_drawingRectangle()._a._y
				* anifactor;
		ioFinalRectangle._b._x +=
				iAnimationFrame.get_drawingRectangle()._b._x
				* anifactor;
		ioFinalRectangle._b._y +=
				iAnimationFrame.get_drawingRectangle()._b._y
				* anifactor;
		ioFinalRectangle._c._x +=
				iAnimationFrame.get_drawingRectangle()._c._x
				* anifactor;
		ioFinalRectangle._c._y +=
				iAnimationFrame.get_drawingRectangle()._c._y
				* anifactor;
		ioFinalRectangle._d._x +=
				iAnimationFrame.get_drawingRectangle()._d._x
				* anifactor;
		ioFinalRectangle._d._y +=
				iAnimationFrame.get_drawingRectangle()._d._y
				* anifactor;

		ioFinalTextureMapping._a._x +=
				iAnimationFrame.get_textureUVMapping()._a._x
				* anifactor;
		ioFinalTextureMapping._a._y +=
				iAnimationFrame.get_textureUVMapping()._b._y
				* anifactor;
		ioFinalTextureMapping._b._x +=
				iAnimationFrame.get_textureUVMapping()._b._x
				* anifactor;
		ioFinalTextureMapping._b._y +=
				iAnimationFrame.get_textureUVMapping()._c._y
				* anifactor;
		ioFinalTextureMapping._c._x +=
				iAnimationFrame.get_textureUVMapping()._c._x
				* anifactor;
		ioFinalTextureMapping._c._y +=
				iAnimationFrame.get_textureUVMapping()._d._y
				* anifactor;
		ioFinalTextureMapping._d._x +=
				iAnimationFrame.get_textureUVMapping()._d._x
				* anifactor;
		ioFinalTextureMapping._d._y +=
				iAnimationFrame.get_textureUVMapping()._a._y
				* anifactor;
	}
	/**
	 * Updates indexes of currently processed key frames for a layer given that
	 * we're currently at frame number iFrameToRender.
	 * @param iLayerNumber Layer number
	 * @param iFrameToRender
	 * @throws ResourceException
	 */
	private void updateProcessedKeyFrames(
			int iLayerNumber,
			List<KeyFrame> iLayerFrames,
			int iFrameToRender) throws RenderException {
		boolean found = false;
		// First check if current frame to render is a base or animation
		// frame on this layer.
		for(
				int frameIdx = 1 + // start looking next frame after...
					(_currentAnimationFrameOnLayer[iLayerNumber]
					!= FrameAdvanceCalculator.NO_FRAME ?
					// After current animation frame because it's always after
					// base frame.
						_currentAnimationFrameOnLayer[iLayerNumber]
					// Since there is no animation frame processed then base
					// frame will be the best place to start search
						: _currentBaseFrameOnLayer[iLayerNumber]);
				frameIdx < iLayerFrames.size() // look until the end...
				; frameIdx++) {
			KeyFrame kf = iLayerFrames.get(frameIdx);
			if(kf.get_framenum() == iFrameToRender) {
				if(kf.get_frameType() == KeyFrameType.BASIC) {
					// new base frame, use it and reset animation frame
					// since it should end
					_currentBaseFrameOnLayer[iLayerNumber] = frameIdx;
					_currentAnimationFrameOnLayer[iLayerNumber] =
						FrameAdvanceCalculator.NO_FRAME;
					found = true;
				} else if (kf.get_frameType() == KeyFrameType.MORPH) {
					// We got a new animation frame so set it to apply
					// to the basic frame that will be processed.
					_currentAnimationFrameOnLayer[iLayerNumber] =
							frameIdx;
					found = true;
				} else {
					throw new RenderException(
						"Unknown frame type "
						+ kf.get_frameType()
						+ "cannot render");
				}
			}
			else if(kf.get_framenum() > iFrameToRender) {
				break;
			}
		}
		if(!found
			&& _currentAnimationFrameOnLayer[iLayerNumber]
				== FrameAdvanceCalculator.NO_FRAME
			&& _currentBaseFrameOnLayer[iLayerNumber]
				== iLayerFrames.size() - 1)
			_currentBaseFrameOnLayer[iLayerNumber] =
				FrameAdvanceCalculator.NO_FRAME;
	}

	@Override
	public void initialize(GLAutoDrawable ioDrawable) throws ResourceException {
		GL2 gl = ioDrawable.getGL().getGL2();
		if(_preloadTextures) {
			for(Layer l : _effect.get_layers())
				for(Texture t : l.get_textures())
					t.load(gl);
		}
		resetCurrentFrameTables();
	}
	/**
	 * Resets tables which indicate current processing frames per layer.
	 */
	private void resetCurrentFrameTables() {
		_currentBaseFrameOnLayer = new int[_effect.get_layers().size()];
		_currentAnimationFrameOnLayer = new int[_effect.get_layers().size()];
		for(int i = 0; i < _effect.get_layers().size(); i++) {
			_currentBaseFrameOnLayer[i] = FrameAdvanceCalculator.NO_FRAME;
			_currentAnimationFrameOnLayer[i] = FrameAdvanceCalculator.NO_FRAME;
		}
	}

	@Override
	public void dispose(GLAutoDrawable ioDrawable) {
	}

	@Override
	public void handleReshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
		// nothing to do.
	}
}
