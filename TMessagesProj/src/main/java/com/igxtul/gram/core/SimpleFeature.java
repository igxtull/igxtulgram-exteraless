package com.igxtul.gram.core;

/** Удобная база для простых фич. */
public abstract class SimpleFeature implements Feature {
    private final String id;
    private final String name;
    private final boolean enabledByDefault;

    protected SimpleFeature(String id, String name, boolean enabledByDefault) {
        this.id = id;
        this.name = name;
        this.enabledByDefault = enabledByDefault;
    }

    @Override public String id() { return id; }
    @Override public String displayName() { return name; }
    @Override public boolean enabledByDefault() { return enabledByDefault; }
}
