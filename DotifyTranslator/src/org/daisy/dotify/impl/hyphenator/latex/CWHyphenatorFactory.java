package org.daisy.dotify.impl.hyphenator.latex;

import java.util.logging.Logger;

import org.daisy.dotify.api.hyphenator.HyphenatorConfigurationException;
import org.daisy.dotify.api.hyphenator.HyphenatorFactory;
import org.daisy.dotify.api.hyphenator.HyphenatorInterface;
import org.daisy.dotify.text.FilterLocale;

public class CWHyphenatorFactory implements HyphenatorFactory {
	private int accuracy = 5;

	public boolean supportsLocale(String locale) {
		return CWHyphenator.supportsLocale(FilterLocale.parse(locale));
	}

	public HyphenatorInterface newHyphenator(String locale) throws HyphenatorConfigurationException {
		return new CWHyphenator(FilterLocale.parse(locale), accuracy);
	}

	public Object getFeature(String key) {
		if (key.equals(HyphenatorFactory.FEATURE_HYPHENATION_ACCURACY)) {
			return accuracy;
		}
		return null;
	}

	public void setFeature(String key, Object value) throws HyphenatorConfigurationException {
		if (key.equals(HyphenatorFactory.FEATURE_HYPHENATION_ACCURACY)) {
			accuracy = (Integer)value;
			if (accuracy !=5 && accuracy != 3) {
				Logger.getLogger(this.getClass().getCanonicalName()).fine(
"Feature " + HyphenatorFactory.FEATURE_HYPHENATION_ACCURACY + " set to an unsupported value: " + accuracy + ". Supported values are 3 and 5.");
			}
		} else {
			throw new LatexHyphenatorConfigurationException();
		}
	}

}