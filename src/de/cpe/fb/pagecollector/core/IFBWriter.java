package de.cpe.fb.pagecollector.core;

import java.util.Collection;

import javax.xml.stream.FactoryConfigurationError;

public interface IFBWriter {

	/**
	 * @param start
	 * @throws FactoryConfigurationError
	 */
	public abstract void writeFile(String filename, Collection<FBFact> factTable);

}
