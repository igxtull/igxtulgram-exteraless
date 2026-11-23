package com.igxtul.gram.core;

import java.util.Map;

/** Тема = те же ключи настроек + метаданные (id, имя, автор). */
public final class Theme extends ModConfig {
    private String id = "custom";
    private String name = "Custom";
    private String author = "igxtul";
    private String description = "";

    public Theme() { }

    public static Theme fromMap(Map<String, Object> map) {
        Theme t = new Theme();
        if (map == null) return t;
        t.id = Json.str(map.get("id"), "custom");
        t.name = Json.str(map.get("name"), t.id);
        t.author = Json.str(map.get("author"), "igxtul");
        t.description = Json.str(map.get("description"), "");
        for (Map.Entry<String, Object> e : map.entrySet()) {
            String k = e.getKey();
            if ("id".equals(k) || "name".equals(k) || "author".equals(k) || "description".equals(k)) continue;
            t.setSilent(k, e.getValue());
        }
        t.sanitize();
        t.clearDirty();
        return t;
    }

    public static Theme fromJson(String json) {
        return fromMap(Json.object(Json.parse(json)));
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = super.toMap();
        map.put("id", id);
        map.put("name", name);
        map.put("author", author);
        map.put("description", description);
        return map;
    }

    public String id() { return id; }
    public String name() { return name; }
    public String author() { return author; }
    public String description() { return description; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAuthor(String author) { this.author = author; }
    public void setDescription(String description) { this.description = description; }
}
