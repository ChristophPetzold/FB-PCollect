/**
 * 
 */
package de.cpe.fb.pagecollector.custom;

import java.text.ParseException;

import de.cpe.fb.pagecollector.core.Config;
import de.cpe.fb.pagecollector.core.ICollector;

/**
 * @author Christoph
 * 
 */
public class Run {

	private static final String	PAGE								= Config.getString("config.run.input.page");				//$NON-NLS-1$
	private static final String	KEYWORD_FILE				= Config.getString("config.run.input.keywordFile"); //$NON-NLS-1$

	private static final String	START_DATE					= Config.getString("config.run.input.date.start");	//$NON-NLS-1$
	private static final String	END_DATE						= Config.getString("config.run.input.date.end");		//$NON-NLS-1$

	private static final String	OUTPUT_FILE_PATTERN	= Config.getString("config.run.output.file");			//$NON-NLS-1$

	private static final String	DAY_START_PATTERN		= Config.getString("config.run.dayStartFormat");		//$NON-NLS-1$
	private static final String	DAY_END_PATTERN			= Config.getString("config.run.dayEndFormat");			//$NON-NLS-1$
	private static final String	TIMESTAMP_PATTERN		= Config.getString("config.timeStampFormat");			//$NON-NLS-1$

	/**
	 * @param args
	 * @throws ParseException
	 */
	public static void main(String[] args) throws ParseException {
		ICollector c = new Collector(PAGE, KEYWORD_FILE);

		long start = getEpochEnd(START_DATE);
		long end = getEpochBegin(END_DATE);

		long runtime = System.currentTimeMillis();

		for (long d = end; d >= start; d -= (long) (24 * 60 * 60)) {
			c.run(d);
		}

		c.updateIndirectRelevance();

		String xmlFile = String.format(OUTPUT_FILE_PATTERN, PAGE);
		c.writeResults(xmlFile, new XmlFbWriter());

		// all inputs
		c.writeStats(0);

		// only indirect relevant inputs
		c.writeStats(1);

		// only relevant inputs
		c.writeStats(2);

		runtime = System.currentTimeMillis() - runtime;
		float minutesRT = ((float) runtime) / (1000 * 60);
		System.out.printf("\n\n\n\tCrawling took %.2f minutes", minutesRT);

	}

	/**
	 * http://www.epochconverter.com/
	 * */
	private static long getEpochBegin(String date) throws ParseException {
		String time = String.format(DAY_START_PATTERN, date);
		long epoch = new java.text.SimpleDateFormat(TIMESTAMP_PATTERN).parse(time).getTime() / 1000;
		return epoch;
	}

	/**
	 * http://www.epochconverter.com/
	 * */
	private static long getEpochEnd(String date) throws ParseException {
		String time = String.format(DAY_END_PATTERN, date);
		long epoch = new java.text.SimpleDateFormat(TIMESTAMP_PATTERN).parse(time).getTime() / 1000;
		return epoch;
	}

}
