package com.suryawebstudio.mark.tools;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ToolRegistry {
    private final Map<String, Tool> tools = new LinkedHashMap<>();

    public synchronized void register(Tool tool) {
        if (tools.containsKey(tool.getName())) throw new IllegalArgumentException("Duplicate tool: " + tool.getName());
        tools.put(tool.getName(), tool);
    }

    public synchronized Tool find(String name) { return tools.get(name); }

    public synchronized Map<String, Tool> all() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(tools));
    }
}
