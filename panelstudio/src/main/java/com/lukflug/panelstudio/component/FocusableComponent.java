package com.lukflug.panelstudio.component;

import com.lukflug.panelstudio.base.Context;
import com.lukflug.panelstudio.base.IBoolean;

/**
 * Base class for all components included in this library.
 * @author lukflug
 */
public abstract class FocusableComponent extends ComponentBase {
	/**
	 * The focus state for this component.
	 */
	private boolean focus=false;

	/**
	 * Constructor.
	 * @param title the caption for this component
	 * @param description the description for this component
	 * @param renderer whether this component is visible
	 */
	public FocusableComponent (String title, String description, IBoolean visible) {
		super(title,description,visible);
	}
	
	@Override
	public void handleButton (Context context, int button) {
		super.handleButton(context,button);
		updateFocus(context,button);
	}
	
	@Override
	public void releaseFocus() {
		focus=false;
	}
	
	/**
	 * Get current focus state.
	 * @param context the {@link Context} for the component
	 * @return set to true, if the component has focus
	 */
	public boolean hasFocus (Context context) {
		return context.hasFocus()&&focus;
	}
	
	/**
	 * If the button is being pressed, update focus state to hover state and call {@link #handleFocus(Context, boolean)}.
	 * @param context the {@link Context} for the component
	 * @param button the mouse button state that changed
	 * @see Interface#LBUTTON
	 * @see Interface#RBUTTON
	 */
	protected void updateFocus (Context context, int button) {
		if (context.getInterface().getButton(button)) {
			focus=context.isHovered();
			handleFocus(context,focus&&context.hasFocus());
		}
	}
	
	/**
	 * Does nothing, called when the focus state changes due to a mouse event.
	 * @param context the {@link Context} for the component
	 * @param focus the new focus state
	 */
	protected void handleFocus (Context context, boolean focus) {
	}
}
