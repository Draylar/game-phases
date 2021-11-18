package draylar.gamephases.impl;

import java.util.Map;

public interface PlayerDataProvider {

    boolean has(String phase);
    void set(String phase, boolean status);
    void set(Map<String, Boolean> all);
    void sync();
}
