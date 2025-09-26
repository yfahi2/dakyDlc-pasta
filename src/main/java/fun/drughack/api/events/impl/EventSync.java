package fun.drughack.api.events.impl;

import fun.drughack.api.events.Event;

public class EventSync extends Event {
    public EventSync(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    float yaw;
    float pitch;
    Runnable postAction;

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void addPostAction(Runnable r) {
        postAction = r;
    }

    public Runnable getPostAction() {
        return postAction;
    }
}