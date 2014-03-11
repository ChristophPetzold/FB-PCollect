/**
 * 
 */
package de.cpe.fb.pagecollector.core;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * TODO add type description
 * 
 * @author Christoph
 * 
 */
public class Config {
	private static final String					BUNDLE_NAME			= "de.cpe.fb.pagecollector.custom.config";	//$NON-NLS-1$

	private static final ResourceBundle	RESOURCE_BUNDLE	= ResourceBundle.getBundle(BUNDLE_NAME);

	private Config() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
