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

import java.util.HashMap;
import java.util.Map;

import org.openengsb.core.api.ServiceInstanceFactory;
import org.openengsb.core.api.descriptor.ServiceDescriptor;
import org.openengsb.core.api.descriptor.ServiceDescriptor.Builder;
import org.openengsb.core.api.validation.MultipleAttributeValidationResult;
import org.openengsb.core.api.validation.MultipleAttributeValidationResultImpl;
import org.openengsb.domain.issue.IssueDomain;

public class GithubServiceInstanceFactory implements ServiceInstanceFactory<IssueDomain, GithubService> {

    @Override
    public ServiceDescriptor getDescriptor(Builder builder) {
        builder.name("service.name").description("service.description");

        builder.attribute(builder.newAttribute().id("github.user").name("github.user.name")
                .description("github.user.description").build());
        builder.attribute(builder.newAttribute().id("github.password").name("github.password.name")
                .description("github.password.description").defaultValue("").asPassword().build());
        builder.attribute(builder.newAttribute().id("github.repository").name("github.repository.name")
                .description("github.repository.description").defaultValue("").required().build());
        builder.attribute(builder.newAttribute().id("github.repositoryOwner").name("github.repositoryOwner.name")
                .description("github.repositoryOwner.description").defaultValue("").required().build());

        return builder.build();
    }

    @Override
    public void updateServiceInstance(GithubService instance, Map<String, String> attributes) {
        instance.setGithubUser(attributes.get("github.user"));
        instance.setGithubPassword(attributes.get("github.password"));

        instance.setRepository(attributes.get("github.repository"));
        instance.setRepositoryOwner(attributes.get("github.repositoryOwner"));
    }

    @Override
    public MultipleAttributeValidationResult updateValidation(GithubService instance, Map<String, String> attributes) {
        return new MultipleAttributeValidationResultImpl(true, new HashMap<String, String>());
    }

    @Override
    public GithubService createServiceInstance(String id, Map<String, String> attributes) {
        GithubService githubConnector = new GithubService(id, attributes.get("github.repository"),
                attributes.get("github.repositoryOwner"));
        githubConnector.setGithubUser(attributes.get("github.user"));
        githubConnector.setGithubPassword(attributes.get("github.password"));
        updateServiceInstance(githubConnector, attributes);
        return githubConnector;
    }

    @Override
    public MultipleAttributeValidationResult createValidation(String id, Map<String, String> attributes) {
        return new MultipleAttributeValidationResultImpl(true, new HashMap<String, String>());
    }

}
