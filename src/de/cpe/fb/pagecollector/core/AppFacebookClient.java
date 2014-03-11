/**
 * 
 */
package de.cpe.fb.pagecollector.core;

import com.restfb.DefaultFacebookClient;

/**
 * @author Christoph
 * 
 *         Making use of RestFB http://restfb.com/
 */
public class AppFacebookClient extends DefaultFacebookClient {

	public AppFacebookClient(String appId, String appSecret) {
		AccessToken accessToken = this.obtainAppAccessToken(appId, appSecret);
		this.accessToken = accessToken.getAccessToken();
	}
}
