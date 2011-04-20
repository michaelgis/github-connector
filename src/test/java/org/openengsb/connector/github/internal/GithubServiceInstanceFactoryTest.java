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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 *
 */
public class GithubServiceInstanceFactoryTest {

    @Test
    public void testUpdateServiceInstance() throws Exception {
        GithubServiceInstanceFactory ghif = new GithubServiceInstanceFactory();
        Map<String, String> attributes = new HashMap<String, String>();
        GithubService service = (GithubService) ghif.createNewInstance("id");
        ghif.applyAttributes(service, attributes);
        assertThat(service.getInstanceId(), is("id"));
    }

    @Test
    public void testUpdateValidation() throws Exception {
        GithubServiceInstanceFactory ghif = new GithubServiceInstanceFactory();
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("github.user", "user");
        attributes.put("github.password", "pwd");
        attributes.put("github.repository", "testRepo");
        attributes.put("github.repositoryOwner", "testOwner");
        
        GithubService githubService = new GithubService("id"); 
        ghif.applyAttributes(githubService, attributes); 

        assertThat(githubService.getGithubPassword(), is("pwd"));
        assertThat(githubService.getGithubUser(), is("user"));
        assertThat(githubService.getRepository(), is("testRepo"));
        assertThat(githubService.getRepositoryOwner(), is("testOwner"));
    }
}
