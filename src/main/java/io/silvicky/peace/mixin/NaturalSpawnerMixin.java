package io.silvicky.peace.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.silvicky.peace.StateSaver;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.MobSpawnSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static io.silvicky.peace.command.Peace.v3iToV3d;

@Mixin(NaturalSpawner.class)
public class NaturalSpawnerMixin
{
    @Inject(method= "isValidSpawnPostitionForType",at= @At("HEAD"),cancellable = true)
    private static void inject1(CallbackInfoReturnable<Boolean> cir, @Local(argsOnly = true) MobSpawnSettings.SpawnerData spawnEntry, @Local(argsOnly = true) ServerLevel world, @Local(argsOnly = true) BlockPos.MutableBlockPos pos)
    {
        StateSaver stateSaver=StateSaver.getServerState(Objects.requireNonNull(world.getServer()));
        HashMap<MobCategory, HashMap<Vec3i, Long>> mp2=stateSaver.mp.get(world.dimension().identifier());
        if(mp2==null)return;
        HashMap<Vec3i, Long> mp3=mp2.get(spawnEntry.type().getCategory());
        if(mp3==null)return;
        for(Map.Entry<Vec3i, Long> entry:mp3.entrySet())
        {
            Vec3 center=v3iToV3d(entry.getKey());
            long radius=entry.getValue();
            double dis=center.distanceTo(pos.getBottomCenter());
            if(dis<=radius)
            {
                cir.setReturnValue(false);
                return;
            }
        }
    }
}
