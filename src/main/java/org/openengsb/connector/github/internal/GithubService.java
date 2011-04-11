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
import java.util.Vector;

import javax.xml.stream.events.Comment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.idlesoft.libraries.ghapi.GitHubAPI;
import org.idlesoft.libraries.ghapi.Issues;
import org.idlesoft.libraries.ghapi.APIAbstract.Response;
import org.idlesoft.libraries.ghapi.Repository;
import org.openengsb.core.api.AliveState;
import org.openengsb.core.common.AbstractOpenEngSBService;
import org.openengsb.domain.issue.IssueDomain;
import org.openengsb.domain.issue.models.Issue;
import org.openengsb.domain.issue.models.IssueAttribute;

import sun.misc.Regexp;
import sun.security.krb5.internal.UDPClient;


public class GithubService extends AbstractOpenEngSBService implements IssueDomain {

    private static Log log = LogFactory.getLog(GithubService.class);

    private AliveState state = AliveState.DISCONNECTED;
    private String githubUser;
    private String githubAuthToken;
    private String repository;
    private String repositoryOwner;

    private GitHubAPI ghapi = new GitHubAPI();
    
    public GithubService(String id, String repository, String repositoryOwner) {
        super(id);
        this.repository = repository;
        this.repositoryOwner = repositoryOwner;
    }

    @Override
    public AliveState getAliveState() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addComment(String issueNumber, String commentString) {
        ghapi.authenticate(githubUser, githubAuthToken);
        Issues service = new Issues(ghapi);
        service.add_comment(repositoryOwner, repository, Integer.valueOf(issueNumber), commentString);        
    }
    
    public Vector<GithubComment> getComments(int issueId) {
        Issues service = new Issues(ghapi);
        
        String[] v = service.list_comments(repositoryOwner, repository, issueId).resp.split("\"\\},\\{\"gravatar_id\":\"");
        
        v[0] = v[0].substring(29);
        Vector<GithubComment> listOfCommets = new Vector<GithubComment>();
        for(String i:v)
        {
            GithubComment c = new GithubComment();
            int index = i.indexOf("\",\"");
            c.setGravatarId(i.substring(0,index));
            i = i.substring(index+"\",\"created_at\":\"".length());
            index = i.indexOf("\",\"");
            c.setCreatedAt(i.substring(0,index));
            i = i.substring(index+"\",\"body\":\"".length());
            index = i.indexOf("\",\"");
            c.setBody(i.substring(0,index));
            i = i.substring(index+"\",\"updated_at\":\"".length());
            index = i.indexOf("\",\"");
            c.setUpdatedAt(i.substring(0,index));
            i = i.substring(index+"\",\"id\":\"".length());
            index = i.indexOf(",\""); //Number has no "
            c.setId(Integer.valueOf(i.substring(0,index)));
            i = i.substring(index+",\"user\":\"".length());
            c.setUser(i);
            listOfCommets.add(c);
        }
        
        return listOfCommets;
    }
    
    public Vector<GithubIssue> getIssues()
    {
        Issues service = new Issues(ghapi);
        System.out.println();
        
        String temp = service.list(repositoryOwner, repository, "open").resp;
        String[] v = temp.substring(0, temp.length() - 5).split("\"\\},\\{\"gravatar_id\":\"");       
        v[0] = v[0].substring(27);
        Vector<GithubIssue> listOfIssues = new Vector<GithubIssue>();

        for(String i:v)
        {
            GithubIssue c = new GithubIssue();
            int index = i.indexOf("\",\"");
            c.setGravatarId(i.substring(0,index));
            i = i.substring(index+"\",\"position\":".length());
            index = i.indexOf(",\"");
            c.setPosition(Double.valueOf(i.substring(0,index)));
            i = i.substring(index+",\"number\":".length());
            index = i.indexOf(",\"");
            c.setNumber(Integer.valueOf(i.substring(0,index)));
            i = i.substring(index+",\"votes\":".length());
            index = i.indexOf(",\"");
            c.setVotes(Integer.valueOf(i.substring(0,index)));
            i = i.substring(index+",\"created_at\":\"".length());
            index = i.indexOf("\",\"");
            c.setCreatedAt(i.substring(0,index));
            i = i.substring(index+"\",\"comments\":".length());
            index = i.indexOf(",\"");
            c.setComments(Integer.valueOf(i.substring(0,index)));
            i = i.substring(index+",\"body\":\"".length());
            index = i.indexOf("\",\"");
            c.setBody(i.substring(0,index));
            i = i.substring(index+"\",\"title\":\"".length());
            index = i.indexOf("\",\"");
            c.setTitle(i.substring(0,index));
            i = i.substring(index+"\",\"updated_at\":\"".length());
            index = i.indexOf("\",\"");
            c.setUpdatedAt(i.substring(0,index));
            i = i.substring(index+"\",\"html_url\":\"".length());
            index = i.indexOf("\",\"");
            c.setHtmlUrl(i.substring(0,index));
            i = i.substring(index+"\",\"user\":\"".length());
            index = i.indexOf("\",\"");
            c.setUser(i.substring(0,index));
            
            i = i.substring(index+"\",\"labels\":[".length());
            index = i.indexOf("],\"");

            Vector<String> labels = new Vector<String>();
            if(!i.startsWith("]")){ 
                String[] tmp = i.substring(0,index).replace("\"", "").split(",");
                for(String k: tmp){
                    labels.add(k);
                }
            }
            c.setLabels(labels);
            
            i = i.substring(index+"],\"state\":\"".length(), i.length());
            c.setState(i);
            
            listOfIssues.add(c);
        }
        
        return listOfIssues;

    }

    @Override
    public void closeRelease(String arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String createIssue(Issue engsbIssue) {
        ghapi.authenticate(githubUser, githubAuthToken);
        Issues service = new Issues(ghapi);
        service.open(repositoryOwner, repository, engsbIssue.getId(), engsbIssue.getDescription());
        return null;
    }

    @Override
    public ArrayList<String> generateReleaseReport(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void moveIssuesFromReleaseToRelease(String arg0, String arg1) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void updateIssue(String arg0, String arg1, HashMap<IssueAttribute, String> arg2) {
        // TODO Auto-generated method stub
        
    }


 /* 
    @Override
    public void updateIssue(String issueKey, String comment, HashMap<IssueAttribute, String> changes) {
        try {
            JiraSoapService jiraSoapService = login();

            RemoteFieldValue[] values = convertChanges(changes);
            jiraSoapService.updateIssue(authToken, issueKey, values);
        } catch (RemoteException e) {
            log.error("Error updating the issue . XMLRPC call failed. ");
            throw new DomainMethodExecutionException("RPC called failed", e);
        } finally {
            state = AliveState.DISCONNECTED;
        }
    }


    @Override
    public void moveIssuesFromReleaseToRelease(String releaseFromId, String releaseToId) {
        try {
            JiraSoapService jiraSoapService = login();

            RemoteVersion version = getNextVersion(authToken, jiraSoapService, releaseToId);

            RemoteIssue[] issues = jiraSoapService
                    .getIssuesFromJqlSearch(authToken, "fixVersion in (\"" + releaseFromId + "\") ", 1000);

            RemoteFieldValue[] changes = new RemoteFieldValue[1];
            RemoteFieldValue change = new RemoteFieldValue();
            change.setId("fixVersions");
            change.setValues(new String[]{version.getId()});

            changes[0] = change;
            for (RemoteIssue issue : issues) {
                jiraSoapService.updateIssue(authToken, issue.getKey(), changes);
            }
        } catch (RemoteException e) {
            log.error("Error updating the issue . XMLRPC call failed. ");
            throw new DomainMethodExecutionException("RPC called failed", e);
        } finally {
            state = AliveState.DISCONNECTED;
        }
    }

    @Override
    public void closeRelease(String id) {
        try {
            JiraSoapService jiraSoapService = login();

            RemoteVersion[] versions = jiraSoapService.getVersions(authToken, projectKey);
            RemoteVersion version = null;
            for (RemoteVersion ver : versions) {
                if (id.equals(ver.getName())) {
                    version = ver;
                }
            }
            if (version == null) {
                log.error("Release not found");
                return;
            }
            jiraSoapService.releaseVersion(authToken, projectKey, version);
        } catch (RemoteException e) {
            log.error("Error closing release, Remote exception ");
            throw new DomainMethodExecutionException("RPC called failed", e);
        } finally {
            state = AliveState.DISCONNECTED;
        }
    }

    @Override
    public ArrayList<String> generateReleaseReport(String releaseId) {

        ArrayList<String> report = new ArrayList<String>();
        Map<String, List<String>> reports = new HashMap<String, List<String>>();

        try {
            JiraSoapService jiraSoapService = login();

            RemoteIssue[] issues = jiraSoapService
                    .getIssuesFromJqlSearch(authToken, "fixVersion in (\"" + releaseId + "\") and status in (6)",
                            1000);
            for (RemoteIssue issue : issues) {
                if ("6".equals(issue.getStatus())) {
                    List<String> issueList = new ArrayList<String>();
                    if (reports.containsKey(issue.getType())) {
                        issueList = reports.get(issue.getType());
                    }
                    issueList.add("\t * [" + issue.getKey() + "] - " + issue.getDescription());
                    reports.put(TypeConverter.fromCode(issue.getType()), issueList);
                }
            }
            for (String key : reports.keySet()) {
                report.add("** " + key + "\n");
                report.addAll(reports.get(key));
                report.add("\n");
            }

        } catch (RemoteException e) {
            log.error("Error generating release report. XMLRPC call failed. ");
            throw new DomainMethodExecutionException("RPC called failed ", e);
        } finally {
            state = AliveState.DISCONNECTED;
        }
        for (String s : report) {
            log.info(s);
        }
        return report;
    }

    private RemoteVersion getNextVersion(String authToken, JiraSoapService jiraSoapService, String releaseToId)
        throws RemoteException {
        RemoteVersion[] versions = jiraSoapService.getVersions(authToken, projectKey);
        RemoteVersion next = null;
        for (RemoteVersion version : versions) {
            if (releaseToId.equals(version.getId())) {
                next = version;
            }
        }
        return next;
    }

    @Override
    public AliveState getAliveState() {
        return state;
    }

    private RemoteFieldValue[] convertChanges(HashMap<IssueAttribute, String> changes) {
        Set<IssueAttribute> changedAttributes = new HashSet<IssueAttribute>(changes.keySet());
        ArrayList<RemoteFieldValue> remoteFields = new ArrayList<RemoteFieldValue>();

        for (IssueAttribute attribute : changedAttributes) {
            String targetField = FieldConverter.fromIssueField((Issue.Field) attribute);

            String targetValue = JiraValueConverter.convert(changes.get(attribute));
            if (targetField != null && targetValue != null) {
                RemoteFieldValue rfv = new RemoteFieldValue();
                rfv.setId(targetField);
                rfv.setValues(new String[]{targetValue});
                remoteFields.add(rfv);
            }
        }
        RemoteFieldValue[] remoteFieldArray = new RemoteFieldValue[remoteFields.size()];
        remoteFields.toArray(remoteFieldArray);
        return remoteFieldArray;
    }


    private RemoteIssue convertIssue(Issue engsbIssue) {
        RemoteIssue remoteIssue = new RemoteIssue();
        remoteIssue.setSummary(engsbIssue.getSummary());
        remoteIssue.setDescription(engsbIssue.getDescription());
        remoteIssue.setReporter(engsbIssue.getReporter());
        remoteIssue.setAssignee(engsbIssue.getOwner());
        remoteIssue.setProject(projectKey);

        remoteIssue.setPriority(PriorityConverter.fromIssuePriority(engsbIssue.getPriority()));
        remoteIssue.setStatus(StatusConverter.fromIssueStatus(engsbIssue.getStatus()));
        remoteIssue.setType(TypeConverter.fromIssueType(engsbIssue.getType()));

        RemoteVersion version = new RemoteVersion();
        version.setId(engsbIssue.getDueVersion());
        RemoteVersion[] remoteVersions = new RemoteVersion[]{version};
        remoteIssue.setFixVersions(remoteVersions);

        return remoteIssue;
    }

    private JiraSoapService login() {
        try {
            state = AliveState.CONNECTING;
            jiraSoapSession.connect(jiraUser, jiraPassword);
            state = AliveState.ONLINE;
            authToken = jiraSoapSession.getAuthenticationToken();
            return jiraSoapSession.getJiraSoapService();
        } catch (RemoteException e) {
            throw new DomainMethodExecutionException("Could not connect to server, maybe wrong user password/username"
                    , e);
        }
    }*/

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

    public String getGithubAuthToken() {
        return githubAuthToken;
    }

    public void setGithubAuthToken(String githubAuthToken) {
        this.githubAuthToken = githubAuthToken;
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
