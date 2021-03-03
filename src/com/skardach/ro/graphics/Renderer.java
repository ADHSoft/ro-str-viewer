package com.skardach.ro.graphics;

import com.jogamp.opengl.GLAutoDrawable;

import com.skardach.ro.resource.ResourceException;

/**
 * Interface for a renderer object
 * @author Stanislaw Kardach
 *
 */
public interface Renderer {
	/**
	 * Should render a single frame on a given surface.
	 * @param ioDrawable Target surface to draw on.
	 * @param iDelaySinceLastInvoke Delay (in milliseconds) since last call so
	 * that implementation can perform some frame skipping if needed.
	 * On the first call to the renderer this should be 0.
	 *
	 * Remark: In this code Renderers are invoked via JOGL FPSAnimator class,
	 * which is set to perform an arbitrary number of refresh operations per
	 * second so instead of passing a delay, a current frame number could be
	 * passed. However If for any reason the animator class fails to run at a
	 * requested speed, the animation would appear to be slowed down. Time
	 * delay allows to perform animation steps irrespectively of the Animator
	 * slow downs (and of course it allows frame skips).
	 * @throws RenderException Whenever something goes wrong with rendering
	 */
	public void renderFrame(
			GLAutoDrawable ioDrawable,
			long iDelaySinceLastInvoke) throws RenderException;
	/**
	 * Initialise renderer to be able to draw on a given  surface.
	 * It is safe to assume that every call to this method should
	 * reset any animation process that implementation might have ongoing.
	 * @param ioDrawable Surface to adapt to.
	 * @throws ResourceException If there is a problem with rendering
	 */
	public void initialize(GLAutoDrawable ioDrawable) throws ResourceException;
	/**
	 * Clean up after rendering process, maybe close resources
	 * @param ioDrawable
	 */
	public void dispose(GLAutoDrawable ioDrawable);
	/**
	 * Handle surface resize. If using JOGL then this method can be empty since
	 * JOGL is performing resizing of the GLCanvas and underlying OpenGL
	 * context.
	 * @param drawable surface
	 * @param x new x coordinate of surface (viewport)
	 * @param y new y coordinate of surface (viewport)
	 * @param width new width of the surface (viewport)
	 * @param height new height of the surface (viewport)
	 */
	public void handleReshape(
			GLAutoDrawable drawable,
			int x,
			int y,
			int width,
			int height);
	/**
	 * Reset renderer state and go back to rendering the first frame
	 */
	public void reset();

}
