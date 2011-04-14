/**
 * ghapi
 * A Java wrapper for the GitHub API
 * 
 * Copyright (c) 2010 Idlesoft.
 * 
 * Licensed under the New BSD License.
 */

package org.idlesoft.libraries.ghapi;

public class Gists extends APIAbstract {

    public Gists(GitHubAPI a) {
        super(a);
    }

    /**
     * Get a Gist's Metadata
     * 
     * @param gistId
     * @return
     */
    public Response get_metadata(String gistId) {
        return httpGet("https://gist.github.com/api/v1/json/" + encode(gistId));
    }

    /**
     * Get a Gist's Content
     * 
     * @param gistId
     * @param filename
     * @return
     */
    public Response get_content(String gistId, String filename) {
        return httpGet("https://gist.github.com/raw/" + encode(gistId) + "/" + encode(filename));
    }

    /**
     * List a User's Public Gists
     * 
     * @param username
     * @return
     */
    public Response list_gists(String username) {
        return httpGet("https://gist.github.com/api/v1/json/gists/" + encode(username));
    }
}