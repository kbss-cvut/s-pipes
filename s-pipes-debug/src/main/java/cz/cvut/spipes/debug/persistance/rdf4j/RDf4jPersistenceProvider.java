/**
 * Copyright (C) 2019 Czech Technical University in Prague
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cvut.spipes.debug.persistance.rdf4j;

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

@Configuration
@PropertySource("classpath:config.properties")
public class RDf4jPersistenceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(RDf4jPersistenceProvider.class);
    private static final String URL_PROPERTY = "repositoryUrl";

    private final Environment environment;

    private final EntityManagerFactory emf;

    private Repository repository;
    @Autowired
    public RDf4jPersistenceProvider(Environment environment, EntityManagerFactory emf) {
        this.environment = environment;
        this.emf = emf;
    }

    @Bean
    public Repository repository() {
        return repository;
    }

    @PostConstruct
    private void initializeStorage() {
        final String repoUrl = environment.getRequiredProperty(URL_PROPERTY);
        try {
            this.repository = RepositoryProvider.getRepository(repoUrl);
            assert repository.isInitialized();
        } catch (RepositoryException | RepositoryConfigException e) {
            LOG.error("Unable to connect to RDF4J repository at " + repoUrl, e);
        }
    }
}
