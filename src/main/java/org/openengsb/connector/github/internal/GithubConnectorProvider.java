package org.openengsb.connector.github.internal;

import org.openengsb.core.api.descriptor.ServiceDescriptor;
import org.openengsb.core.api.descriptor.ServiceDescriptor.Builder;
import org.openengsb.core.common.AbstractConnectorProvider;

public class GithubConnectorProvider extends AbstractConnectorProvider {

    @Override
    public ServiceDescriptor getDescriptor() {
        Builder builder = ServiceDescriptor.builder(strings);
        builder.id(this.id);
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

}
