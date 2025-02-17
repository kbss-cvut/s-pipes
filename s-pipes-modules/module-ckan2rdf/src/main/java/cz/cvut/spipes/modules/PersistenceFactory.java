package cz.cvut.spipes.modules;
/**
 * Copyright (C) 2016 Czech Technical University in Prague
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

import cz.cvut.kbss.jopa.Persistence;
import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.model.EntityManagerFactory;
import cz.cvut.kbss.jopa.model.JOPAPersistenceProperties;
import cz.cvut.kbss.jopa.model.JOPAPersistenceProvider;
import cz.cvut.kbss.ontodriver.config.OntoDriverProperties;
import cz.cvut.kbss.ontodriver.rdf4j.config.Rdf4jOntoDriverProperties;
import java.util.HashMap;
import java.util.Map;

public class PersistenceFactory {

    private static boolean initialized = false;

    private static EntityManagerFactory emf;

    private PersistenceFactory() {
        throw new AssertionError();
    }

    /**
     * Supplies basic configuration to the persistence manager factory.
     *
     * @param properties default properties - overriding any other set up in this method
     */
    public static void init(final Map<String, String> properties) {
        final Map<String, String> props = new HashMap<>();
        // Basic storage access properties - driver class, physical location of the storage
        props.put(JOPAPersistenceProperties.DATA_SOURCE_CLASS,
            "cz.cvut.kbss.ontodriver.sesame.SesameDataSource");
        // View transactional changes during transaction
        props.put(OntoDriverProperties.USE_TRANSACTIONAL_ONTOLOGY, Boolean.TRUE.toString());
        // Use in-memory storage if not remote or local file path specified
        props.put(Rdf4jOntoDriverProperties.USE_VOLATILE_STORAGE, Boolean.TRUE.toString());
        // Don't use Sesame inference
        props.put(Rdf4jOntoDriverProperties.USE_INFERENCE, Boolean.FALSE.toString());
        // Ontology language
        props.put(JOPAPersistenceProperties.LANG, "en");
        if (properties != null) {
            props.putAll(properties);
        }
        // Persistence provider name
        props.put(JOPAPersistenceProperties.JPA_PERSISTENCE_PROVIDER,
            JOPAPersistenceProvider.class.getName());

        emf = Persistence.createEntityManagerFactory("Jackan", props);
        initialized = true;
    }

    /**
     * Creates a new entity manager, provided the underlying EntityManagerFactory is initialized.
     *
     * @return a new EntityManager
     */
    public static EntityManager createEntityManager() {
        if (!initialized) {
            throw new IllegalStateException("Factory has not been initialized.");
        }
        return emf.createEntityManager();
    }

    public static void close() {
        emf.close();
    }
}