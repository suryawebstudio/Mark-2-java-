package com.suryawebstudio.mark;

import android.app.Application;

import com.suryawebstudio.mark.executor.TaskExecutor;
import com.suryawebstudio.mark.tools.ToolRegistry;
import com.suryawebstudio.mark.tools.impl.BatteryInfoTool;
import com.suryawebstudio.mark.tools.impl.FlashlightTool;

public final class MarkApplication extends Application {
    private AppContainer appContainer;

    @Override
    public void onCreate() {
        super.onCreate();
        ToolRegistry registry = new ToolRegistry();
        registry.register(new BatteryInfoTool(this));
        registry.register(new FlashlightTool(this));
        appContainer = new AppContainer(registry, new TaskExecutor(registry));
    }

    public AppContainer getAppContainer() {
        return appContainer;
    }

    public static final class AppContainer {
        public final ToolRegistry toolRegistry;
        public final TaskExecutor taskExecutor;

        public AppContainer(ToolRegistry toolRegistry, TaskExecutor taskExecutor) {
            this.toolRegistry = toolRegistry;
            this.taskExecutor = taskExecutor;
        }
    }
}
