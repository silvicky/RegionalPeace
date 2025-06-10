package io.silvicky.peace.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.silvicky.peace.StateSaver;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.biome.SpawnSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static io.silvicky.peace.command.Peace.v3iToV3d;

@Mixin(SpawnHelper.class)
public class SpawnHelperMixin
{
    @Inject(method="canSpawn",at= @At("HEAD"),cancellable = true)
    private static void inject1(CallbackInfoReturnable<Boolean> cir, @Local(argsOnly = true)SpawnSettings.SpawnEntry spawnEntry, @Local(argsOnly = true) ServerWorld world, @Local(argsOnly = true) BlockPos.Mutable pos)
    {
        StateSaver stateSaver=StateSaver.getServerState(Objects.requireNonNull(world.getServer()));
        HashMap<SpawnGroup, HashMap<Vec3i, Long>> mp2=stateSaver.mp.get(world.getRegistryKey().getValue());
        if(mp2==null)return;
        HashMap<Vec3i, Long> mp3=mp2.get(spawnEntry.type().getSpawnGroup());
        if(mp3==null)return;
        for(Map.Entry<Vec3i, Long> entry:mp3.entrySet())
        {
            Vec3d center=v3iToV3d(entry.getKey());
            long radius=entry.getValue();
            double dis=center.distanceTo(pos.toBottomCenterPos());
            if(dis<=radius)
            {
                cir.setReturnValue(false);
                return;
            }
        }
    }
}
