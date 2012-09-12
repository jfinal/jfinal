package com.jfinal.render;

/**
 * IMainRenderFactory. Create Render for Controller.render(String view);
 */
public interface IMainRenderFactory {
	
	/**
	 * Return the render.
	 * @param view the view for this render.
	 */
	Render getRender(String view);
	
	/**
	 * The extension of the view.
	 * <p>
	 * It must start with dot char "."
	 * Example: ".html" or ".ftl"
	 * </p>
	 */
	String getViewExtension();
}


