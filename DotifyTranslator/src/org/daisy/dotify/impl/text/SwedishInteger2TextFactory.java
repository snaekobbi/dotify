package org.daisy.dotify.impl.text;

import org.daisy.dotify.api.text.Integer2Text;
import org.daisy.dotify.api.text.Integer2TextConfigurationException;
import org.daisy.dotify.api.text.Integer2TextFactory;
import org.daisy.dotify.text.FilterLocale;

public class SwedishInteger2TextFactory implements Integer2TextFactory {
	private final static FilterLocale sv_SE = FilterLocale.parse("sv-SE");

	public boolean supportsLocale(String locale) {
		return FilterLocale.parse(locale).equals(sv_SE);
	}

	public Integer2Text newInteger2Text(String locale) throws Integer2TextConfigurationException {
		return new BasicInteger2Text(new SvInt2TextLocalization());
	}

	public Object getFeature(String key) {
		return null;
	}

	public void setFeature(String key, Object value) throws Integer2TextConfigurationException {
		throw new SwedishInteger2TextConfigurationException();
	}
	
	private class SwedishInteger2TextConfigurationException extends Integer2TextConfigurationException {

		/**
		 * 
		 */
		private static final long serialVersionUID = -7090139699406930899L;
		
	}

}
