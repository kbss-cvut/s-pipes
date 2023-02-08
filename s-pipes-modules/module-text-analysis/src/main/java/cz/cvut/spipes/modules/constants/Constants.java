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

/**
 * Application-wide constants.
 */
public class Constants {

    public static final String termitUri = "http://onto.fel.cvut.cz/ontologies/application/termit/pojem/";
    public static final String VYSKYT_TERMU = termitUri + "v\u00fdskyt-termu";
    public static final String JE_VYSKYT_TERMU = termitUri + "je-v\u00fdskytem-termu";
    public static final String MA_KONCOVOU_POZICI = termitUri + "má-koncovou-pozici";
    public static final String MA_STARTOVNI_POZICI = termitUri + "má-startovní-pozici";
    public static final String MA_SKORE = termitUri + "má-skóre";

    private Constants() {
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

    public static final String SCORE = "score";
    public static final String WHOLE_TEXT = "whole-text";
    public static final String REFERENCES_ANNOTATION = "references-annotation";
    public static final String REFERENCES_TEXT = "references-text";
    public static final String ANNOTATION_IN_TEXT_START = "annotation-in-text-start";
    public static final String ANNOTATION_IN_TEXT_END = "annotation-in-text-end";
    public static final String CSVW_ROW_URI = "http://www.w3.org/ns/csvw#Row";

}
