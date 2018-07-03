/*
 * Copyright (C) 2015 Panemu.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package com.panemu.tiwulfx.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author amrullah
 */
public class LiteralUtil {

	private ResourceBundle literalBundle;
	private List<String> lstFiles = new ArrayList<>();
	private final static String DEFAULT_LITERAL = "com.panemu.tiwulfx.res.literal";
	private Locale locale;
//	private static Log log = LogFactory.getLog(LiteralUtil.class);
	private Logger logger = Logger.getLogger(LiteralUtil.class.getName());

	public LiteralUtil(Locale locale) {
		this.locale = locale;
		init();
	}

	private void init() {
		ResourceBundle.clearCache();
		literalBundle = ResourceBundle.getBundle(DEFAULT_LITERAL, locale);
		if (!"en".equals(locale.getLanguage()) && logger.isLoggable(Level.WARNING)) {
			String foreignLang = DEFAULT_LITERAL + "_" + locale.getLanguage();
			URL url = LiteralUtil.class.getResource(toResourceName(foreignLang));
			if (url == null && !"".equals(locale.getCountry())) {
				foreignLang = foreignLang + "_" + locale.getCountry();
				url = LiteralUtil.class.getResource(toResourceName(foreignLang));
			}
			if (url == null) {
				logger.log(Level.SEVERE, "Unable to find Tiwulfx Resource Bundle for current locale. Please create " + toResourceName(foreignLang) + " resource bundle file.");
			}
		}
	}

	public ResourceBundle addLiteral(String baseName) {
		if (!lstFiles.contains(baseName)) {
			lstFiles.add(baseName);
			build(baseName);
		}
		return literalBundle;
	}

	public ResourceBundle changeLocale(Locale locale) {
		this.locale = locale;
		init();
		for (String baseName : lstFiles) {
			build(baseName);
		}
		return literalBundle;
	}

	private ResourceBundle build(String baseName) {

		ResourceBundle.Control cntl = ResourceBundle.Control.getControl(Collections.unmodifiableList(Arrays.asList("java.properties")));
		List<Locale> lcl = cntl.getCandidateLocales(baseName, locale);
		boolean found = false;
		for (int i = lcl.size() - 1; i >= 0; i--) {
			Locale loc = lcl.get(i);
			String file = baseName;
			if (!"".equals(loc.getLanguage())) {
				file = file + "_" + loc.getLanguage();
				if (!"".equals(loc.getScript())) {
					file = file + "_" + loc.getScript();
				}
				if (!"".equals(loc.getCountry())) {
					file = file + "_" + loc.getCountry();
				}
			}
			file = toResourceName(file);
			try (InputStream is = LiteralUtil.class.getResourceAsStream(file)) {
				if (is != null) {
					try (InputStreamReader reader = new InputStreamReader(is, "UTF-8")) {
						literalBundle = new TiwulFXResourceBundle(reader);
						found = true;
					}
					if (logger.isLoggable(Level.INFO)) {
						logger.log(Level.INFO, "found resource bundle: " + file);
					}
				} else if (logger.isLoggable(Level.WARNING)) {
					logger.log(Level.WARNING, "unable to find resource bundle: " + file);
				}
			} catch (IOException ex) {
				if (logger.isLoggable(Level.WARNING)) {
					logger.log(Level.WARNING, "unable to load properties file: " + file + "\n" + ex.getMessage());
				}
			}

		}
		if (!found) {
			throw new RuntimeException("Unable to load resource bundle: " + baseName);
		}
		return literalBundle;
	}

	private String toResourceName(String bundleName) {
		String suffix = "properties";
		StringBuilder sb = new StringBuilder(bundleName.length() + 2 + suffix.length());
		sb.append("/");
		sb.append(bundleName.replace('.', '/')).append('.').append(suffix);
		return sb.toString();
	}

	private class TiwulFXResourceBundle extends PropertyResourceBundle {

		public TiwulFXResourceBundle(Reader reader) throws IOException {
			super(reader);
			if (literalBundle != null) {
				super.setParent(literalBundle);
			}
		}

	}

}
