package org.daisy.dotify.impl.text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.daisy.dotify.api.text.Integer2TextFactory;
import org.daisy.dotify.api.text.Integer2TextFactoryService;

import aQute.bnd.annotation.component.Component;

@Component
public class EnglishInteger2TextFactoryService implements
		Integer2TextFactoryService {
	
	private final static List<String> locales;
	static {
		locales = new ArrayList<>();
		locales.add("en");
	}

	@Override
	public boolean supportsLocale(String locale) {
		for (String l : locales) {
			if (l.equalsIgnoreCase(locale)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Integer2TextFactory newFactory() {
		return new EnglishInteger2TextFactory();
	}

	@Override
	public Collection<String> listLocales() {
		return locales;
	}

}
