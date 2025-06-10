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
import net.minecraft.util.math.Vec3i;

import java.util.HashMap;
import java.util.Map;

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
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(context->help(context.getSource()))
                        .then(argument(TYPE, StringArgumentType.string())
                                .then(argument(RADIUS, LongArgumentType.longArg())
                                        .executes(ctx->updatePeace(StringArgumentType.getString(ctx,TYPE),LongArgumentType.getLong(ctx,RADIUS),ctx.getSource()))
                                        .then(argument(CENTER, Vec3ArgumentType.vec3())
                                                .executes(ctx->updatePeace(StringArgumentType.getString(ctx,TYPE),LongArgumentType.getLong(ctx,RADIUS),Vec3ArgumentType.getVec3(ctx,CENTER),ctx.getSource()))
                                                .then(argument(DIMENSION,DimensionArgumentType.dimension())
                                                        .executes(ctx->updatePeace(StringArgumentType.getString(ctx,TYPE),LongArgumentType.getLong(ctx,RADIUS),Vec3ArgumentType.getVec3(ctx,CENTER),DimensionArgumentType.getDimensionArgument(ctx,DIMENSION),ctx.getSource()))))))
                        .then(literal("list")
                                .executes(ctx->listPeace(null,null,ctx.getSource()))
                                .then(argument(TYPE, StringArgumentType.string())
                                        .executes(ctx->listPeace(StringArgumentType.getString(ctx,TYPE),null,ctx.getSource()))
                                        .then(argument(DIMENSION,DimensionArgumentType.dimension())
                                                .executes(ctx->listPeace(StringArgumentType.getString(ctx,TYPE),DimensionArgumentType.getDimensionArgument(ctx,DIMENSION),ctx.getSource()))))
                                .then(argument(DIMENSION,DimensionArgumentType.dimension())
                                        .executes(ctx->listPeace(null,DimensionArgumentType.getDimensionArgument(ctx,DIMENSION),ctx.getSource())))));
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
        return updatePeace(type,radius,source.getPosition(),source.getWorld(),source);
    }
    public static int updatePeace(String type, long radius, Vec3d center, ServerCommandSource source) throws CommandSyntaxException
    {
        return updatePeace(type,radius,center,source.getWorld(),source);
    }
    public static SpawnGroup getSpawnGroup(String type)
    {
        if(type==null)return null;
        for(SpawnGroup i:SpawnGroup.values())
        {
            if(i.getName().equals(type))
            {
                return i;
            }
        }
        return null;
    }
    public static Vec3i v3dToV3i(Vec3d vec3d)
    {
        return new Vec3i((int) Math.floor(vec3d.x), (int) Math.floor(vec3d.y), (int) Math.floor(vec3d.z));
    }
    public static Vec3d v3iToV3d(Vec3i vec3i)
    {
        return new Vec3d(vec3i.getX()+0.5,vec3i.getY()+0.5,vec3i.getZ()+0.5);
    }
    public static String v3iToString(Vec3i vec3i)
    {
        return vec3i.getX()+","+ vec3i.getY()+","+vec3i.getZ();
    }
    public static Vec3i stringToV3i(String s)
    {
        String[] t=s.split(",");
        if(t.length<3)throw new NumberFormatException(s);
        return new Vec3i(Integer.parseInt(t[0]),Integer.parseInt(t[1]),Integer.parseInt(t[2]));
    }
    public static int updatePeace(String type, long radius, Vec3d center, ServerWorld dimension, ServerCommandSource source) throws CommandSyntaxException
    {
        if(center==null)throw ERR_POS.create();
        if(dimension==null)throw ERR_DIMENSION.create();
        SpawnGroup spawnGroup=getSpawnGroup(type);
        if(spawnGroup==null)throw ERR_TYPE.create();
        HashMap<Identifier, HashMap<SpawnGroup, HashMap<Vec3i, Long>>> mp= StateSaver.getServerState(dimension.getServer()).mp;
        if(!mp.containsKey(dimension.getRegistryKey().getValue()))mp.put(dimension.getRegistryKey().getValue(),new HashMap<>());
        HashMap<SpawnGroup, HashMap<Vec3i, Long>> mp2=mp.get(dimension.getRegistryKey().getValue());
        if(!mp2.containsKey(spawnGroup))mp2.put(spawnGroup,new HashMap<>());
        HashMap<Vec3i, Long> mp3=mp2.get(spawnGroup);
        Vec3i center2=v3dToV3i(center);
        if(radius< 0L)
        {
            mp3.remove(center2);
        }
        else
        {
            mp3.put(center2,radius);
        }
        source.sendFeedback(()-> Text.literal("Done."),false);
        return Command.SINGLE_SUCCESS;
    }
    public static int listPeace(String type, ServerWorld dimension, ServerCommandSource source)
    {
        source.sendFeedback(()-> Text.literal("Current peace ranges:"),false);
        SpawnGroup spawnGroup=getSpawnGroup(type);
        HashMap<Identifier, HashMap<SpawnGroup, HashMap<Vec3i, Long>>> mp= StateSaver.getServerState(source.getServer()).mp;
        for(Map.Entry<Identifier, HashMap<SpawnGroup, HashMap<Vec3i, Long>>> entry:mp.entrySet())
        {
            if(dimension!=null&&!entry.getKey().equals(dimension.getRegistryKey().getValue()))continue;
            for(Map.Entry<SpawnGroup, HashMap<Vec3i, Long>> entry1:entry.getValue().entrySet())
            {
                if(spawnGroup!=null&&!entry1.getKey().equals(spawnGroup))continue;
                for(Map.Entry<Vec3i, Long> entry2:entry1.getValue().entrySet())
                {
                    source.sendFeedback(()-> Text.literal(entry.getKey()+" "+entry1.getKey().asString()+" "+entry2.getKey()+" "+entry2.getValue()),false);
                }
            }
        }
        return Command.SINGLE_SUCCESS;
    }
}
