package at.petrak.hexcasting.common.recipe.ingredient;

import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.google.gson.JsonObject;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

// you ever step back and realize the thoughts that have coursed through your mind for so long
// they've become second nature are in fact incredibly horrific?
// jesus christ I'm making a class called `VillagerIngredient`
public record VillagerIngredient(
    @Nullable ResourceLocation profession,
    @Nullable ResourceLocation biome,     // aka their "type"
    int minLevel
) implements Predicate<Villager> {
    @Override
    public boolean test(Villager villager) {
        var data = villager.getVillagerData();
        ResourceLocation profID = IXplatAbstractions.INSTANCE.getID(data.getProfession());

        return (this.profession == null || this.profession.equals(profID))
            && (this.biome == null || this.biome.equals(Registry.VILLAGER_TYPE.getKey(data.getType())))
            && this.minLevel <= data.getLevel();
    }

    public Component name() {
        MutableComponent component = new TextComponent("");

        boolean addedAny = false;

        if (profession != null) {
            component.append(new TranslatableComponent("entity.minecraft.villager." + profession.getPath()));
            addedAny = true;
        }

        if (biome != null) {
            if (addedAny)
                component.append(" ");
            component.append(new TranslatableComponent("biome.minecraft." + biome.getPath()));
            addedAny = true;
        }

        if (minLevel >= 5) {
            if (addedAny)
                component.append(" ");
            component.append(new TranslatableComponent("merchant.level.5"));
            addedAny = true;
        } else if (minLevel > 1) {
            if (addedAny)
                component.append(" ");
            component.append(new TranslatableComponent("merchant.level." + minLevel));
            addedAny = true;
        }

        if (addedAny)
            component.append(" ");
        component.append(EntityType.VILLAGER.getDescription());

        return component;
    }

    public List<Component> getTooltip() {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(EntityType.VILLAGER.getDescription());

        if (profession != null) {
            var professionKey = "entity.minecraft.villager." + profession.getPath();
            tooltip.add(new TranslatableComponent("hexcasting.tooltip.brainsweep.profession",
                    new TranslatableComponent(professionKey)));
        }

        if (biome != null) {
            var biomeKey = "biome.minecraft." + biome.getPath();
            tooltip.add(new TranslatableComponent("hexcasting.tooltip.brainsweep.biome",
                    new TranslatableComponent(biomeKey)));
        }

        if (minLevel >= 5)
            tooltip.add(new TranslatableComponent("hexcasting.tooltip.brainsweep.level",
                    new TranslatableComponent("merchant.level.5")));
        else if (minLevel > 1)
            tooltip.add(new TranslatableComponent("hexcasting.tooltip.brainsweep.min_level",
                    new TranslatableComponent("merchant.level." + minLevel)));

        tooltip.add(getModNameComponent());

        return tooltip;
    }

    public Component getModNameComponent() {
        String namespace = profession == null ? "minecraft" : profession.getNamespace();
        String mod = IXplatAbstractions.INSTANCE.getModName(namespace);
        return new TextComponent(mod).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC);
    }

    public JsonObject serialize() {
        var obj = new JsonObject();
        if (this.profession != null) {
            obj.addProperty("profession", this.profession.toString());
        }
        if (this.biome != null) {
            obj.addProperty("biome", this.biome.toString());
        }
        obj.addProperty("minLevel", this.minLevel);
        return obj;
    }

    public void write(FriendlyByteBuf buf) {
        if (this.profession != null) {
            buf.writeVarInt(1);
            buf.writeResourceLocation(this.profession);
        } else {
            buf.writeVarInt(0);
        }
        if (this.biome != null) {
            buf.writeVarInt(1);
            buf.writeResourceLocation(this.biome);
        } else {
            buf.writeVarInt(0);
        }
        buf.writeInt(this.minLevel);
    }

    public static VillagerIngredient deserialize(JsonObject json) {
        ResourceLocation profession = null;
        if (json.has("profession")) {
            profession = new ResourceLocation(GsonHelper.getAsString(json, "profession"));
        }
        ResourceLocation biome = null;
        if (json.has("biome")) {
            biome = new ResourceLocation(GsonHelper.getAsString(json, "biome"));
        }
        int minLevel = GsonHelper.getAsInt(json, "minLevel");
        return new VillagerIngredient(profession, biome, minLevel);
    }

    public static VillagerIngredient read(FriendlyByteBuf buf) {
        ResourceLocation profession = null;
        var hasProfession = buf.readVarInt();
        if (hasProfession != 0) {
            profession = buf.readResourceLocation();
        }
        ResourceLocation biome = null;
        var hasBiome = buf.readVarInt();
        if (hasBiome != 0) {
            biome = buf.readResourceLocation();
        }
        int minLevel = buf.readInt();
        return new VillagerIngredient(profession, biome, minLevel);
    }
}
