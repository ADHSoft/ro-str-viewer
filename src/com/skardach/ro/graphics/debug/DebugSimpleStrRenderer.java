package com.skardach.ro.graphics.debug;

import java.awt.Font;
import java.util.Arrays;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

import com.jogamp.opengl.util.awt.TextRenderer;
import com.skardach.ro.graphics.FrameAdvanceCalculator;
import com.skardach.ro.graphics.Point3D;
import com.skardach.ro.graphics.RenderException;
import com.skardach.ro.graphics.SimpleStrRenderer;
import com.skardach.ro.resource.str.Str;
/**
 * Debug wrapper over SimpleStrRenderer which displays key frame table states
 * on the rendered canvas.
 * @author Stanislaw Kardach
 *
 */
public class DebugSimpleStrRenderer extends SimpleStrRenderer {
	TextRenderer _textRenderer;
	/**
	 * Default constructors. Parameters same as for {@link SimpleStrRenderer}
	 * @param iEffect See {@link SimpleStrRenderer}
	 * @param iFrameAdvanceCalculator See {@link SimpleStrRenderer}
	 * @param iPreloadTextures See {@link SimpleStrRenderer}
	 * @param iRenderPosition See {@link SimpleStrRenderer}
	 * @param iXRotation See {@link SimpleStrRenderer}
	 * @param iYRotation See {@link SimpleStrRenderer}
	 * @param iZRotation See {@link SimpleStrRenderer}
	 * @param iXScale See {@link SimpleStrRenderer}
	 * @param iYScale See {@link SimpleStrRenderer}
	 * @param iZScale See {@link SimpleStrRenderer}
	 * @throws RenderException See {@link SimpleStrRenderer}
	 */
	public DebugSimpleStrRenderer(
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
		super(
			iEffect,
			iFrameAdvanceCalculator,
			iPreloadTextures,
			iRenderPosition,
			iXRotation,
			iYRotation,
			iZRotation,
			iXScale,
			iYScale,
			iZScale);
	}
	/**
	 * Draws the number of last rendered frame, calculated frame to render and
	 * state the of key frame tables before the SimpleStrRenderer renders a
	 * frame.
	 * @param iGL OpenGL context
	 * @param ioCanvas GL drawable to draw the statistics on
	 * @param iDelaySinceLastInvoke Delay in ms since last invoked.
	 */
	protected void beforeRender(GL2 iGL, GLAutoDrawable ioCanvas, long iDelaySinceLastInvoke) {
		if(_textRenderer == null)
			_textRenderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 14));
		_textRenderer.beginRendering(ioCanvas.getSurfaceWidth(), ioCanvas.getSurfaceHeight());
		_textRenderer.setColor(1, 1, 1, 1);
		_textRenderer.draw("[Before] Frame: " + _lastRenderedFrame, 10, 64);
		_textRenderer.draw("[Before] Frame to render: " + _frameAdvanceCalculator.calculateFrameToRender(iDelaySinceLastInvoke, _lastRenderedFrame), 10, 46);
		_textRenderer.draw("[Before] BasicFrames: " + Arrays.toString(_currentBaseFrameOnLayer), 10, 28);
		_textRenderer.draw("[Before] AnimaFrames: " + Arrays.toString(_currentAnimationFrameOnLayer), 10, 10);
		_textRenderer.endRendering();
		super.beforeRender(iGL);
	}
	/**
	 * Draws the number of last rendered frame and the state of key frame
	 * tables after SimpleStrRenderer has finished rendering the frame.
	 * @param iGL OpenGL context
	 * @param ioCanvas GL drawable to draw the statistics on
	 */
	protected void afterRender(GL2 iGL, GLAutoDrawable ioCanvas) {
		super.afterRender(iGL);
		_textRenderer.beginRendering(ioCanvas.getSurfaceWidth(), ioCanvas.getSurfaceHeight());
		_textRenderer.setColor(1, 1, 1, 1);
		_textRenderer.draw("[After]  Frame: " + _lastRenderedFrame, 10, 122);
		_textRenderer.draw("[After]  BasicFrames: " + Arrays.toString(_currentBaseFrameOnLayer), 10, 104);
		_textRenderer.draw("[After]  AnimaFrames: " + Arrays.toString(_currentAnimationFrameOnLayer), 10, 86);
		_textRenderer.endRendering();
	}
	@Override
	public void renderFrame(
			GLAutoDrawable ioCanvas,
			long iDelaySinceLastInvoke) throws RenderException {
		GL2 gl = ioCanvas.getGL().getGL2();
		beforeRender(gl, ioCanvas, iDelaySinceLastInvoke);
		render(gl, iDelaySinceLastInvoke);
		afterRender(gl, ioCanvas);
	}
}
