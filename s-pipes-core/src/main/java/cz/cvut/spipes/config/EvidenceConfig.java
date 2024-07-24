package cz.cvut.spipes.config;

import cz.cvut.spipes.util.CoreConfigProperies;

public class EvidenceConfig {

    public static int getEvidenceNumber() {
        return Integer.parseInt(CoreConfigProperies.get("evidence.number", "3"));
    }
}
