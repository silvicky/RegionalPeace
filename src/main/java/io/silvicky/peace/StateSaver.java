package io.silvicky.peace;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.silvicky.peace.command.Peace;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;

public class StateSaver extends SavedData
{
    private static boolean checkMigrate=true;
    private static final Identifier id=Identifier.fromNamespaceAndPath("silvicky",RegionalPeace.MOD_ID);
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
            id,
            StateSaver::new,
            CODEC,
            DataFixTypes.PLAYER
    );
    private static void migrate(MinecraftServer server)
    {
        Path root= server.getDataStorage().dataFolder;
        Path oldPath=root.resolve("RegionalPeace.dat");
        Path newPath=id.withSuffix(".dat").resolveAgainst(root);
        newPath.getParent().toFile().mkdir();
        if(oldPath.toFile().exists())
        {
            try
            {
                Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
            }
            catch(Exception e){throw new RuntimeException(e);}
        }
    }
    public static StateSaver getServerState(MinecraftServer server) {
        if(checkMigrate)
        {
            migrate(server);
            checkMigrate=false;
        }
        SavedDataStorage persistentStateManager = server.getDataStorage();
        StateSaver state = persistentStateManager.computeIfAbsent(type);
        state.setDirty();
        return state;
    }
}
