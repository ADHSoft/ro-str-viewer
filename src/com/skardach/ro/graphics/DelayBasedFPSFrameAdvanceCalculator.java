package com.skardach.ro.graphics;
/**
 * This implementation calculates the next frame based on the delay
 * since last call and given required frames per second count. This allows to
 * have multiple renderers, each with different FPS draw stuff on a single
 * GLCanvas with one animator.
 * FIXME Resuming animation is not working well when rendering effects.
 * @author Stanislaw Kardach
 *
 */
public class DelayBasedFPSFrameAdvanceCalculator implements FrameAdvanceCalculator {
	float _timeRemainderSinceLastFrame = 0;
	private int _fps;
	/**
	 * Create delay based frame advance calculator that should calculate frames
	 * according to given frame per second value.
	 * @param iFPS Try to generate this many frames per second.
	 */
	public DelayBasedFPSFrameAdvanceCalculator(int iFPS) {
		_fps = iFPS;
	}

	@Override
	/**
	 * Calculate which frame to render next. FPS in the effect files are crap
	 * so assume 30 FPS as the original client.
	 * @param iDelaySinceLastInvoke
	 * @param iLastRenderedFrame Number of frame that was previously rendered
	 * @return
	 */
	public int calculateFrameToRender(	//toDo fix speed
			long iDelaySinceLastInvoke,
			int iLastRenderedFrame) {
		int frameToRender = 0;
		if(iLastRenderedFrame != NO_FRAME) { // always start with first frame
			frameToRender =
				iLastRenderedFrame // last frame rendered
				+ (int)(( // + delay since last frame / time for single frame
					iDelaySinceLastInvoke + _timeRemainderSinceLastFrame)
					/ (700.0f/_fps)); // 1s / FPS
			_timeRemainderSinceLastFrame =
					 ((float)(iDelaySinceLastInvoke + _timeRemainderSinceLastFrame)
					% (700.0f/_fps));
		}
		return frameToRender;
	}


}
