package fun.drughack.commands.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fun.drughack.DrugHack;
import fun.drughack.commands.Command;
import fun.drughack.utils.network.ChatUtils;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static com.mojang.brigadier.arguments.StringArgumentType.word;

public class FriendCommand extends Command {

    public FriendCommand() {
        super("friend");
    }

    @Override
    public void execute(LiteralArgumentBuilder<CommandSource> builder) {
        builder
                .then(literal("add")
                        .then(arg("player", word())
                                .suggests((context, builder1) -> {
                                    for (PlayerListEntry entry : mc.getNetworkHandler().getPlayerList()) {
                                        String name = entry.getProfile().getName();
                                        if (name.toLowerCase().startsWith(builder1.getRemaining().toLowerCase())) builder1.suggest(name);
                                    }

                                    return builder1.buildFuture();
                                })
                                .executes(context -> {
                                    String player = context.getArgument("player", String.class);
                                    if (!DrugHack.getInstance().getFriendManager().isFriend(player)) {
                                        DrugHack.getInstance().getFriendManager().add(player);
                                        ChatUtils.sendMessage(I18n.translate("commands.friend.added", player));
                                    } else ChatUtils.sendMessage(I18n.translate("commands.friend.already", player));
                                    return SINGLE_SUCCESS;
                                })
                        )
                )
                .then(literal("remove")
                        .then(arg("player", word())
                                .suggests((context, builder1) -> {
                                    DrugHack.getInstance().getFriendManager().getFriends().stream()
                                            .sorted(String::compareTo)
                                            .filter(name -> name.startsWith(builder1.getRemaining()))
                                            .forEach(builder1::suggest);
                                    return builder1.buildFuture();
                                })
                                .executes(context -> {
                                    String player = context.getArgument("player", String.class);
                                    if (DrugHack.getInstance().getFriendManager().isFriend(player)) {
                                        DrugHack.getInstance().getFriendManager().remove(player);
                                        ChatUtils.sendMessage(I18n.translate("commands.friend.removed", player));
                                    } else ChatUtils.sendMessage(I18n.translate("commands.friend.notfound", player));
                                    return SINGLE_SUCCESS;
                                })
                        )
                )
                .then(literal("list")
                        .executes(context -> {
                            StringBuilder builder1 = new StringBuilder();

                            if (DrugHack.getInstance().getFriendManager().getFriends().isEmpty()) ChatUtils.sendMessage(I18n.translate("commands.friend.empty"));
                            else {
                                for (int i = 0; i < DrugHack.getInstance().getFriendManager().getFriends().size(); i++) {
                                    builder1.append(DrugHack.getInstance().getFriendManager().getFriends().get(i));
                                    if (i < DrugHack.getInstance().getFriendManager().getFriends().size() - 1) builder1.append(", ");
                                }
                                builder1.append(".");
                                ChatUtils.sendMessage(I18n.translate("commands.friend.friends") + builder1);
                            }

                            return SINGLE_SUCCESS;
                        })
                )
                .then(literal("clear")
                        .executes(context -> {
                            if (!DrugHack.getInstance().getFriendManager().isEmpty()) {
                                DrugHack.getInstance().getFriendManager().clear();
                                ChatUtils.sendMessage(I18n.translate("commands.friend.cleared"));
                            } else ChatUtils.sendMessage(I18n.translate("commands.friend.empty"));
                            return SINGLE_SUCCESS;
                        })
                );
    }
}