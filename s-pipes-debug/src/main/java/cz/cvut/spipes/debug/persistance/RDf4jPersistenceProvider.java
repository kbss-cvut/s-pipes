/**
 * Copyright (C) 2019 Czech Technical University in Prague
 * <p>
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package cz.cvut.spipes.debug.persistance;

import javax.annotation.PostConstruct;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.manager.RepositoryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import cz.cvut.kbss.jopa.model.EntityManagerFactory;
import cz.cvut.spipes.debug.config.PropertyResolver;

@Configuration
public class RDf4jPersistenceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(RDf4jPersistenceProvider.class);
    private static final String STORAGE_URL_PROPERTY = "storageUrl";
    private static final String DEFAULT_REPOSITORY_NAME = "repositoryName";

    private final PropertyResolver propertyResolver;

    private final EntityManagerFactory emf;

    private Repository repository;

    @Autowired
    public RDf4jPersistenceProvider(PropertyResolver propertyResolver, EntityManagerFactory emf) {
        this.propertyResolver = propertyResolver;
        this.emf = emf;
    }

    @Bean
    public Repository repository() {
        return repository;
    }

    @PostConstruct
    private void initializeStorage() {
        final String repoUrl = buildRepoUrl(propertyResolver.getProperty(DEFAULT_REPOSITORY_NAME));
        initializeStorage(repoUrl);
    }

    public void changeRepository(String repositoryName) {
        String url = buildRepoUrl(repositoryName);
        initializeStorage(url);
    }

    public void initializeStorage(String repositoryUrl) {
        try {
            this.repository = RepositoryProvider.getRepository(repositoryUrl);
            assert repository.isInitialized();
        } catch (RepositoryException | RepositoryConfigException e) {
            LOG.error("Unable to connect to RDF4J repository at " + repositoryUrl, e);
        }
    }

    private String buildRepoUrl(String repoName) {
        return propertyResolver.getProperty(STORAGE_URL_PROPERTY) + "/" + repoName;
    }
}
