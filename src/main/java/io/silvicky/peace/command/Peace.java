package io.silvicky.peace.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.silvicky.peace.StateSaver;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Peace {
    public static final String TYPE="type";
    public static final String RADIUS="radius";
    public static final String CENTER="center";
    public static final String DIMENSION="dimension";
    public static final SimpleCommandExceptionType ERR_TYPE=new SimpleCommandExceptionType(new LiteralMessage("Invalid mob type!"));
    public static final SimpleCommandExceptionType ERR_POS=new SimpleCommandExceptionType(new LiteralMessage("Invalid position!"));
    public static final SimpleCommandExceptionType ERR_DIMENSION=new SimpleCommandExceptionType(new LiteralMessage("Invalid dimension!"));
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher)
    {
        dispatcher.register(
                literal("peace")
                        .executes(context->help(context.getSource()))
                        .then(argument(TYPE, StringArgumentType.string())
                                .then(argument(RADIUS, LongArgumentType.longArg())
                                        .executes(ctx->updatePeace(StringArgumentType.getString(ctx,TYPE),LongArgumentType.getLong(ctx,RADIUS),ctx.getSource()))
                                        .then(argument(CENTER, Vec3ArgumentType.vec3())
                                                .executes(ctx->updatePeace(StringArgumentType.getString(ctx,TYPE),LongArgumentType.getLong(ctx,RADIUS),Vec3ArgumentType.getVec3(ctx,CENTER),ctx.getSource()))
                                                .then(argument(DIMENSION,DimensionArgumentType.dimension())
                                                        .executes(ctx->updatePeace(StringArgumentType.getString(ctx,TYPE),LongArgumentType.getLong(ctx,RADIUS),Vec3ArgumentType.getVec3(ctx,CENTER),DimensionArgumentType.getDimensionArgument(ctx,DIMENSION))))))));
    }
    private static int help(ServerCommandSource source)
    {
        source.sendFeedback(()-> Text.literal("Usage: /peace <type> <radius> [<center>] [<dimension>]"),false);
        source.sendFeedback(()-> Text.literal("Disable mob spawn in a sphere."),false);
        source.sendFeedback(()-> Text.literal("Set radius to negative to remove a rule."),false);
        return Command.SINGLE_SUCCESS;
    }
    public static int updatePeace(String type, long radius, ServerCommandSource source) throws CommandSyntaxException
    {
        return updatePeace(type,radius,source.getPosition(),source.getWorld());
    }
    public static int updatePeace(String type, long radius, Vec3d center, ServerCommandSource source) throws CommandSyntaxException
    {
        return updatePeace(type,radius,center,source.getWorld());
    }
    public static int updatePeace(String type, long radius, Vec3d center, ServerWorld dimension) throws CommandSyntaxException
    {
        if(center==null)throw ERR_POS.create();
        if(dimension==null)throw ERR_DIMENSION.create();
        SpawnGroup spawnGroup=null;
        for(SpawnGroup i:SpawnGroup.values())
        {
            if(i.getName().equals(type))
            {
                spawnGroup=i;
                break;
            }
        }
        if(spawnGroup==null)throw ERR_TYPE.create();
        HashMap<Identifier, HashMap<SpawnGroup, HashMap<Vec3d, Long>>> mp= StateSaver.getServerState(dimension.getServer()).mp;
        if(!mp.containsKey(dimension.getRegistryKey().getValue()))mp.put(dimension.getRegistryKey().getValue(),new HashMap<>());
        HashMap<SpawnGroup, HashMap<Vec3d, Long>> mp2=mp.get(dimension.getRegistryKey().getValue());
        if(!mp2.containsKey(spawnGroup))mp2.put(spawnGroup,new HashMap<>());
        HashMap<Vec3d, Long> mp3=mp2.get(spawnGroup);
        if(radius< 0L)
        {
            mp3.remove(center);
        }
        else
        {
            mp3.put(center,radius);
        }
        return Command.SINGLE_SUCCESS;
    }
}
