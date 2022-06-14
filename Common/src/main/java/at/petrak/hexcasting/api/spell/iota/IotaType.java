package at.petrak.hexcasting.api.spell.iota;

import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;

// Take notes from ForgeRegistryEntry
public abstract class IotaType<T extends Iota> {
    /**
     * Spell datums are stored as such: {@code { "type": "modid:type", "datum": a_tag }}.
     * <p>
     * The {@code type} key is given when registering the spell datum type; this method
     * deserializes the tag associated with {@code "datum"}.
     * <p>
     * Returning {@code null} makes the resulting datum be {@link at.petrak.hexcasting.api.spell.Widget Widget.NULL}.
     * Throwing an exception raises a mishap.
     */
    @Nullable
    public abstract T deserialize(Tag tag, ServerLevel world) throws IllegalArgumentException;

    /**
     * Get a display of this datum from the {@code data} tag, <i>without</i> the world.
     * This is for use on the client.
     */
    public abstract Component display(Tag tag);

    /**
     * Get the color associated with this datum type.
     */
    public abstract int color();
}