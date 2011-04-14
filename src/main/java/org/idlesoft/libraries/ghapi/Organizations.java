/**
 * ghapi
 * A Java wrapper for the GitHub API
 * 
 * Copyright (c) 2010 Idlesoft.
 * 
 * Licensed under the New BSD License.
 */

package org.idlesoft.libraries.ghapi;

import java.security.InvalidParameterException;

public class Organizations extends APIAbstract {

    public Organizations(GitHubAPI a) {
        super(a);
    }

    public Response info(String organization) {
        return httpGet("https://github.com/api/v2/json/organizations/" + encode(organization));
    }

    public Response update(String organization, String name, String email, String blog, String company,
            String location, String billingEmail) {
        String post = "";
        if (!name.equals("")) {
            post += "organization[name]=" + name;
        }
        if (!email.equals("")) {
            if (post.equals("")) {
                post += "&";
            }
            post += "organization[email]=" + email;
        }
        if (!blog.equals("")) {
            if (post.equals("")) {
                post += "&";
            }
            post += "organization[blog]=" + blog;
        }
        if (!company.equals("")) {
            if (post.equals("")) {
                post += "&";
            }
            post += "organization[company]=" + company;
        }
        if (!location.equals("")) {
            if (post.equals("")) {
                post += "&";
            }
            post += "organization[location]=" + location;
        }
        if (!billingEmail.equals("")) {
            if (post.equals("")) {
                post += "&";
            }
            post += "organization[billing_email]=" + billingEmail;
        }
        return httpPost("https://github.com/api/v2/json/organizations/" + encode(organization), encode(post));
    }

    public Response userOrganizations(String user) {
        return httpGet("https://github.com/api/v2/json/organizations/" + encode(user));
    }

    public Response myOrganizations() {
        return httpGet("https://github.com/api/v2/json/organizations");
    }

    public Response repositories() {
        return httpGet("https://github.com/api/v2/json/organizations/repositories");
    }

    public Response publicRepositories(String organization) {
        return httpGet("https://github.com/api/v2/json/organizations/" + encode(organization) + "/public_repositories");
    }

    public Response publicMembers(String organization) {
        return httpGet("https://github.com/api/v2/json/organizations/" + encode(organization) + "/public_members");
    }

    public Response listTeams(String organization) {
        return httpGet("https://github.com/api/v2/json/organizations/" + encode(organization) + "/teams");
    }

    public Response createTeam(String organization, String name, String permission, String... repoNames) {
        if (organization.equals("") || name.equals("") || permission.equals("") || repoNames.length == 0) {
            throw new InvalidParameterException("Missing information");
        }
        String post = "team[name]=" + encode(name) + "&team[permission]=" + encode(name);
        for (int i = 0; i < repoNames.length; i++) {
            post += "&team[repo_names][]=" + encode(repoNames[i]);
        }
        return httpPost("https://github.com/api/v2/json/organizations/" + encode(organization) + "/teams", post);
    }

    public Response teamInfo(int teamId) {
        return httpGet("https://github.com/api/v2/json/teams/" + teamId);
    }

    public Response teamMembers(int teamId) {
        return httpGet("https://github.com/api/v2/json/teams/" + teamId + "/members");
    }

    public Response teamRepositories(int teamId) {
        return httpGet("https://github.com/api/v2/json/teams/" + teamId + "/repositories");
    }
}