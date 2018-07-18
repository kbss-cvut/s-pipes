/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.cvut.kbss.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author blcha
 */
public class ListUtils {


    public static Map<String, String> createMap(List<String> keys, List<String> values) {
        if (keys.size() != values.size()) {
            throw new RuntimeException("Unable to create map from 2 lists of different size");
        }

        Map<String,String> resultMap =  new HashMap<String, String>();
        for (int i = 0; i<keys.size(); i++) {
            resultMap.put(keys.get(i), values.get(i));
        }
        return resultMap;
    }

   

}
