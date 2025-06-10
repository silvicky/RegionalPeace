package io.silvicky.peace;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.silvicky.peace.command.Peace;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Objects;

public class StateSaver extends PersistentState {
    public final HashMap<Identifier, HashMap<SpawnGroup, HashMap<Vec3i, Long> > > mp;
    public static final String SAVED="saved";
    public static final Codec<StateSaver> CODEC= RecordCodecBuilder.create((instance)->
            instance.group
                    (
                            Codec.unboundedMap(Identifier.CODEC,Codec.unboundedMap(SpawnGroup.CODEC,Codec.unboundedMap(Codec.STRING.xmap(Peace::stringToV3i,Peace::v3iToString),Codec.LONG).xmap(HashMap::new, map->map)).xmap(HashMap::new, map->map)).xmap(HashMap::new, map->map).fieldOf(SAVED).orElse(new HashMap<>()).forGetter(stateSaver -> stateSaver.mp)
                    ).apply(instance,StateSaver::new));
    public StateSaver(HashMap<Identifier, HashMap<SpawnGroup, HashMap<Vec3i, Long> > > mp){this.mp=mp;}
    public StateSaver(){this(new HashMap<>());}
    private static final PersistentStateType<StateSaver> type = new PersistentStateType<>(
            RegionalPeace.MOD_ID,
            StateSaver::new,
            CODEC,
            DataFixTypes.PLAYER
    );

    public static StateSaver getServerState(MinecraftServer server) {
        return getServerState(Objects.requireNonNull(server.getWorld(World.OVERWORLD)));
    }
    //DO NOT USE THIS UNLESS DURING CONSTRUCTION OF OVERWORLD
    public static StateSaver getServerState(ServerWorld world) {
        PersistentStateManager persistentStateManager = world.getPersistentStateManager();
        StateSaver state = persistentStateManager.getOrCreate(type);
        state.markDirty();
        return state;
    }
}
