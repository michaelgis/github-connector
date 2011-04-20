/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.connector.github.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.idlesoft.libraries.ghapi.GitHubAPI;
import org.idlesoft.libraries.ghapi.Issues;
import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.DomainMethodNotImplementedException;
import org.openengsb.core.common.AbstractOpenEngSBService;
import org.openengsb.domain.issue.IssueDomain;
import org.openengsb.domain.issue.models.Issue;
import org.openengsb.domain.issue.models.Issue.Status;
import org.openengsb.domain.issue.models.IssueAttribute;


public class GithubService extends AbstractOpenEngSBService implements IssueDomain {

    private static Log log = LogFactory.getLog(GithubService.class);

    private AliveState state = AliveState.DISCONNECTED;
    private String githubUser;
    private String githubPassword;
    private String repository;
    private String repositoryOwner;

    private GitHubAPI ghapi = new GitHubAPI();
    
    public GithubService(String id) {
        super(id);
    }

    @Override
    public AliveState getAliveState() {
        return state;
    }

    @Override
    public void addComment(String issueNumber, String commentString) {
        ghapi.authenticate(githubUser, githubPassword);
        Issues service = new Issues(ghapi);
        service.add_comment(repositoryOwner, repository, Integer.valueOf(issueNumber), commentString);        
    }
    
    public Vector<GithubComment> getComments(int issueId) {
        Issues service = new Issues(ghapi);
        
        String[] v = service.list_comments(repositoryOwner, repository, issueId).resp
                .split("\"\\},\\{\"gravatar_id\":\"");

        v[0] = v[0].substring(29);
        Vector<GithubComment> listOfCommets = new Vector<GithubComment>();
        for (String i : v) {
            String tmp = i;
            GithubComment c = new GithubComment();
            int index = tmp.indexOf("\",\"");
            c.setGravatarId(tmp.substring(0, index));
            tmp = tmp.substring(index + "\",\"created_at\":\"".length());
            index = tmp.indexOf("\",\"");
            c.setCreatedAt(tmp.substring(0, index));
            tmp = tmp.substring(index + "\",\"body\":\"".length());
            index = tmp.indexOf("\",\"");
            c.setBody(tmp.substring(0, index));
            tmp = tmp.substring(index + "\",\"updated_at\":\"".length());
            index = tmp.indexOf("\",\"");
            c.setUpdatedAt(tmp.substring(0, index));
            tmp = tmp.substring(index + "\",\"id\":\"".length());
            index = tmp.indexOf(",\""); // Number has no "
            c.setId(Integer.valueOf(tmp.substring(0, index)));
            tmp = tmp.substring(index + ",\"user\":\"".length());
            c.setUser(tmp);
            listOfCommets.add(c);
        }

        return listOfCommets;
    }

    public Vector<GithubIssue> getIssues() {
        Issues service = new Issues(ghapi);
        String temp = service.list(repositoryOwner, repository, "open").resp;
        Vector<GithubIssue> listOfIssues = processIssueResponse(temp);
        return listOfIssues;
    }

    private Vector<GithubIssue> processIssueResponse(String temp) {
        Vector<GithubIssue> listOfIssues = new Vector<GithubIssue>();
        String[] v = temp.split(",\\{\"gravatar_id\":\"");
        v[0] = v[0].substring(29);
        for (String i : v) {
            listOfIssues.add(processSingleIssueResponse("{\"gravatar_id\":\"" + i));
        }
        return listOfIssues;
    }
    
    private GithubIssue processSingleIssueResponse(String temp) {
        String tmp = temp.substring(temp.indexOf("{\"gravatar_id\":\"") + "{\"gravatar_id\":\"".length(),
                temp.lastIndexOf("\"}"));

        GithubIssue c = new GithubIssue();
        int index = tmp.indexOf("\",\"");
        c.setGravatarId(tmp.substring(0, index));
        tmp = tmp.substring(index + "\",\"position\":".length());
        index = tmp.indexOf(",\"");
        c.setPosition(Double.valueOf(tmp.substring(0, index)));
        tmp = tmp.substring(index + ",\"number\":".length());
        index = tmp.indexOf(",\"");
        c.setNumber(Integer.valueOf(tmp.substring(0, index)));
        tmp = tmp.substring(index + ",\"votes\":".length());
        index = tmp.indexOf(",\"");
        c.setVotes(Integer.valueOf(tmp.substring(0, index)));
        tmp = tmp.substring(index + ",\"created_at\":\"".length());
        index = tmp.indexOf("\",\"");
        c.setCreatedAt(tmp.substring(0, index));
        tmp = tmp.substring(index + "\",\"comments\":".length());
        index = tmp.indexOf(",\"");
        c.setComments(Integer.valueOf(tmp.substring(0, index)));
        tmp = tmp.substring(index + ",\"body\":\"".length());
        index = tmp.indexOf("\",\"");
        c.setBody(tmp.substring(0, index));
        tmp = tmp.substring(index + "\",\"title\":\"".length());
        index = tmp.indexOf("\",\"");
        c.setTitle(tmp.substring(0, index));
        tmp = tmp.substring(index + "\",\"updated_at\":\"".length());
        index = tmp.indexOf("\",\"");
        c.setUpdatedAt(tmp.substring(0, index));
        
        int tempIndex = temp.indexOf("\",\"closed_at\":\"");
        if (tempIndex > -1) {
            tmp = tmp.substring(index + "\",\"closed_at\":\"".length());
            index = tmp.indexOf("\",\"");
            c.setClosedAt(tmp.substring(0, index));
        }
        
        tmp = tmp.substring(index + "\",\"html_url\":\"".length());
        index = tmp.indexOf("\",\"");
        c.setHtmlUrl(tmp.substring(0, index));
        tmp = tmp.substring(index + "\",\"user\":\"".length());
        index = tmp.indexOf("\",\"");
        c.setUser(tmp.substring(0, index));

        tmp = tmp.substring(index + "\",\"labels\":[".length());
        index = tmp.indexOf("],\"");

        Vector<String> labels = new Vector<String>();
        if (!tmp.startsWith("]")) {
            String[] tempArray = tmp.substring(0, index).replace("\"", "").split(",");
            for (String k : tempArray) {
                labels.add(k);
            }
        }
        c.setLabels(labels);

        tmp = tmp.substring(index + "],\"state\":\"".length(), tmp.length());
        c.setState(tmp);

        return c;
    }
    
    private List<String> processLabels(String labels) {
        List<String> labelList = new ArrayList<String>();
        
        labels = labels.substring(labels.indexOf(":[\"") + ":[\"".length(), labels.length() - 4);
        String[] tmp = labels.split("\",\"");
        for (String temp : tmp) {
            labelList.add(temp);
        }
        
        return labelList;
    }

    @Override
    public void closeRelease(String arg0) {
        //Not available in ghapi
        throw new DomainMethodNotImplementedException("method not yet implemented");
    }

    @Override
    public String createIssue(Issue engsbIssue) {
        ghapi.authenticate(githubUser, githubPassword);
        Issues service = new Issues(ghapi);
        String tmp = service.open(repositoryOwner, repository, engsbIssue.getSummary(),
                engsbIssue.getDescription()).resp;
        if (tmp != null) {
            state = AliveState.ONLINE;
            return String.valueOf(processIssueResponse(tmp).get(0).getNumber());
        } else {
            state = AliveState.OFFLINE;
            return null;
        }
    }

    @Override
    public ArrayList<String> generateReleaseReport(String arg0) {
        //Not available in ghapi
        throw new DomainMethodNotImplementedException("method not yet implemented");
    }

    @Override
    public void moveIssuesFromReleaseToRelease(String arg0, String arg1) {
        //Not available in ghapi
        throw new DomainMethodNotImplementedException("method not yet implemented");
    }

    @Override
    public void updateIssue(String id, String comment, HashMap<IssueAttribute, String> changes) {
        ghapi.authenticate(githubUser, githubPassword);
        Issues service = new Issues(ghapi);
        if (comment != null && !comment.equals("")) {
            addComment(id, comment);
        }

        for (Map.Entry<IssueAttribute, String> entry : changes.entrySet()) {
            if (entry.getKey().equals(Issue.Field.STATUS)) {
                if (entry.getValue().toLowerCase().equals("open")) {
                    throw new DomainMethodNotImplementedException("reopen in ghapi does not work properly");
                    //service.reopen(repositoryOwner, repository, Integer.valueOf(id));
                } else if (entry.getValue().toLowerCase().equals("closed")) {
                    service.close(repositoryOwner, repository, Integer.valueOf(id));
                }
            } else if (entry.getKey().equals(Issue.Field.DESCRIPTION)) {
                Issue tmp = getIssue(id);
                service.edit(repositoryOwner, repository, Integer.valueOf(id),
                        tmp.getSummary(), entry.getValue().toString());
            } else if (entry.getKey().equals(Issue.Field.SUMMARY)) {
                Issue tmp = getIssue(id);
                service.edit(repositoryOwner, repository, Integer.valueOf(id), entry.getValue(),
                        tmp.getDescription());
            } else if (entry.getKey().equals(Issue.Field.COMPONENT)) {
                Issue tmp = getIssue(id);
                
                for (String i : tmp.getComponents()) {
                    try {
                        removeLabelFromIssue(i, Integer.valueOf(id));
                    } catch (NumberFormatException e) {
                        log.error(e);
                    } catch (Exception e) {
                        log.error(e);
                    }
                }
                
                for (String i : entry.getValue().split(",")) {
                    try {
                        addLabelToIssue(i, Integer.valueOf(id));
                    } catch (NumberFormatException e) {
                        log.error(e);
                    } catch (Exception e) {
                        log.error(e);
                    }
                }
            }
        }
    }
    
    @Override
    public void addComponent(String component) {
        ghapi.authenticate(githubUser, githubPassword);
        Issues service = new Issues(ghapi);
        
        service.add_label(repositoryOwner, repository, component);
    }

    @Override
    public void removeComponent(String component) {
        ghapi.authenticate(githubUser, githubPassword);
        Issues service = new Issues(ghapi);
        service.remove_label(repositoryOwner, repository, component);
    }
    
    public List<String> getLabels() {
        Issues service = new Issues(ghapi);
        return processLabels(service.labels(repositoryOwner, repository).resp);
    }
    
    public GithubIssue getGithubIssue(String id) {
        Issues service = new Issues(ghapi);
        String tmp = service.issue(repositoryOwner, repository, Integer.valueOf(id)).resp;
        return processSingleIssueResponse(tmp);
    }
    
    public Issue getIssue(String id) {
        return convertGithubIssue(getGithubIssue(id));
    }

    private Issue convertGithubIssue(GithubIssue issue) {
        Issue i = new Issue();
        i.setDescription(issue.getBody());
        i.setId(String.valueOf(issue.getNumber()));
        i.setOwner(issue.getUser());
        if (issue.getState().toLowerCase().equals("open")) {
            i.setStatus(Status.NEW);
        } else if (issue.getState().toLowerCase().equals("closed")) {
            i.setStatus(Status.CLOSED);
        }
        i.setSummary(issue.getTitle());
        i.setComponents(issue.getLabels());
        return i;
    }

    public void addLabelToIssue(String text, int issueId) throws Exception {
       
        ghapi.authenticate(githubUser, githubPassword);
        Issues service = new Issues(ghapi);
        String tmp = service.add_label_to_Issue(repositoryOwner, repository, text, issueId).resp;
        if (tmp == null || tmp.equals("{\"error\":\"not authorized\"}")) {
            log.error("Not Authorized Access!");
            throw new Exception("User not authorized!");
        }
    }

    public void removeLabelFromIssue(String text, int issueId) throws Exception {
        ghapi.authenticate(githubUser, githubPassword);
        Issues service = new Issues(ghapi);
        String tmp = service.remove_label_from_Issue(repositoryOwner, repository, text, issueId).resp;
        if (tmp == null || tmp.equals("{\"error\":\"not authorized\"}")) {
            log.error("Not Authorized Access!");
            throw new Exception("User not authorized!");
        }
    }
    
    public AliveState getState() {
        return state;
    }

    public void setState(AliveState state) {
        this.state = state;
    }

    public String getGithubUser() {
        return githubUser;
    }

    public void setGithubUser(String githubUser) {
        this.githubUser = githubUser;
    }

    public String getGithubPassword() {
        return githubPassword;
    }

    public void setGithubPassword(String githubPassword) {
        this.githubPassword = githubPassword;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String projectKey) {
        this.repository = projectKey;
    }

    public void setRepositoryOwner(String repositoryOwner) {
        this.repositoryOwner = repositoryOwner;
    }

    public String getRepositoryOwner() {
        return repositoryOwner;
    }
}
