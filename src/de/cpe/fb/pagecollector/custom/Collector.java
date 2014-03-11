package de.cpe.fb.pagecollector.custom;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.restfb.Connection;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.types.Comment;
import com.restfb.types.Page;
import com.restfb.types.Post;
import com.restfb.types.User;

import de.cpe.fb.pagecollector.core.AppFacebookClient;
import de.cpe.fb.pagecollector.core.Config;
import de.cpe.fb.pagecollector.core.FBFact;
import de.cpe.fb.pagecollector.core.ICollector;
import de.cpe.fb.pagecollector.core.IFBWriter;

/**
 * 
 * Making use of RestFB http://restfb.com/
 * */
public class Collector implements ICollector {

	// Logging
	private static final Logger	logger			= LoggerFactory.getLogger(de.cpe.fb.pagecollector.custom.Collector.class);

	// Authentication
	private static final String	APP_ID			= Config.getString("access.appId");																				//$NON-NLS-1$
	private static final String	APP_SECRET	= Config.getString("access.appSecret");																		//$NON-NLS-1$

	// Parameters
	/** Fetching is limited by FB to 1000 objects at once */
	private static final int		LIMIT				= 1000;

	/**
	 * PROGRAM VARIABLES
	 *********************************************************************************************/
	// FB
	private FacebookClient			fbClient;
	private String							mainPage;
	private Page								fbPage;
	private String							fbPageUser;

	// Time range
	private long								unixStart;
	private long								unixEnd;

	// Relevance
	private List<String>				keywords;

	// Output
	private List<FBFact>				factTable;

	public Collector(String page, String filterFile) {
		// load keywords for relevance filter
		keywords = new ArrayList<String>();
		readKeywords(filterFile);

		factTable = new ArrayList<FBFact>();

		// setup facebook connection
		fbClient = new AppFacebookClient(APP_ID, APP_SECRET);

		// general page info
		mainPage = page;
		fbPage = loadPage();

	}

	/*
	 * (non-Javadoc)
	 * @see de.cpe.fb.pagecollector.core.ICollector#run(long)
	 */
	@Override
	public void run(long start) {

		setTimeRange(start);

		logger.info("\n{}", long2String(start));

		loadPosts();

	}

	/*
	 * (non-Javadoc)
	 * @see de.cpe.fb.pagecollector.core.ICollector#writeResults(java.lang.String, de.cpe.fb.pagecollector.core.IFBWriter)
	 */
	@Override
	public void writeResults(String filename, IFBWriter writer) {
		writer.writeFile(filename, factTable);
	}

	/*
	 * (non-Javadoc)
	 * @see de.cpe.fb.pagecollector.core.ICollector#writeStats(int)
	 */
	@Override
	public void writeStats(int relevance) {

		int userPosts = 0;
		int userComments = 0;
		int userReplies = 0;

		int companyPosts = 0;
		int companyComments = 0;
		int companyReplies = 0;

		for (FBFact fact : factTable) {

			if ((relevance == 1 && !fact.isRelevant()) || (relevance == 2 && !fact.isIndirectRelevant())) {
				continue;
			}

			if (fact.getType() == FBFact.Type.POST) { //$NON-NLS-1$

				if (fact.getUser().compareTo(fbPageUser) == 0) {
					companyPosts++;
				} else {
					userPosts++;
				}

				continue;
			}

			if (fact.getType() == FBFact.Type.COMMENT) { //$NON-NLS-1$

				if (fact.getUser().compareTo(fbPageUser) == 0) {
					companyComments++;
				} else {
					userComments++;
				}

				continue;
			}

			if (fact.getType() == FBFact.Type.REPLY) { //$NON-NLS-1$
				if (fact.getUser().compareTo(fbPageUser) == 0) {
					companyReplies++;
				} else {
					userReplies++;
				}

				continue;
			}

		}

		int postSum = companyPosts + userPosts;
		int commentSum = companyComments + userComments;
		int replySum = companyReplies + userReplies;

		int companySum = companyPosts + companyComments + companyReplies;
		int userSum = userPosts + userComments + userReplies;
		int sumSum = postSum + commentSum + replySum;

		logger.info("Statistics of {}\n", (relevance == 0 ? "all" : (relevance == 1 ? "relevant" : "indirect relevant")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				+ " inputs"); //$NON-NLS-1$
		logger.info("Type \t\t Company \t User \t\t together \n"); //$NON-NLS-1$

		logger.info("Posts: 			{} \t\t {} \t\t {} \n", companyPosts, userPosts, postSum); //$NON-NLS-1$
		logger.info("Comments:		{} \t\t {} \t\t {} \n", companyComments, userComments, commentSum); //$NON-NLS-1$
		logger.info("Replies:		{} \t\t {} \t\t {} \n", companyReplies, userReplies, replySum); //$NON-NLS-1$

		logger.info("\nSums			{} \t\t {} \t\t {} \n\n", companySum, userSum, sumSum); //$NON-NLS-1$

	}

	/*
	 * (non-Javadoc)
	 * @see de.cpe.fb.pagecollector.core.ICollector#updateIndirectRelevance()
	 */
	@Override
	public void updateIndirectRelevance() {

		boolean someRelevant = false;
		int startInterval = 0;

		for (int i = 0; i < factTable.size(); i++) {
			FBFact fact = factTable.get(i);

			/**
			 * If the current entry is a post, we have to check the previous entries From the last post up to the the currents
			 * post predecessor.
			 */
			if (fact.getType() == FBFact.Type.POST) { //$NON-NLS-1$

				// check subinterval
				if (someRelevant) {
					for (int j = startInterval; j < i; j++) {
						factTable.get(j).setIndirectRelevant(true);
					}
				}

				// Initialization for the next bucket of entries
				startInterval = i;
				someRelevant = false;
			}

			// update the relevance flag for the current bucket
			someRelevant = someRelevant | fact.isRelevant();

		}
	}

	protected void loadPosts() {
		Connection<Post> posts = getPosts(mainPage);
		int totalPostCount = posts.getData().size();

		/**
		 * Iterate over all posts
		 * */
		for (int p = 0; p < totalPostCount; p++) {

			Post post = posts.getData().get(p);

			// store the fields of interest into the local table
			storeFact(post);

			// short console output as user feedback
			logger.debug("\np");

			// now collect the comments of that post
			loadComments(post);

		}
	}

	/**
	 * @param post
	 */
	protected void loadComments(Post post) {
		Connection<Comment> comments = getComments(post.getId());
		int currentTotalComments = comments.getData().size();
		for (int c = 0; c < currentTotalComments; c++) {

			Comment comment = comments.getData().get(c);

			// store the fields of interest into the local table
			storeFact(comment);

			// short console output as user feedback
			logger.debug("c"); //$NON-NLS-1$

			loadReplies(comment);

		}
	}

	/**
	 * @param comment
	 * @return
	 */
	protected void loadReplies(Comment comment) {
		Connection<Comment> replies = getComments(comment.getId());

		if (replies == null) {
			return;
		}

		int currentTotalReplies = replies.getData().size();

		for (int r = 0; r < currentTotalReplies; r++) {
			Comment reply = replies.getData().get(r);

			// short console output as user feedback
			logger.debug("r"); //$NON-NLS-1$

			storeReply(reply);
		}
	}

	/**
	 * @param post
	 */
	protected void storeFact(Post post) {

		// ignore already deleted content
		if (post.getMessage() == null) {
			return;
		}

		FBFact fact = new FBFact(FBFact.Type.POST); //$NON-NLS-1$
		fact.setDate(fbDate2String(post.getCreatedTime()));
		fact.setMessage(post.getMessage());
		fact.setUser(post.getFrom().getName());
		fact.setRelevant(isRelevant(post.getMessage()));

		fact.setAttachedLink(post.getLink());

		String url = post.getPicture();
		if (url != null) {
			if (url.startsWith("http://external")) { //$NON-NLS-1$
				int idx = url.indexOf("http", 1); //$NON-NLS-1$

				if (idx > 0)
					url = url.substring(idx);
			}

			url = url.replace("%3A", ":"); //$NON-NLS-1$ //$NON-NLS-2$
			url = url.replace("%2F", "/"); //$NON-NLS-1$ //$NON-NLS-2$

			fact.setPictureLink(url);

		}

		this.factTable.add(fact);
	}

	/**
	 * @param comment
	 */
	protected void storeFact(Comment comment) {

		// ignore already deleted content
		if (comment.getMessage() == null) {
			return;
		}

		FBFact fact = new FBFact(FBFact.Type.COMMENT); //$NON-NLS-1$
		fact.setDate(fbDate2String(comment.getCreatedTime()));
		fact.setMessage(comment.getMessage());
		fact.setUser(comment.getFrom().getName());
		fact.setRelevant(isRelevant(comment.getMessage()));

		this.factTable.add(fact);
	}

	/**
	 * @param comment
	 */
	protected void storeReply(Comment comment) {

		// ignore already deleted content
		if (comment.getMessage() == null) {
			return;
		}

		FBFact fact = new FBFact(FBFact.Type.REPLY); //$NON-NLS-1$
		fact.setDate(fbDate2String(comment.getCreatedTime()));
		fact.setMessage(comment.getMessage());
		fact.setUser(comment.getFrom().getName());
		fact.setRelevant(isRelevant(comment.getMessage()));
		this.factTable.add(fact);
	}

	protected void setTimeRange(long start) {
		unixStart = start;
		unixEnd = start + new Long(24 * 60 * 60);
	}

	protected Page loadPage() {
		fbPage = fbClient.fetchObject(mainPage, Page.class, Parameter.with("metadata", 1)); //$NON-NLS-1$

		fbPageUser = fbClient.fetchObject(fbPage.getUsername(), User.class).getName();

		logger.info("Page: " + fbPage.getName());
		logger.info("\t link: {} \n", fbPage.getLink());
		logger.info("\t likes: {} \n", fbPage.getLikes());
		logger.info("\t user name: {} \n", fbPageUser);
		logger.info("\n{} \n\n", fbPage.getDescription());

		return fbPage;
	}

	private Connection<Post> getPosts(String page) {
		return fbClient.fetchConnection(page + "/feed", Post.class, Parameter.with("limit", LIMIT), //$NON-NLS-1$ //$NON-NLS-2$
				Parameter.with("since", unixStart), Parameter.with("until", unixEnd)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private Connection<Comment> getComments(String postId) {
		Connection<Comment> comments = fbClient.fetchConnection(postId + "/comments", Comment.class, //$NON-NLS-1$
				Parameter.with("limit", LIMIT)); //$NON-NLS-1$
		return comments;
	}

	/**
	 * Read the given file line by line and add each line as keyword to the internal keyword list.
	 * 
	 * @param keywordFile
	 *          complete path and filename
	 * */
	protected void readKeywords(String keywordFile) {

		File f = new File(keywordFile);
		if (!f.exists()) {
			return;
		}

		FileInputStream fis;
		try {
			fis = new FileInputStream(f);

			InputStreamReader isReader = new InputStreamReader(fis, "utf-8"); //$NON-NLS-1$
			BufferedReader reader = new BufferedReader(isReader);

			String line;
			while ((line = reader.readLine()) != null) {
				keywords.add(line);
			}

			reader.close();

		} catch (IOException e) {
			logger.error("Could not read keyword file: {}\n");
		}
	}

	protected boolean isRelevant(String message) {

		for (String keyword : keywords) {
			if (message.contains(keyword)) {
				return true;
			}
		}

		return false;
	}

	protected String long2String(long time) {
		return new java.text.SimpleDateFormat("dd.MM.yyyy").format(new Date(time * 1000)); //$NON-NLS-1$
	}

	protected String fbDate2String(Date time) {
		return new java.text.SimpleDateFormat("dd.MM.yyyy [HH:mm]").format(time); //$NON-NLS-1$
	}
}
