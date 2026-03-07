package io.silvicky.peace;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.silvicky.peace.command.Peace;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Objects;

public class StateSaver extends SavedData
{
    public final HashMap<Identifier, HashMap<MobCategory, HashMap<Vec3i, Long> > > mp;
    public static final String SAVED="saved";
    public static final Codec<StateSaver> CODEC= RecordCodecBuilder.create((instance)->
            instance.group
                    (
                            Codec.unboundedMap(Identifier.CODEC,Codec.unboundedMap(MobCategory.CODEC,Codec.unboundedMap(Codec.STRING.xmap(Peace::stringToV3i,Peace::v3iToString),Codec.LONG).xmap(HashMap::new, map->map)).xmap(HashMap::new, map->map)).xmap(HashMap::new, map->map).fieldOf(SAVED).orElse(new HashMap<>()).forGetter(stateSaver -> stateSaver.mp)
                    ).apply(instance,StateSaver::new));
    public StateSaver(HashMap<Identifier, HashMap<MobCategory, HashMap<Vec3i, Long> > > mp){this.mp=mp;}
    public StateSaver(){this(new HashMap<>());}
    private static final SavedDataType<StateSaver> type = new SavedDataType<>(
            RegionalPeace.MOD_ID,
            StateSaver::new,
            CODEC,
            DataFixTypes.PLAYER
    );

    public static StateSaver getServerState(MinecraftServer server) {
        return getServerState(Objects.requireNonNull(server.getLevel(Level.OVERWORLD)));
    }
    //DO NOT USE THIS UNLESS DURING CONSTRUCTION OF OVERWORLD
    public static StateSaver getServerState(ServerLevel world) {
        DimensionDataStorage persistentStateManager = world.getDataStorage();
        StateSaver state = persistentStateManager.computeIfAbsent(type);
        state.setDirty();
        return state;
    }
}
