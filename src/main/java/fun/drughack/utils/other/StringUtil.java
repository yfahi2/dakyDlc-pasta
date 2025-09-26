package fun.drughack.utils.other;


import fun.drughack.utils.Wrapper;
import lombok.experimental.UtilityClass;
import net.minecraft.client.util.InputUtil;

import java.util.Random;

import static net.minecraft.client.util.InputUtil.Type.*;

@UtilityClass
public class StringUtil implements Wrapper {

    public static String capitalizeWords(String input) {
        if (input == null || input.isEmpty()) return input;
        String[] words = input.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) continue;
            sb.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1).toLowerCase())
                    .append(" ");
        }
        return sb.toString().trim();
    }

    public static String toRomanNumeral(int number) {
        switch (number) {
            case 1: return "1";
            case 2: return "2";
            case 3: return "3";
            case 4: return "4";
            case 5: return "5";
            case 6: return "6";
            case 7: return "7";
            case 8: return "8";
            case 9: return "9";
            case 10: return "10";
            default: return String.valueOf(number);
        }
    }

    public static String formatTicks(int ticks) {
        int totalSeconds = ticks / 20;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    public String generateString(int length) {
        Random random = new Random();
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return builder.toString();
    }

    public static String getBindName(int key) {
        InputUtil.Key isMouse = key < 8 ? MOUSE.createFromCode(key) : KEYSYM.createFromCode(key);

        InputUtil.Key code = key == -1
                ? SCANCODE.createFromCode(key)
                : isMouse;

        if (key == -1) {
            return "N/A";
        }

        String bindName = code.getTranslationKey()
                .replace("key.keyboard.", "")
                .replace("key.mouse.", "mouse ")
                .replace(".", " ")
                .toUpperCase();

        return shortenBindName(bindName);
    }

    public static String shortenBindName(String bindName) {
        if (bindName == null) return "";
        bindName = bindName.toUpperCase().replace('_', ' ').trim();
        if (bindName.equals("INSERT")) {
            return "INS";
        } else if (bindName.equals("PAGE DOWN")) {
            return "P DOWN";
        } else if (bindName.equals("PAGE UP")) {
            return "P UP";
        } else if (bindName.equals("PRINT SCREEN")) {
            return "PR SC";
        } else if (bindName.equals("NUMPAD 0")) {
            return "NUM 0";
        } else if (bindName.equals("NUMPAD 1")) {
            return "NUM 1";
        } else if (bindName.equals("NUMPAD 2")) {
            return "NUM 2";
        } else if (bindName.equals("NUMPAD 3")) {
            return "NUM 3";
        } else if (bindName.equals("NUMPAD 4")) {
            return "NUM 4";
        } else if (bindName.equals("NUMPAD 5")) {
            return "NUM 5";
        } else if (bindName.equals("NUMPAD 6")) {
            return "NUM 6";
        } else if (bindName.equals("NUMPAD 7")) {
            return "NUM 7";
        } else if (bindName.equals("NUMPAD 8")) {
            return "NUM 8";
        } else if (bindName.equals("NUMPAD 9")) {
            return "NUM 9";
        } else if (bindName.equals("ESCAPE")) {
            return "ESC";
        } else if (bindName.equals("BACKSPACE")) {
            return "BACKSPC";
        } else if (bindName.equals("TAB")) {
            return "TAB";
        } else if (bindName.equals("CAPS LOCK")) {
            return "CAPS";
        } else if (bindName.equals("LEFT SHIFT")) {
            return "L SHIFT";
        } else if (bindName.equals("RIGHT SHIFT")) {
            return "R SHIFT";
        } else if (bindName.equals("LEFT CONTROL")) {
            return "L CTRL";
        } else if (bindName.equals("RIGHT CONTROL")) {
            return "R CTRL";
        } else if (bindName.equals("LEFT ALT")) {
            return "L ALT";
        } else if (bindName.equals("RIGHT ALT")) {
            return "R ALT";
        } else if (bindName.equals("SPACE")) {
            return "SPACE";
        } else if (bindName.equals("ENTER")) {
            return "ENTER";
        } else if (bindName.equals("DELETE")) {
            return "DEL";
        }
        return bindName;
    }
}
