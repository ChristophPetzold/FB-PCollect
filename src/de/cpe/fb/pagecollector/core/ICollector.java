/**
 * 
 */
package de.cpe.fb.pagecollector.core;

/**
 * TODO add type description
 *
 * @author Christoph
 *
 */
public interface ICollector {

	/**
	 * Collect posts, comments and comment replies for the specified time.
	 * 
	 * Starts at start time until 24h ahead.
	 * 
	 * */
	public abstract void run(long start);

	public abstract void writeResults(String filename, IFBWriter writer);

	/**
	 * @param relevance
	 *          <ul>
	 *          <li>0: all</li>
	 *          <li>1: indirect (and direct) relevance</li>
	 *          <li>2: direct relevance</li>
	 *          </ul>
	 * */
	public abstract void writeStats(int relevance);

	/**
	 * The relevance of an post, comment or reply is determined by the existence of an keyword within its message. The
	 * indirect relevance indicates that at least one piece of an post->comment->reply chain is relevant.
	 * */
	public abstract void updateIndirectRelevance();

}
