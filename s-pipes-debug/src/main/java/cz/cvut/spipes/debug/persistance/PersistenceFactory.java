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

import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import cz.cvut.kbss.jopa.Persistence;
import cz.cvut.kbss.jopa.model.EntityManagerFactory;
import cz.cvut.kbss.jopa.model.JOPAPersistenceProperties;
import cz.cvut.kbss.jopa.model.JOPAPersistenceProvider;
import cz.cvut.spipes.debug.config.PropertyResolver;

/**
 * Provides entity manager factory as a Spring bean.
 */
@Configuration
public class PersistenceFactory {

    private static final String REPOSITORY_NAME_PROPERTY = "repositoryName";

    private static final String STORAGE_URL = "storageUrl";
    private static final String DRIVER_PROPERTY = "driver";

    public static final Map<String, String> PARAMS = initParams();

    private final PropertyResolver propertyResolver;

    private EntityManagerFactory emf;

    @Autowired
    public PersistenceFactory(PropertyResolver propertyResolver) {
        this.propertyResolver = propertyResolver;
    }

    @Bean
    public EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

    @PostConstruct
    private void init() {
        String repositoryUrl = buildRepositoryUrl(propertyResolver.getProperty(REPOSITORY_NAME_PROPERTY));
        init(repositoryUrl);
    }

    public void reloadEmf(String repositoryName) {
        String repositoryUrl = buildRepositoryUrl(repositoryName);
        init(repositoryUrl);
    }

    @PreDestroy
    private void close() {
        if (emf.isOpen()) {
            emf.close();
        }
    }

    private void init(String repositoryUrl) {
        final Map<String, String> properties = new HashMap<>(PARAMS);
        properties.put(JOPAPersistenceProperties.ONTOLOGY_PHYSICAL_URI_KEY, repositoryUrl);
        properties.put(JOPAPersistenceProperties.DATA_SOURCE_CLASS, propertyResolver.getProperty(DRIVER_PROPERTY));
        this.emf = Persistence.createEntityManagerFactory("debug", properties);
    }

    private String buildRepositoryUrl(String repositoryName) {
        return propertyResolver.getProperty(STORAGE_URL) + "/" + repositoryName;
    }

    private static Map<String, String> initParams() {
        final Map<String, String> map = new HashMap<>();
        map.put(JOPAPersistenceProperties.SCAN_PACKAGE, "cz.cvut.spipes.model");
        map.put(JOPAPersistenceProperties.JPA_PERSISTENCE_PROVIDER, JOPAPersistenceProvider.class.getName());
        return map;
    }

    public EntityManagerFactory getEmf() {
        return this.emf;
    }
}
