package com.igxtul.gram.core;

/** Абстракция хранилища, чтобы ядро не зависело от Android. */
public interface ConfigStore {
    String load();
    void save(String json);
}

/** Хранилище в памяти — для тестов и первого запуска. */
final class MemoryStore implements ConfigStore {
    private String json;

    @Override
    public String load() { return json; }

    @Override
    public void save(String data) { this.json = data; }
}
