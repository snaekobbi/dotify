package org.daisy.dotify.impl.input;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daisy.dotify.SystemKeys;
import org.daisy.dotify.input.InputManager;
import org.daisy.dotify.system.InternalTask;
import org.daisy.dotify.system.RunParameters;
import org.daisy.dotify.system.TaskSystemException;
import org.daisy.dotify.system.ValidatorTask;
import org.daisy.dotify.system.XsltTask;
import org.daisy.dotify.tools.ResourceLocator;
import org.daisy.dotify.tools.ResourceLocatorException;
import org.daisy.util.xml.peek.PeekResult;
import org.daisy.util.xml.peek.Peeker;
import org.daisy.util.xml.peek.PeekerPool;
import org.xml.sax.SAXException;

/**
 * <p>Provides a method to determine the input format and load the 
 * appropriate settings based on the detected input format.</p>
 * 
 * <p>The InputDetectorTaskSystem is specifically designed to aid 
 * the process of selecting and executing the correct validation rules 
 * and transformation for a given input document and locale.</p>
 * 
 * <p>Note that, input format must be well-formed XML.</p>
 * 
 * <p>Resources are located in the following order:</p>
 * <ul> 
 * <li>localBase/[output format]/[input format].properties</li>
 * <li>localBase/[output format]/xml.properties</li>
 * <li>commonBase/[output format]/[input format].properties</li>
 * <li>commonBase/[output format]/xml.properties</li>
 * </ul>
 * <p>The properties file for the format should contain two entries:</p>
 * <ul>
 * <li>&lt;entry key="validation"&gt;path/to/schema/file&lt;/entry&gt;</li>
 * <li>&lt;entry key="transformation"&gt;path/to/xslt/file&lt;/entry&gt;</li>
 * </ul>
 * <p>Paths in the properties file are relative to the resource base url.</p>
 * <p>Whitespace normalization of the OBFL file is added last in the chain.</p>
 * 
 * @author Joel Håkansson, TPB
 *
 */
class XMLInputManager implements InputManager {
	private final static String CONFIG_PATH = "config-files/";
	private final ResourceLocator localLocator;
	private final ResourceLocator commonLocator;
	private final String name;
	private final Logger logger;

	/**
	 * Create a new InputDetectorTaskSystem. 
	 * @param locator the resource locator root 
	 * @param localBase a path relative the resource root to the local resources
	 * @param commonBase a path relative the resource root to the common resources
	 */
	XMLInputManager(ResourceLocator localLocator, ResourceLocator commonLocator) {
		this(localLocator, commonLocator, "XMLInputManager");
	}
	
	XMLInputManager(ResourceLocator localLocator, ResourceLocator commonLocator, String name) {
		this.localLocator = localLocator;
		this.commonLocator = commonLocator;
		this.name = name;
		this.logger = Logger.getLogger(XMLInputManager.class.getCanonicalName());
	}
	
	public String getName() {
		return name;
	}

	public List<InternalTask> compile(RunParameters parameters)
			throws TaskSystemException {

		String input = parameters.getProperty(SystemKeys.INPUT);
		String inputformat = null;
		Peeker peeker = null;
		FileInputStream is = null;
		try {
			PeekResult peekResult;
			peeker = PeekerPool.getInstance().acquire();
			is = new FileInputStream(new File(input));
			peekResult = peeker.peek(is);
			String rootNS = peekResult.getRootElementNsUri();
			String rootElement = peekResult.getRootElementLocalName();
			DefaultInputUrlResourceLocator p = DefaultInputUrlResourceLocator.getInstance();

			inputformat = p.getConfigFileName(rootElement, rootNS);
			if (inputformat !=null && "".equals(inputformat)) {
				return new ArrayList<InternalTask>();
			}
		} catch (SAXException e) {
			throw new TaskSystemException("SAXException while reading input", e);
		} catch (IOException e) {
			throw new TaskSystemException("IOException while reading input", e);
		}  finally {
			if (peeker!=null) {
				PeekerPool.getInstance().release(peeker);
			}
			if (is!=null) {
				try { is.close(); } catch (IOException e) { }
			}
		}
		Properties p = new Properties();
		try {
			try {
				p.loadFromXML(localLocator.getResource(CONFIG_PATH+"output_mode_catalog.xml").openStream());
			} catch (ResourceLocatorException e1) {
				try {
					p.loadFromXML(commonLocator.getResource(CONFIG_PATH+"output_mode_catalog.xml").openStream());
				} catch (ResourceLocatorException e2) {
					logger.log(Level.FINE, "Failed to localize resource.", e2);
				}
			}
		} catch (InvalidPropertiesFormatException e1) {
			logger.log(Level.FINE, "Invalid format.", e1);
		} catch (IOException e1) {
			logger.log(Level.FINE, "I/O error.", e1);
		}
		
		String xmlformat = "xml.properties";
		String outputMode = p.getProperty(parameters.getProperty(SystemKeys.OUTPUT_FORMAT).toLowerCase());
		if (outputMode==null) {
			logger.info("Failed to set output mode for '" +parameters.getProperty(SystemKeys.OUTPUT_FORMAT)+ "'. Using braille mode.");
		}

		String basePath = CONFIG_PATH + outputMode + "/";

		if (inputformat!=null) {
			try {
				return readConfiguration(localLocator, basePath + inputformat, parameters);
			} catch (ResourceLocatorException e) {
				logger.fine("Cannot find localized URL " + basePath + inputformat);
			}
		}
		try {
			return readConfiguration(localLocator, basePath + xmlformat, parameters);
		} catch (ResourceLocatorException e) {
			logger.fine("Cannot find localized URL " + basePath + xmlformat);
		}
		if (inputformat!=null) {
			try {
				return readConfiguration(commonLocator, basePath + inputformat, parameters);
			} catch (ResourceLocatorException e) {
				logger.fine("Cannot find common URL " + basePath + inputformat);
			}
		}
		try {
			return readConfiguration(commonLocator, basePath + xmlformat, parameters);
		} catch (ResourceLocatorException e) {
			logger.fine("Cannot find common URL " + basePath + xmlformat);
		}
		throw new TaskSystemException("Unable to open a configuration stream for the format.");
	}
	
	private ArrayList<InternalTask> readConfiguration(ResourceLocator locator, String path, RunParameters parameters) throws TaskSystemException, ResourceLocatorException {
		URL t = locator.getResource(path);
		ArrayList<InternalTask> setup = new ArrayList<InternalTask>();
		try {
			InputStream propsStream = null;
			try {
				propsStream = t.openStream();
				logger.fine("Opening stream: " + t.getFile());
			} catch (IOException e) {
				logger.log(Level.FINE, "Cannot open stream: " + t.getFile(), e);
				throw new ResourceLocatorException("Cannot open stream");
			}
			if (propsStream != null) {
				Properties p = new Properties();
				p.loadFromXML(propsStream);
				propsStream.close();


				HashMap<String, Object> xsltProps = new HashMap<String, Object>();
				for (Object key : parameters.getKeys()) {
					xsltProps.put(key.toString(), parameters.getProperty(key));
				}
				for (Object key : p.keySet()) {
					String[] schemas = p.get(key).toString().split("\\s*,\\s*");
					if ("validation".equals(key.toString())) {
						for (String s : schemas) {
							if (s!=null && s!="") {
								setup.add(new ValidatorTask("Conformance checker: " + s, locator.getResource(s)));
							}
						}
					} else if ("transformation".equals(key.toString())) {
						for (String s : schemas) {
							if (s!=null && s!="") {
								setup.add(new XsltTask("Input to OBFL converter: " + s, locator.getResource(s), null, xsltProps));
							}
						}
					} else {
						logger.info("Unrecognized key: " + key);
					}
				}
			} else {
				throw new TaskSystemException("Unable to open a configuration stream for the format.");
			}

		} catch (InvalidPropertiesFormatException e) {
			throw new TaskSystemException("Unable to read settings file.", e);
		} catch (IOException e) {
			throw new TaskSystemException("Unable to open settings file.", e);
		}
		return setup;
	}

}
