package fun.drughack.utils.rotations;

import java.util.function.Supplier;

public record RotationChanger(
        int priority,
        Supplier<Float[]> rotations,
        Supplier<Boolean> remove
) {}