package fun.drughack.api.events.impl;

import fun.drughack.api.events.Event;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class EventCollision extends Event {
    private BlockState bs;
    private BlockPos bp;

    public EventCollision(BlockState bs, BlockPos bp) {
        this.bs = bs;
        this.bp = bp;
    }

    public BlockState getState() {
        return bs;
    }

    public BlockPos getPos() {
        return bp;
    }

    public void setState(BlockState bs) {
        this.bs = bs;
    }
}