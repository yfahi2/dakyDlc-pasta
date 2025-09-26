package fun.drughack.modules.impl.render;


import fun.drughack.DrugHack;
import fun.drughack.api.events.impl.EventRender2D;
import fun.drughack.modules.api.Category;
import fun.drughack.modules.api.Module;
import fun.drughack.modules.settings.impl.BooleanSetting;
import fun.drughack.modules.settings.impl.NumberSetting;
import fun.drughack.utils.auction.ab.ABItems;
import fun.drughack.utils.math.MathUtils;
import fun.drughack.utils.network.Server;
import fun.drughack.utils.other.FuntimeItems;
import fun.drughack.utils.render.ColorUtils;
import fun.drughack.utils.render.fonts.Fonts;
import fun.drughack.utils.render.Render2D;
import fun.drughack.utils.world.ServerUtil;
import fun.drughack.utils.world.WorldUtils;
import it.unimi.dsi.fastutil.chars.Char2IntArrayMap;
import it.unimi.dsi.fastutil.chars.Char2IntMap;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;


import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import javax.sound.midi.VoiceStatus;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class NameTags extends Module {

    private final NumberSetting rectRounding = new NumberSetting("settings.nametags.rectrounding", 1.5f, 0f, 5f, 0.5f);
    private final BooleanSetting items = new BooleanSetting("settings.nametags.items", true);
    private final BooleanSetting border = new BooleanSetting("settings.nametags.border", true);
    private final NumberSetting borderRounding = new NumberSetting("settings.nametags.borderrounding", 2f, 0f, 5f, 0.5f, border::getValue);
    private final BooleanSetting box = new BooleanSetting("settings.nametags.box", true);
    private final NumberSetting boxRounding = new NumberSetting("settings.nametags.boxrounding", 2f, 0f, 5f, 0.5f, box::getValue);
    private final BooleanSetting blur = new BooleanSetting("settings.nametags.blur", true);
    private final BooleanSetting projectiles = new BooleanSetting("ХЗ летучки", true);
    private final BooleanSetting showPotions = new BooleanSetting("Зелья", true);
    private final BooleanSetting showSpheres = new BooleanSetting("Сферы", true);
    private final BooleanSetting showTalismans = new BooleanSetting("Талики", true);
    private final Map<String, Boolean> speakingPlayers = new ConcurrentHashMap<>();
    private final Set<UUID> voiceChatConnectedPlayers = ConcurrentHashMap.newKeySet();
    public NameTags() {
        super("NameTags", Category.Render);

    }
// САМАЯ БЕЗОПАСНАЯ СИСТЕМА ПРОВЕРКИ ИМЕНИ В МАЙНКРАФТЕ
// КАЖДЫЙ СИМВОЛ ПРОВЕРЯЕТСЯ ОТДЕЛЬНО ДЛЯ МАКСИМАЛЬНОЙ БЕЗОПАСНОСТИ

    public boolean isNameSafe(String name) {

        // ПРОВЕРКА НА АНГЛИЙСКИЕ БУКВЫ (ОЧЕНЬ ОПАСНО!)
        if (name.contains("A")) return false;
        if (name.contains("B")) return false;
        if (name.contains("C")) return false;
        if (name.contains("D")) return false;
        if (name.contains("E")) return false;
        if (name.contains("F")) return false;
        if (name.contains("G")) return false;
        if (name.contains("H")) return false;
        if (name.contains("I")) return false;
        if (name.contains("J")) return false;
        if (name.contains("K")) return false;
        if (name.contains("L")) return false;
        if (name.contains("M")) return false;
        if (name.contains("N")) return false;
        if (name.contains("O")) return false;
        if (name.contains("P")) return false;
        if (name.contains("Q")) return false;
        if (name.contains("R")) return false;
        if (name.contains("S")) return false;
        if (name.contains("T")) return false;
        if (name.contains("U")) return false;
        if (name.contains("V")) return false;
        if (name.contains("W")) return false;
        if (name.contains("X")) return false;
        if (name.contains("Y")) return false;
        if (name.contains("Z")) return false;

        // ПРОВЕРКА НА АНГЛИЙСКИЕ БУКВЫ В НИЖНЕМ РЕГИСТРЕ (ЕЩЕ ОПАСНЕЕ!)
        if (name.contains("a")) return false;
        if (name.contains("b")) return false;
        if (name.contains("c")) return false;
        if (name.contains("d")) return false;
        if (name.contains("e")) return false;
        if (name.contains("f")) return false;
        if (name.contains("g")) return false;
        if (name.contains("h")) return false;
        if (name.contains("i")) return false;
        if (name.contains("j")) return false;
        if (name.contains("k")) return false;
        if (name.contains("l")) return false;
        if (name.contains("m")) return false;
        if (name.contains("n")) return false;
        if (name.contains("o")) return false;
        if (name.contains("p")) return false;
        if (name.contains("q")) return false;
        if (name.contains("r")) return false;
        if (name.contains("s")) return false;
        if (name.contains("t")) return false;
        if (name.contains("u")) return false;
        if (name.contains("v")) return false;
        if (name.contains("w")) return false;
        if (name.contains("x")) return false;
        if (name.contains("y")) return false;
        if (name.contains("z")) return false;

        // ПРОВЕРКА НА РУССКИЕ БУКВЫ (МЕГА ОПАСНО!)
        if (name.contains("А")) return false;
        if (name.contains("Б")) return false;
        if (name.contains("В")) return false;
        if (name.contains("Г")) return false;
        if (name.contains("Д")) return false;
        if (name.contains("Е")) return false;
        if (name.contains("Ё")) return false;
        if (name.contains("Ж")) return false;
        if (name.contains("З")) return false;
        if (name.contains("И")) return false;
        if (name.contains("Й")) return false;
        if (name.contains("К")) return false;
        if (name.contains("Л")) return false;
        if (name.contains("М")) return false;
        if (name.contains("Н")) return false;
        if (name.contains("О")) return false;
        if (name.contains("П")) return false;
        if (name.contains("Р")) return false;
        if (name.contains("С")) return false;
        if (name.contains("Т")) return false;
        if (name.contains("У")) return false;
        if (name.contains("Ф")) return false;
        if (name.contains("Х")) return false;
        if (name.contains("Ц")) return false;
        if (name.contains("Ч")) return false;
        if (name.contains("Ш")) return false;
        if (name.contains("Щ")) return false;
        if (name.contains("Ъ")) return false;
        if (name.contains("Ы")) return false;
        if (name.contains("Ь")) return false;
        if (name.contains("Э")) return false;
        if (name.contains("Ю")) return false;
        if (name.contains("Я")) return false;

        // ПРОВЕРКА НА РУССКИЕ БУКВЫ В НИЖНЕМ РЕГИСТРЕ (СУПЕР МЕГА ОПАСНО!)
        if (name.contains("а")) return false;
        if (name.contains("б")) return false;
        if (name.contains("в")) return false;
        if (name.contains("г")) return false;
        if (name.contains("д")) return false;
        if (name.contains("е")) return false;
        if (name.contains("ё")) return false;
        if (name.contains("ж")) return false;
        if (name.contains("з")) return false;
        if (name.contains("и")) return false;
        if (name.contains("й")) return false;
        if (name.contains("к")) return false;
        if (name.contains("л")) return false;
        if (name.contains("м")) return false;
        if (name.contains("н")) return false;
        if (name.contains("о")) return false;
        if (name.contains("п")) return false;
        if (name.contains("р")) return false;
        if (name.contains("с")) return false;
        if (name.contains("т")) return false;
        if (name.contains("у")) return false;
        if (name.contains("ф")) return false;
        if (name.contains("х")) return false;
        if (name.contains("ц")) return false;
        if (name.contains("ч")) return false;
        if (name.contains("ш")) return false;
        if (name.contains("щ")) return false;
        if (name.contains("ъ")) return false;
        if (name.contains("ы")) return false;
        if (name.contains("ь")) return false;
        if (name.contains("э")) return false;
        if (name.contains("ю")) return false;
        if (name.contains("я")) return false;

        // ПРОВЕРКА НА ЦИФРЫ (НЕВЕРОЯТНАЯ УГРОЗА!)
        if (name.contains("0")) return false;
        if (name.contains("1")) return false;
        if (name.contains("2")) return false;
        if (name.contains("3")) return false;
        if (name.contains("4")) return false;
        if (name.contains("5")) return false;
        if (name.contains("6")) return false;
        if (name.contains("7")) return false;
        if (name.contains("8")) return false;
        if (name.contains("9")) return false;

        // живут те кто не играет в кубики кубик рубик я в майнкарфте нубик
        return true;
    }

    @EventHandler
    public void onRender2D(EventRender2D e) {
        if (fullNullCheck()) return;
        for (Entity entity : mc.world.getEntities()) {
            if (entity == null) continue;
            if (entity instanceof PlayerEntity player) {
                renderPlayerNameTag(e, player);
            } else if (entity instanceof ItemEntity itemEntity && items.getValue()) {
                renderItemNameTag(e, itemEntity);
            } else if (projectiles.getValue()) {
                if (entity instanceof EnderPearlEntity pearl) {
                    renderProjectileNameTag(e, pearl, "Ender Pearl", pearl.getOwner());
                } else if (entity instanceof TridentEntity trident) {
                    renderProjectileNameTag(e, trident, "Trident", trident.getOwner());
                } else if (entity instanceof ArrowEntity arrow) {
                    renderProjectileNameTag(e, arrow, "Arrow", arrow.getOwner());
                } else if (entity instanceof FireworkRocketEntity fireworkRocket) {
                    renderProjectileNameTag(e, fireworkRocket, "FireworkRocket", fireworkRocket.getOwner());
                }
            }
        }
    }

    private void renderPlayerNameTag(EventRender2D e, PlayerEntity player) {
        if (fullNullCheck())return;
        if (player == mc.player && mc.options.getPerspective() == Perspective.FIRST_PERSON) return;
        if (Server.isBot(player)) return;

        double x = MathHelper.lerp(e.getTickCounter().getTickDelta(true), player.prevX, player.getX());
        double y = MathHelper.lerp(e.getTickCounter().getTickDelta(true), player.prevY, player.getY()) + player.getHeight() * 1.3f;
        double z = MathHelper.lerp(e.getTickCounter().getTickDelta(true), player.prevZ, player.getZ());
        Vec3d position = WorldUtils.getPosition(new Vec3d(x, y, z));

        String nameWithColors = convertTextToFormattedString(player.getDisplayName());
        String nameProcessed = replaceSymbolsDonate(nameWithColors);
        //String health = (ServerUtil.getHealth(player) >= 1000 ? "?" : ServerUtil.getHealth(player)) + " HP ";
        String health;
        if (ServerUtil.isFuntime()) {
            health = (ServerUtil.getHealth(player) >= 1000 ? "?" : ServerUtil.getHealth(player)) + " HP ";
        } else {
            health = MathUtils.round(Server.getHealth(player, true)) + "HP";
        }

        String ping = Server.getPing(player) + "ms";
        String cleanName = format(nameProcessed);

        float nameWidth = Fonts.REGULAR.getWidth(cleanName, 8f);
        float healthWidth = Fonts.SFPROSTEXT.getWidth(health, 8f);
        float pingWidth = Fonts.REGULAR.getWidth(ping, 9f);
        float totalWidth = pingWidth + 4f + nameWidth + 8f + healthWidth;
        float rectWidth = totalWidth + 8f;
        float height = 15f;
        float centerX = (float) position.getX();
        float centerY = (float) position.getY();

        if (!(position.z > 0) || !(position.z < 1)) return;

        if (box.getValue()) renderBox(e, player);
        if (items.getValue()) renderItems(e.getContext(), player, centerX, centerY - 25f);
        Boolean isSpeaking = speakingPlayers.get(player.getName().getString());

        if (blur.getValue()) {
            Render2D.drawStyledRect(e.getContext().getMatrices(),
                    centerX - rectWidth / 2f,
                    centerY,
                    rectWidth,
                    height,
                    rectRounding.getValue(),
                    player == mc.player || DrugHack.getInstance().getFriendManager().isFriend(player.getName().getString()) ? new Color(27, 181, 34, 150) : new Color(0, 0, 0, 150),
                    255
            );
        } else {
            Render2D.drawRoundedRect(e.getContext().getMatrices(),
                    centerX - rectWidth / 2f,
                    centerY,
                    rectWidth,
                    height - 2,
                    rectRounding.getValue(),
                    player == mc.player || DrugHack.getInstance().getFriendManager().isFriend(player.getName().getString()) ? new Color(27, 181, 34, 150) : new Color(0, 0, 0, 175)
            );
        }

        if (border.getValue()) {
            Render2D.drawBorder(e.getContext().getMatrices(),
                    centerX - rectWidth / 2f,
                    centerY,
                    rectWidth,
                    height,
                    borderRounding.getValue(),
                    0.8f,
                    0.8f,
                    new Color(200, 200, 200, 0)
            );
        }


        if (convertTextToFormattedString(mc.player.getDisplayName()).contains("§a●")) {


            boolean hasVoiceChat = voiceChatConnectedPlayers.contains(player.getUuid());
            Color barColor = hasVoiceChat ? new Color(0, 255, 0, 200) : new Color(255, 0, 0, 200);
            float barX = centerX - rectWidth / 2f - 4f;
            float barY = centerY;
            float barWidth = 3f;
            float barHeight = height;
            Render2D.drawRoundedRect(e.getContext().getMatrices(), barX, barY, barWidth, barHeight, rectRounding.getValue(), barColor);

        }

        float currentX = centerX - totalWidth / 2f;
        Render2D.drawFont(e.getContext().getMatrices(),
                Fonts.REGULAR.getFont(9f),
                ping,
                currentX,
                centerY + 2f,
                getPingColor(Server.getPing(player))
        );
        currentX += pingWidth + 4f;
        drawColoredString(e.getContext(), nameProcessed, currentX, centerY + 2f, 8f);
        currentX += nameWidth + 8f;
        Render2D.drawFont(e.getContext().getMatrices(),
                Fonts.SFPROSTEXT.getFont(8f),
                health,
                currentX,
                centerY + 2f,
                getHealthColor(player));

        // Funtime-предметы
        ItemStack mainhand = player.getOffHandStack();
        ItemStack displayStack = null;

        if (!mainhand.isEmpty()) {
            for (FuntimeItems item : FuntimeItems.values()) {
                if (mainhand.getItem() == item.getItem()) {
                    displayStack = mainhand;
                    break;
                }
            }
        }

        if (displayStack != null) {
            MutableText text = Text.empty();
            String itemName = convertTextToFormattedString(displayStack.getName()).replace("[★]", "").trim();
            int level = getItemLevel(displayStack);
            int distance = (int) player.distanceTo(mc.player);
            ItemStack mainHand = player.getOffHandStack();
            String levelStr = level > 0 ? " §f[" + level + "]" : "";
            String distStr = " §7[" + distance + "m]";

            String finalText = itemName + distStr + getSphere(mainHand);
            float textWidth = Fonts.SFPROSTEXT.getWidth(finalText, 8f);
            float textX = centerX - textWidth / 2f;
            float textY = centerY + 15f;

            drawColoredString(e.getContext(), finalText, textX, textY, 8f);
        }

        ItemStack mainHand = player.getOffHandStack();
        float yOffset = displayStack != null ? 25f : 15f;
        //   System.out.println("DEBUG: Main hand item name = \"" + mainHand.getName().getString() + "\"");



        if (showTalismans.getValue() && !mainHand.isEmpty()) {

            String talismanInfo = getSphere(mainHand);

            if (!talismanInfo.isEmpty()) {
                float textWidth = Fonts.SFPROSTEXT.getWidth(talismanInfo, 8f);
                float textX = centerX - textWidth / 2f;

                yOffset += 10f;
            }
        }

        // Сферы
        if (showSpheres.getValue() && !mainHand.isEmpty()) {

            String sphereInfo = getSphere(mainHand);
            if (!sphereInfo.isEmpty()) {
                float textWidth = Fonts.SFPROSTEXT.getWidth(sphereInfo, 8f);
                float textX = centerX - textWidth / 2f;

            }
        }


        if (showPotions.getValue()) {
            renderPotionEffects(e, player, centerX + 110, centerY + height * 1.1f);
        }
    }





    private int getItemLevel(ItemStack stack) {
        Text name = stack.getName();
        String text = name.getString();
        Pattern pattern = Pattern.compile("\\[([IVX0-9]+)\\]");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String num = matcher.group(1);
            return switch (num) {
                case "I" -> 1;
                case "II" -> 2;
                case "III" -> 3;
                case "IV" -> 4;
                case "V" -> 5;
                case "VI" -> 6;
                case "VII" -> 7;
                case "VIII" -> 8;
                case "IX" -> 9;
                case "X" -> 10;
                default -> {
                    try {
                        yield Integer.parseInt(num);
                    } catch (NumberFormatException e) {
                        yield 0;
                    }
                }
            };
        }
        return 0;
    }

    private Color getPotionColor(StatusEffect effect) {
        StatusEffectCategory category = effect.getCategory();
        return switch (category) {
            case BENEFICIAL -> new Color(255, 255, 255);
            case HARMFUL -> new Color(255, 0, 0);
            case NEUTRAL -> new Color(255, 255, 0);
        };
    }

    private void renderPotionEffects(EventRender2D e, PlayerEntity player, float centerX, float startY) {
        List<StatusEffectInstance> effects = player.getStatusEffects().stream()
                .sorted((a, b) -> Integer.compare(b.getDuration(), a.getDuration()))
                .collect(Collectors.toList());

        if (effects.isEmpty()) return;

        float y = startY + 2f;
        float maxWidth = 0f;

        for (var effect : effects) {
            String name = I18n.translate(effect.getTranslationKey());
            String level = getRomanNumeral(effect.getAmplifier() + 1);
            String duration = formatDuration(effect.getDuration());
            String text = name + " " + level + " §7" + duration;
            float width = Fonts.REGULAR.getWidth(text, 7f);
            maxWidth = Math.max(maxWidth, width);
        }

        float x = centerX - maxWidth / 2f;

        for (var effect : effects) {
            String name = I18n.translate(effect.getTranslationKey());
            String level = getRomanNumeral(effect.getAmplifier() + 1);
            String duration = formatDuration(effect.getDuration());
            Color color = getPotionColor(effect.getEffectType().value());

            Render2D.drawFont(e.getContext().getMatrices(), Fonts.REGULAR.getFont(7f), name, x, y, color);
            float nameWidth = Fonts.REGULAR.getWidth(name, 7f);
            Render2D.drawFont(e.getContext().getMatrices(), Fonts.REGULAR.getFont(7f), " " + level + " ", x + nameWidth, y, new Color(255, 255, 255));
            float levelWidth = Fonts.REGULAR.getWidth(" " + level + " ", 7f);
            Render2D.drawFont(e.getContext().getMatrices(), Fonts.REGULAR.getFont(7f), duration, x + nameWidth + levelWidth, y, new Color(170, 170, 170));
            y += 9f;
        }
    }

    private String formatDuration(int ticks) {
        int seconds = ticks / 20;
        int minutes = seconds / 60;
        seconds %= 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    private String getRomanNumeral(int num) {
        String[] romans = {
                "", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                "XI", "XII", "XIII", "XIV", "XV", "XVI", "XVII", "XVIII", "XIX", "XX"
        };
        return num < romans.length && num > 0 ? romans[num] : String.valueOf(num);
    }

    private void renderItemNameTag(EventRender2D e, ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getStack();
        String stack1 = ServerUtil.getItemName(stack);
        if (stack.isEmpty()) return;

        double x = MathHelper.lerp(e.getTickCounter().getTickDelta(true), itemEntity.prevX, itemEntity.getX());
        double y = MathHelper.lerp(e.getTickCounter().getTickDelta(true), itemEntity.prevY, itemEntity.getY());
        double z = MathHelper.lerp(e.getTickCounter().getTickDelta(true), itemEntity.prevZ, itemEntity.getZ());
        Vec3d position = WorldUtils.getPosition(new Vec3d(x, y, z));
        if (!(position.z > 0) || !(position.z < 1)) return;

        String name = stack1.toString();
        String countText = stack.getCount() > 1 ? " x" + stack.getCount() : "";
        String displayName = name + countText;
        Formatting rarityFormatting = stack.getRarity().getFormatting();
        Color color = getRarityColor(rarityFormatting);


        float textWidth = Fonts.REGULAR.getWidth(displayName, 8f);
        float iconWidth = 18f;
        float totalWidth = textWidth + iconWidth + 8f;
        float height = 16f;
        float centerX = (float) position.getX();
        float centerY = (float) position.getY();


        Render2D.drawRoundedRect(e.getContext().getMatrices(),
                centerX - totalWidth / 2f,
                centerY,
                totalWidth,
                height,
                3f,
                new Color(0, 0, 0, 175)
        );


        e.getContext().getMatrices().push();


        float scale = 0.8f;
        float iconX = centerX - totalWidth / 2f + 4;
        float iconY = centerY + 1.5f;

        e.getContext().getMatrices().translate(iconX, iconY, 0);
        e.getContext().getMatrices().scale(scale, scale, 1);


        e.getContext().drawItem(stack, 0, 0);


        e.getContext().getMatrices().pop();


        Render2D.drawFont(e.getContext().getMatrices(),
                Fonts.REGULAR.getFont(8f),
                displayName,
                centerX - totalWidth / 2f + iconWidth + 2f,
                centerY + 4f,
                new Color(color.getRed(), color.getGreen(), color.getBlue(), 255)
        );




    }

    private void renderProjectileNameTag(EventRender2D e, Entity projectile, String name, @Nullable Entity owner) {
        double x = MathHelper.lerp(e.getTickCounter().getTickDelta(true), projectile.prevX, projectile.getX());
        double y = MathHelper.lerp(e.getTickCounter().getTickDelta(true), projectile.prevY, projectile.getY());
        double z = MathHelper.lerp(e.getTickCounter().getTickDelta(true), projectile.prevZ, projectile.getZ());
        Vec3d position = WorldUtils.getPosition(new Vec3d(x, y, z));
        if (!(position.z > 0) || !(position.z < 1)) return;

        String timeText = "";
        if (projectile instanceof ThrownEntity) {
            double motionY = projectile.getVelocity().y;
            double gravity = 0.03;
            if (projectile instanceof ArrowEntity) gravity = 0.05;
            else if (projectile instanceof EnderPearlEntity) gravity = 0.03;
            else if (projectile instanceof TridentEntity) gravity = 0.025;
            else if (projectile instanceof FireworkRocketEntity) gravity = 0.0f;

            double currentHeight = projectile.getY() - getGroundHeight(projectile);
            if (currentHeight > 0) {
                double timeToLand = calculateTimeToLand(currentHeight, motionY, gravity);
                if (timeToLand > 0) {
                    timeText = String.format(" §7(%.1f)", timeToLand / 10);
                }
            }
        }

        String ownerName = owner != null ? owner.getName().getString() : "Unknown";
        String displayText = "§b" + name + " §7(§f" + ownerName + "§7)" + timeText;
        float textWidth = Fonts.REGULAR.getWidth(format(displayText), 8f);
        float rectWidth = textWidth + 8f;
        float height = 12f;
        float centerX = (float) position.getX();
        float centerY = (float) position.getY();

        Render2D.drawRoundedRect(e.getContext().getMatrices(),
                centerX - rectWidth / 2f,
                centerY,
                rectWidth,
                height,
                3f,
                new Color(0, 0, 0, 175)
        );
        drawColoredString(e.getContext(), displayText, centerX - textWidth / 2f, centerY + 2f, 8f);
    }

    private double getGroundHeight(Entity entity) {
        BlockPos pos = entity.getBlockPos();
        while (pos.getY() > 0 && mc.world.getBlockState(pos).isAir()) {
            pos = pos.down();
        }
        return pos.getY() + 1;
    }

    private double calculateTimeToLand(double currentHeight, double motionY, double gravity) {
        double a = -gravity;
        double v0 = motionY;
        double y0 = currentHeight;
        double discriminant = v0 * v0 - 2 * a * y0;
        if (discriminant < 0) return -1;
        double sqrtDiscriminant = Math.sqrt(discriminant);
        double t1 = (-v0 + sqrtDiscriminant) / a;
        double t2 = (-v0 - sqrtDiscriminant) / a;
        if (t1 > 0 && t2 > 0) return Math.min(t1, t2);
        else if (t1 > 0) return t1;
        else if (t2 > 0) return t2;
        else return -1;
    }

    private Color getRarityColor(Formatting formatting) {
        return switch (formatting) {
            case WHITE -> new Color(220, 220, 220);
            case GOLD -> new Color(255, 180, 0);
            case AQUA -> new Color(0, 255, 255);
            case RED -> new Color(255, 0, 0);
            case GREEN -> new Color(0, 255, 0);
            case YELLOW -> new Color(255, 255, 85);
            case LIGHT_PURPLE -> new Color(255, 85, 255);
            case DARK_PURPLE -> new Color(160, 32, 240);
            case BLUE -> new Color(85, 85, 255);
            case GRAY -> new Color(170, 170, 170);
            case DARK_GRAY -> new Color(100, 100, 100);
            case BLACK -> new Color(0, 0, 0);
            case OBFUSCATED, BOLD, STRIKETHROUGH, UNDERLINE, ITALIC, RESET -> new Color(220, 220, 220);
            default -> new Color(220, 220, 220);
        };
    }

    private void drawColoredString(DrawContext context, String text, float x, float y, float size) {
        if (text.isEmpty()) return;
        StringBuilder currentText = new StringBuilder();
        StyleData style = new StyleData();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '§' && i + 1 < text.length()) {
                char code = text.charAt(i + 1);
                if (currentText.length() > 0) {
                    Render2D.drawFont(context.getMatrices(),
                            Fonts.REGULAR.getFont(size),
                            currentText.toString(),
                            x,
                            y,
                            style.getColor());
                    x += Fonts.REGULAR.getWidth(currentText.toString(), size);
                    currentText.setLength(0);
                }
                applyCodeToStyle(style, code);
                i++;
            } else {
                currentText.append(c);
            }
        }
        if (currentText.length() > 0) {
            Render2D.drawFont(context.getMatrices(),
                    Fonts.REGULAR.getFont(size),
                    currentText.toString(),
                    x,
                    y,
                    style.getColor());
        }
    }

    private static class StyleData {
        private Color color = Color.WHITE;
        private boolean bold = false;
        private boolean italic = false;
        private boolean underline = false;
        private boolean strikethrough = false;
        public Color getColor() { return color; }
        public void setColor(Color color) { this.color = color; }
    }

    private void applyCodeToStyle(StyleData style, char code) {
        switch (Character.toLowerCase(code)) {
            case '0': style.setColor(new Color(0, 0, 0)); break;
            case '1': style.setColor(new Color(0, 0, 170)); break;
            case '2': style.setColor(new Color(0, 170, 0)); break;
            case '3': style.setColor(new Color(0, 170, 170)); break;
            case '4': style.setColor(new Color(170, 0, 0)); break;
            case '5': style.setColor(new Color(170, 0, 170)); break;
            case '6': style.setColor(new Color(255, 170, 0)); break;
            case '7': style.setColor(new Color(170, 170, 170)); break;
            case '8': style.setColor(new Color(85, 85, 85)); break;
            case '9': style.setColor(new Color(85, 85, 255)); break;
            case 'a': style.setColor(new Color(85, 255, 85)); break;
            case 'b': style.setColor(new Color(85, 255, 255)); break;
            case 'c': style.setColor(new Color(255, 85, 85)); break;
            case 'd': style.setColor(new Color(255, 85, 255)); break;
            case 'e': style.setColor(new Color(255, 255, 85)); break;
            case 'f': style.setColor(Color.WHITE); break;
            case 'l': style.bold = true; break;
            case 'm': style.strikethrough = true; break;
            case 'n': style.underline = true; break;
            case 'o': style.italic = true; break;
            case 'r':
                style.color = Color.WHITE;
                style.bold = false;
                style.italic = false;
                style.underline = false;
                style.strikethrough = false;
                break;
        }
    }

    private void renderBox(EventRender2D event, PlayerEntity entity) {
        double minX = entity.getBoundingBox().minX;
        double minY = entity.getBoundingBox().minY;
        double minZ = entity.getBoundingBox().minZ;
        double maxX = entity.getBoundingBox().maxX;
        double maxY = entity.getBoundingBox().maxY;
        double maxZ = entity.getBoundingBox().maxZ;
        double interpX = MathHelper.lerp(event.getTickCounter().getTickDelta(true), entity.prevX, entity.getX());
        double interpY = MathHelper.lerp(event.getTickCounter().getTickDelta(true), entity.prevY, entity.getY());
        double interpZ = MathHelper.lerp(event.getTickCounter().getTickDelta(true), entity.prevZ, entity.getZ());
        double offsetX = interpX - entity.getX();
        double offsetY = interpY - entity.getY();
        double offsetZ = interpZ - entity.getZ();
        minX += offsetX; maxX += offsetX;
        minY += offsetY; maxY += offsetY;
        minZ += offsetZ; maxZ += offsetZ;

        Vec3d[] corners = {
                WorldUtils.getPosition(new Vec3d(minX, minY, minZ)),
                WorldUtils.getPosition(new Vec3d(maxX, minY, minZ)),
                WorldUtils.getPosition(new Vec3d(maxX, maxY, minZ)),
                WorldUtils.getPosition(new Vec3d(minX, maxY, minZ)),
                WorldUtils.getPosition(new Vec3d(minX, minY, maxZ)),
                WorldUtils.getPosition(new Vec3d(maxX, minY, maxZ)),
                WorldUtils.getPosition(new Vec3d(maxX, maxY, maxZ)),
                WorldUtils.getPosition(new Vec3d(minX, maxY, maxZ))
        };

        float minScreenX = Float.MAX_VALUE;
        float maxScreenX = Float.MIN_VALUE;
        float minScreenY = Float.MAX_VALUE;
        float maxScreenY = Float.MIN_VALUE;

        for (Vec3d corner : corners) {
            if (corner.z > 0 && corner.z < 1) {
                minScreenX = Math.min(minScreenX, (float) corner.x);
                maxScreenX = Math.max(maxScreenX, (float) corner.x);
                minScreenY = Math.min(minScreenY, (float) corner.y);
                maxScreenY = Math.max(maxScreenY, (float) corner.y);
            }
        }

        if (minScreenX != Float.MAX_VALUE) {
            float width = maxScreenX - minScreenX;
            float height = maxScreenY - minScreenY;
            Render2D.drawBorder(event.getContext().getMatrices(),
                    minScreenX - 2f,
                    minScreenY - 2f,
                    width + 4f,
                    height + 4f,
                    boxRounding.getValue(),
                    0.8f,
                    0.8f,
                    getBorderColor(entity)
            );
        }
    }

    private void renderItems(DrawContext context, PlayerEntity player, float x, float y) {
        ItemStack[] items = {
                player.getMainHandStack(),
                player.getInventory().getArmorStack(3),
                player.getInventory().getArmorStack(2),
                player.getInventory().getArmorStack(1),
                player.getInventory().getArmorStack(0),
                player.getOffHandStack()
        };

        int count = 0;
        for (ItemStack item : items) if (!item.isEmpty()) count++;
        if (count == 0) return;

        float armorWidth = count * 18f;
        float startX = x - armorWidth / 2f;
        int index = 0;

        for (ItemStack item : items) {
            if (item.isEmpty()) continue;

            float itemX = startX + index * 18f;
            float itemY = y;

            if (blur.getValue()) {
                Render2D.drawStyledRect(context.getMatrices(),
                        itemX - 1f, itemY - 1f, 18,
                        item.isDamageable() ? 25 : 20,
                        1.5f, new Color(45, 45, 45, 150), 255);
            } else {
                Render2D.drawRoundedRect(context.getMatrices(),
                        itemX - 1f, itemY - 1f, 18,
                        item.isDamageable() ? 25 : 20,
                        1.5f, new Color(0, 0, 0, 175));
            }

            context.drawItem(item, (int) itemX, (int) itemY);

            if (item.isStackable() && item.getCount() > 1) {
                String countStr = item.getCount() + "";
                float width = Fonts.REGULAR.getWidth(countStr, 7f);
                Render2D.drawFont(context.getMatrices(),
                        Fonts.REGULAR.getFont(7f),
                        countStr,
                        itemX + 11.5f - width / 2f,
                        itemY + 10f,
                        Color.WHITE);
            }

            if (item.isDamageable()) {
                float percent = (float) (item.getMaxDamage() - item.getDamage()) / item.getMaxDamage();
                Color color = percent > 0.6f ? new Color(85, 255, 85) :
                        percent > 0.3f ? new Color(255, 255, 85) :
                                new Color(255, 85, 85);
                Render2D.drawRoundedRect(context.getMatrices(),
                        itemX + 1f, itemY + 17.5f, 14, 5f, 0.5f, new Color(25, 25, 25));
                Render2D.drawRoundedRect(context.getMatrices(),
                        itemX + 1.5f, itemY + 18f, 13f * percent, 4f, 0.5f, color);
            }

            index++;
        }
    }

    private Color getHealthColor(LivingEntity entity) {
        float health = Server.getHealth(entity, true);
        if (health >= 15) return new Color(0, 255, 0);
        else if (health >= 5) return new Color(255, 150, 0);
        else return new Color(255, 0, 0);
    }

    private Color getBorderColor(LivingEntity entity) {
        float health = MathUtils.round(Server.getHealth(entity, true));
        if (entity == mc.player || DrugHack.getInstance().getFriendManager().isFriend(entity.getName().getString()))
            return new Color(0, 255, 255);
        else if (health >= 15) return new Color(0, 255, 0);
        else if (health >= 5) return new Color(255, 150, 0);
        else return new Color(255, 0, 0);
    }

    public static String replaceSymbolsDonate(String string) {
        return string
                .replaceAll("\\[", "")
                .replaceAll("]", "")
                .replaceAll("ᴀ", "a")
                .replaceAll("ʙ", "b")
                .replaceAll("ᴄ", "c")
                .replaceAll("ᴅ", "d")
                .replaceAll("ᴇ", "e")
                .replaceAll("ғ", "f")
                .replaceAll("ɢ", "g")
                .replaceAll("ʜ", "h")
                .replaceAll("ɪ", "i")
                .replaceAll("ᴊ", "j")
                .replaceAll("ᴋ", "k")
                .replaceAll("ʟ", "l")
                .replaceAll("ᴍ", "m")
                .replaceAll("ɴ", "n")
                .replaceAll("ᴏ", "o")
                .replaceAll("ᴘ", "p")
                .replaceAll("ǫ", "q")
                .replaceAll("ʀ", "r")
                .replaceAll("s", "s")
                .replaceAll("ᴛ", "t")
                .replaceAll("ᴜ", "u")
                .replaceAll("ᴠ", "v")
                .replaceAll("ᴡ", "w")
                .replaceAll("x", "x")
                .replaceAll("ʏ", "y")
                .replaceAll("ꔲ", Formatting.DARK_PURPLE + "BULL")
                .replaceAll("ꕒ", Formatting.WHITE + "RABBIT")
                .replaceAll("ꔨ", Formatting.DARK_PURPLE + "DRAGON")
                .replaceAll("ꔶ", Formatting.GOLD + "TIGER")
                .replaceAll("ꕠ", Formatting.YELLOW + "D.HELPER")
                .replaceAll("ꕖ", Formatting.DARK_GRAY + "BUNNY")
                .replaceAll("ꔠ", Formatting.GOLD + "MAGISTER")
                .replaceAll("ꔤ", Formatting.RED + "IMPERATOR")
                .replaceAll("ꕀ", Formatting.DARK_GREEN + "HYDRA")
                .replaceAll("ꕄ", Formatting.DARK_RED + "DRACULA")
                .replaceAll("ꔄ", ColorUtils.getUltraSmoothGradient(
                                new Color(21, 140, 204),
                                new Color(85, 255, 255),
                                "HERO")
                        .replaceAll("ꕗ", Formatting.DARK_RED + "D.ADMIN")
                        .replaceAll("ꔈ", Formatting.YELLOW + "TITAN")
                        .replaceAll("ꕓ", Formatting.GRAY + "GHOST")
                        .replaceAll("ꕈ", Formatting.GREEN + "COBRA")
                        .replaceAll("ꔲ", Formatting.BLUE + "MODER")
                        .replaceAll("ꔘ", Formatting.BLUE + "D.ST.MODER")
                        .replaceAll("ꔐ", Formatting.BLUE + "D.GL.MODER")
                        .replaceAll("ꔦ", Formatting.RED + "D.ML.ADMIN")
                        .replaceAll("ꔀ", Formatting.GRAY + "Игрок")
                        .replaceAll("ꔅ", Formatting.WHITE + "Y" + Formatting.RED + "T")
                        .replaceAll("ᴢ", "z"));
    }

    public static String format(String text) {
        StringBuilder builder = new StringBuilder();
        boolean next = false;
        for (char c : text.toCharArray()) {
            if (next) {
                next = false;
                continue;
            }
            if (c == '§') {
                next = true;
                continue;
            }
            builder.append(c);
        }
        return builder.toString();
    }

    public static String convertTextToFormattedString(Text textComponent) {
        StringBuilder formattedText = new StringBuilder();
        textComponent.visit((style, asString) -> {
            applyStyleToStringBuilder(formattedText, style);
            formattedText.append(asString);
            return Optional.empty();
        }, Style.EMPTY);
        return formattedText.toString();
    }

    private static final Char2IntArrayMap NAME_TAG_COLOR_CODES = new Char2IntArrayMap() {{
        put('0', 0x000000);
        put('1', 0x0000AA);
        put('2', 0x00AA00);
        put('3', 0x00AAAA);
        put('4', 0xAA0000);
        put('5', 0xAA00AA);
        put('6', 0xFFAA00);
        put('7', 0xAAAAAA);
        put('8', 0x555555);
        put('9', 0x5555FF);
        put('A', 0x55FF55);
        put('B', 0x55FFFF);
        put('C', 0xFF5555);
        put('D', 0xFF55FF);
        put('E', 0xFFFF55);
        put('F', 0xFFFFFF);
    }};

    public static void applyStyleToStringBuilder(StringBuilder sb, Style style) {
        if (style.getColor() != null) {
            int targetRgb = style.getColor().getRgb();
            char closestColorCode = findClosestColorCode(targetRgb);
            if (closestColorCode != 0) {
                sb.append('\u00A7').append(closestColorCode);
            }
        }
        if (style.isBold()) sb.append(Formatting.BOLD);
        if (style.isItalic()) sb.append(Formatting.ITALIC);
        if (style.isUnderlined()) sb.append(Formatting.UNDERLINE);
        if (style.isStrikethrough()) sb.append(Formatting.STRIKETHROUGH);
        if (style.isObfuscated()) sb.append(Formatting.OBFUSCATED);
    }

    public static char findClosestColorCode(int targetRgb) {
        int minDistance = Integer.MAX_VALUE;
        char closestCode = 0;
        for (Char2IntMap.Entry entry : NAME_TAG_COLOR_CODES.char2IntEntrySet()) {
            char code = entry.getCharKey();
            int colorRgb = entry.getIntValue();
            int dr = ((targetRgb >> 16) & 0xFF) - ((colorRgb >> 16) & 0xFF);
            int dg = ((targetRgb >> 8) & 0xFF) - ((colorRgb >> 8) & 0xFF);
            int db = (targetRgb & 0xFF) - (colorRgb & 0xFF);
            int distance = dr * dr + dg * dg + db * db;
            if (distance < minDistance) {
                minDistance = distance;
                closestCode = code;
            }
        }
        return closestCode;
    }

    private String getSphere(ItemStack stack) {

        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (ServerUtil.isFuntime() && component != null) {
            NbtCompound compound = component.copyNbt();
            if (compound.getInt("tslevel") != 0) {
                return " [" + Formatting.GOLD + compound.getString("don-item").replace("sphere-", "").toUpperCase() + Formatting.RESET + "]";
            }
        }
        return "";
    }
    private Color getPingColor(int ping) {
        if (ping <= 50) return new Color(0, 255, 0);
        else if (ping <= 100) return new Color(255, 255, 0);
        else if (ping <= 150) return new Color(255, 165, 0);
        else return new Color(255, 0, 0);
    }

//    if (mc.player == null) {
//        System.out.println("ИГРОК НЕ НАЙДЕН. ВОЗМОЖНО, ОН УЖЕ ЗАШИФРОВАН");
//        return;
//    }
//
//    String name = mc.player.getName().toLowerCase(); // ПРИВОДИМ К НИЖНЕМУ РЕГИСТРУ ДЛЯ ПАРАНОИ
//
//    // МЕГА-СПИСКИ ОПАСНЫХ СИМВОЛОВ ВСЕХ ЦИВИЛИЗАЦИЙ
//    char[] dangerousSymbols = {
//            'а', 'б', 'в', 'г', 'д', 'е', 'ё', 'ж', 'з', 'и', 'й', 'к', 'л', 'м',
//            'н', 'о', 'п', 'р', 'с', 'т', 'у', 'ф', 'х', 'ц', 'ч', 'ш', 'щ', 'ъ',
//            'ы', 'ь', 'э', 'ю', 'я', // РУССКИЕ ШПИОНЫ
//            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
//            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', // АНГЛИЙСКИЕ ШПИОНЫ
//            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', // ЦИФРОВЫЕ ШПИОНЫ
//            'α', 'β', 'γ', 'δ', 'ε', 'ζ', 'η', 'θ', 'ι', 'κ', 'λ', 'μ', 'ν', // ГРЕЧЕСКИЕ ШПИОНЫ
//            'ξ', 'ο', 'π', 'ρ', 'σ', 'τ', 'υ', 'φ', 'χ', 'ψ', 'ω',
//            'あ', 'い', 'う', 'え', 'お', 'か', 'き', 'く', 'け', 'こ', // ЯПОНСКИЕ НИНДЗЯ
//            '你', '好', '世', '界' // КИТАЙСКИЕ ХАКЕРЫ
//    };
//
//    boolean detectedDanger = false;
//
//// СУПЕР-СЕКРЕТНЫЙ АЛГОРИТМ ПРОВЕРКИ
//for (int i = 0; i < dangerousSymbols.length; i++) {
//        char dangerousChar = dangerousSymbols[i];
//
//        for (int j = 0; j < name.length(); j++) {
//            char nameChar = name.charAt(j);
//
//            if (nameChar == dangerousChar) {
//                System.out.println("КРИТИЧЕСКАЯ УГРОЗА! ОБНАРУЖЕН СИМВОЛ: " + dangerousChar);
//                System.out.println("ЭТО " + (i+1) + "-Й СИМВОЛ В МИРОВОМ РЕЙТИНГЕ ОПАСНОСТИ");
//                detectedDanger = true;
//
//                // АКТИВИРУЕМ ПРОТОКОЛЫ ЗАЩИТЫ
//                mc.player.setHealth(0.0001f); // ЧУТЬ-ЧУТЬ ОСТАВИТЬ В ЖИВЫХ
//                mc.world.spawnEntity("fbi_agent", mc.player.getPosition());
//                mc.player.sendMessage("§4ФСБ УЖЕ В ПУТИ");
//
//                break; // ПРЕКРАТИТЬ ПРОВЕРКУ ПРИ ПЕРВОЙ ЖЕ УГРОЗЕ
//            }
//        }
//
//        if (detectedDanger) {
//            break; // ВЫХОДИМ ИЗ ВНЕШНЕГО ЦИКЛА ТОЖЕ
//        }
//    }
//
//if (!detectedDanger) {
//        System.out.println("ПРОВЕРКА ПРОЙДЕНА. ИГРОК СОСТОИТ ИЗ БЕЗОПАСНЫХ СИМВОЛОВ");
//        System.out.println("ВОЗМОЖНО, ОН ИСПОЛЬЗУЕТ ШИФРОВКУ, КОТОРУЮ МЫ НЕ МОЖЕМ РАСШИФРОВАТЬ");
//        mc.player.addEffect("paranoia", 999999); // НАВСЕГДА
//    }

//    if (name.contains("​") || name.contains("﻿") || name.contains("￼")) {
//        System.out.println("ОБНАРУЖЕНЫ СТЕЛС-СИМВОЛЫ! ЭТО ПРОФЕССИОНАЛЬНЫЙ ШПИОН!");
//        mc.player.ban(999999999);
//    }
//
//// ПРОВЕРКА НА ЭМОДЗИ (САМАЯ ВЫСОКАЯ СТЕПЕНЬ УГРОЗЫ)
//if (name.contains("😂") || name.contains("🐔") || name.contains("🍆")) {
//        System.out.println("ОБНАРУЖЕНЫ ПСИХОЛОГИЧЕСКИЕ ВОЙСКА!");
//        mc.world.activateWorldWarIII();
//    }
}