/**
 * TermIt Copyright (C) 2019 Czech Technical University in Prague
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program.  If not, see
 * <https://www.gnu.org/licenses/>.
 */
package cz.cvut.spipes.modules.constants;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Application-wide constants.
 */
public class Termit {

    protected static Resource resource(String local )
    { return ResourceFactory.createResource( getURI() + local ); }

    protected static Property property(String local )
    { return ResourceFactory.createProperty( getURI() + local ); }

    public static final String uri = "http://onto.fel.cvut.cz/ontologies/application/termit/pojem/";
    public static final String SCORE = "score";

    /* Stable TermIt terms */
    public static final String VYSKYT_TERMU = uri + "výskyt-termu";
    public static final Resource VYSKYT_TERMU_RESOURCE = resource("výskyt-termu");
    public static final Resource CIL_VYSKYTU = resource("cíl-výskytu");
    public static final Resource SELEKTOR_POZICI_V_TEXTU = resource("selektor-pozici-v-textu");
    public static final Resource SELEKTOR_TEXT_QUOTE = resource("selektor-text-quote");

    public static final Property MA_CIL = property("má-cíl");
    public static final Property JE_PRIRAZENIM_TERMU = property("je-přiřazením-termu");
    public static final Property MA_SELEKTOR = property("má-selektor");
    public static final Property MA_PREFIX_TEXT_QUOTE = property("má-prefix-text-quote");
    public static final Property MA_PRESNY_TEXT_QUOTE = property("má-přesný-text-quote");
    public static final Property MA_SUFFIX_TEXT_QUOTE = property("má-suffix-text-quote");
    public static final Property MA_KONCOVOU_POZICI = property("má-koncovou-pozici");
    public static final Property MA_STARTOVNI_POZICI = property("má-startovní-pozici");

    /* New TermIt terms */
    public static final Property ODKAZUJE_NA_ANOTACI = property("odkazuje-na-anotaci");
    public static final Property ODKAZUJE_NA_ANOTOVANY_TEXT = property("odkazuje-na-anotovaný-text");
    public static final Property MA_SKORE = property("má-skóre");

    public static String getURI() {
        return uri;
    }

    private Termit() {
        throw new AssertionError();
    }
    /**
     * Constants from the RDFa vocabulary.
     */
    public static final class RDFa {

        /**
         * RDFa property attribute.
         */
        public static final String PROPERTY = "property";

        /**
         * RDFa context identifier attribute.
         */
        public static final String ABOUT = "about";

        /**
         * RDFa content attribute.
         */
        public static final String CONTENT = "content";

        /**
         * RDFa type identifier attribute.
         */
        public static final String TYPE = "typeof";

        /**
         * RDFa resource identifier.
         */
        public static final String RESOURCE = "resource";

        /**
         * RDFa prefix attribute.
         */
        public static final String PREFIX = "prefix";

        private RDFa() {
            throw new AssertionError();
        }
    }

}
