package fun.drughack.managers;

import fun.drughack.DrugHack;
import fun.drughack.api.events.impl.*;
import fun.drughack.api.events.impl.rotations.EventJump;
import fun.drughack.api.events.impl.rotations.EventMotion;
import fun.drughack.api.events.impl.rotations.EventTrace;
import fun.drughack.api.events.impl.rotations.EventTravel;
import fun.drughack.modules.api.Module;
import fun.drughack.modules.impl.movement.MoveFix;
import fun.drughack.utils.Wrapper;
import fun.drughack.utils.network.NetworkUtils;
import fun.drughack.utils.rotations.RotationChanger;
import fun.drughack.utils.rotations.RotationData;
import lombok.Getter;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RotationManager implements Wrapper {

    private final List<RotationChanger> changers = new ArrayList<>();
    @Getter private final RotationData rotationData = new RotationData();

    public RotationManager() {
        DrugHack.getInstance().getEventHandler().subscribe(this);
    }

    public void addRotation(RotationChanger changer) {
        if (Module.fullNullCheck()) return;

        if (!changers.contains(changer)) {
            changers.add(changer);
            sortRotations();
        }

        rotationData.setRotation(changers.getFirst().rotations().get()[0], changers.getFirst().rotations().get()[1]);
    }

    public void removeRotation(RotationChanger changer) {
        if (Module.fullNullCheck()) return;

        changers.remove(changer);
        sortRotations();
    }

    public void addPacketRotation(float[] rotations) {
        if (Module.fullNullCheck()
                || rotations[0] == DrugHack.getInstance().getServerManager().getServerYaw()
                || rotations[1] == DrugHack.getInstance().getServerManager().getServerPitch()
        ) return;

        NetworkUtils.sendPacket(new PlayerMoveC2SPacket.Full(
                DrugHack.getInstance().getServerManager().getServerX(),
                DrugHack.getInstance().getServerManager().getServerY(),
                DrugHack.getInstance().getServerManager().getServerZ(),
                rotations[0],
                rotations[1],
                DrugHack.getInstance().getServerManager().isServerOnGround(),
                DrugHack.getInstance().getServerManager().isServerHorizontalCollision()
        ));
    }

    public boolean isEmpty() {
        return changers.isEmpty();
    }

    private void sortRotations() {
        changers.sort(Comparator.comparing(RotationChanger::priority));
        Collections.reverse(changers);
    }

    @EventHandler
    public void onTrace(EventTrace e) {
    	if (Module.fullNullCheck()|| changers.isEmpty()|| !DrugHack.getInstance().getModuleManager().getModule(MoveFix.class).isToggled()) return;
    	
    	if (changers.getFirst().remove().get()) removeRotation(changers.getFirst());

        e.setYaw(rotationData.getYaw());
        e.setPitch(rotationData.getPitch());
        e.cancel();
    }

    @EventHandler
    public void onMotion(EventMotion e) {
        if (Module.fullNullCheck() || changers.isEmpty()) return;

    	if (changers.getFirst().remove().get()) removeRotation(changers.getFirst());
        
        e.setYaw(rotationData.getYaw());
        e.setPitch(rotationData.getPitch());
        mc.player.setHeadYaw(rotationData.getYaw());
        mc.player.setBodyYaw(rotationData.getYaw());
    }

    @EventHandler
    public void onTravel(EventTravel e) {
        if (Module.fullNullCheck() || changers.isEmpty() || !DrugHack.getInstance().getModuleManager().getModule(MoveFix.class).isToggled()) return;

    	if (changers.getFirst().remove().get()) removeRotation(changers.getFirst());
        
        e.setYaw(rotationData.getYaw());
        e.setPitch(rotationData.getPitch());
    }

    @EventHandler
    public void onKeyboardInput(EventKeyboardInput e) {
        if (Module.fullNullCheck()|| changers.isEmpty() || !DrugHack.getInstance().getModuleManager().getModule(MoveFix.class).isToggled()) return;
        
    	if (changers.getFirst().remove().get()) removeRotation(changers.getFirst());

        e.setYaw(rotationData.getYaw(), mc.player.getYaw());
    }

    @EventHandler
    public void onJump(EventJump e) {
        if (Module.fullNullCheck() || changers.isEmpty()) return;
        
    	if (changers.getFirst().remove().get()) removeRotation(changers.getFirst());

        e.setYaw(rotationData.getYaw());
    }
}