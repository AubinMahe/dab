package org.hpms.automaton.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin implements Constants {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.hpms.automaton.zest"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	@Override
	protected void initializeImageRegistry( ImageRegistry reg ) {
	   super.initializeImageRegistry( reg );
	   final Bundle b = FrameworkUtil.getBundle( getClass());
      reg.put( ICON_KEY_NEW   , ImageDescriptor.createFromURL( b.getEntry( ICON_PATH_NEW )));
      reg.put( ICON_KEY_RENAME, ImageDescriptor.createFromURL( b.getEntry( ICON_PATH_RENAME )));
      reg.put( ICON_KEY_DELETE, ImageDescriptor.createFromURL( b.getEntry( ICON_PATH_DELETE )));
	}
}
