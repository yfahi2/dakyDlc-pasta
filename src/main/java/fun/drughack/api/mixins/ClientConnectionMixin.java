package fun.drughack.api.mixins;

import fun.drughack.DrugHack;
import fun.drughack.api.events.impl.EventPacket;
import fun.drughack.utils.network.NetworkUtils;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin {

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
    public void channelRead0(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
        EventPacket.Receive eventReceive = new EventPacket.Receive(packet);
        EventPacket.All eventAll = new EventPacket.All(packet);
        DrugHack.getInstance().getEventHandler().post(eventReceive);
        DrugHack.getInstance().getEventHandler().post(eventAll);
        if (eventReceive.isCancelled() || eventAll.isCancelled()) ci.cancel();
    }

    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
    public void send(Packet<?> packet, CallbackInfo ci) {
        if (NetworkUtils.getSilentPackets().contains(packet)) {
            NetworkUtils.getSilentPackets().remove(packet);
            return;
        }

        EventPacket.Send eventSend = new EventPacket.Send(packet);
        EventPacket.All eventAll = new EventPacket.All(packet);
        DrugHack.getInstance().getEventHandler().post(eventSend);
        DrugHack.getInstance().getEventHandler().post(eventAll);
        if (eventSend.isCancelled() || eventAll.isCancelled()) ci.cancel();
    }
}