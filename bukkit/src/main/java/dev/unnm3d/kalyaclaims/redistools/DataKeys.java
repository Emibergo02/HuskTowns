package dev.unnm3d.kalyaclaims.redistools;


public enum DataKeys {
    EXPIRED_CLAIMS("kclaims:expired_");

    private final String keyName;

    /**
     * @param keyName the name of the key
     */
    DataKeys(final String keyName) {
        this.keyName = keyName;
    }

    @Override
    public String toString() {
        return keyName;
    }

}
