package fun.drughack.utils.other;

import lombok.Getter;
import net.minecraft.item.Item;
import net.minecraft.item.Items;


public enum FuntimeItems {
    // АЙАЙАЙАЙ невезуха

    PARANOIA_ARROW("[★] Стрела паранойи", "", false, Items.TIPPED_ARROW);

    @Getter
    private final String name;
    @Getter
    private final String textureValue;
    @Getter
    private final boolean isHead;
    @Getter
    private final Item item;

    FuntimeItems(String name, String textureValue, boolean isHead, Item item) {
        this.name = name;
        this.textureValue = textureValue;
        this.isHead = isHead;
        this.item = item;
    }

}