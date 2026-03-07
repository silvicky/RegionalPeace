package io.silvicky.peace.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.silvicky.peace.StateSaver;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Vec3i;

import java.util.HashMap;
import java.util.Map;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class Peace {
    public static final String TYPE="type";
    public static final String RADIUS="radius";
    public static final String CENTER="center";
    public static final String DIMENSION="dimension";
    public static final SimpleCommandExceptionType ERR_TYPE=new SimpleCommandExceptionType(new LiteralMessage("Invalid mob type!"));
    public static final SimpleCommandExceptionType ERR_POS=new SimpleCommandExceptionType(new LiteralMessage("Invalid position!"));
    public static final SimpleCommandExceptionType ERR_DIMENSION=new SimpleCommandExceptionType(new LiteralMessage("Invalid dimension!"));
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        dispatcher.register(
                literal("peace")
                        .requires(source -> source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.GAMEMASTERS)))
                        .executes(context->help(context.getSource()))
                        .then(argument(TYPE, StringArgumentType.string())
                                .then(argument(RADIUS, LongArgumentType.longArg())
                                        .executes(ctx->updatePeace(StringArgumentType.getString(ctx,TYPE),LongArgumentType.getLong(ctx,RADIUS),ctx.getSource()))
                                        .then(argument(CENTER, Vec3Argument.vec3())
                                                .executes(ctx->updatePeace(StringArgumentType.getString(ctx,TYPE),LongArgumentType.getLong(ctx,RADIUS), Vec3Argument.getVec3(ctx,CENTER),ctx.getSource()))
                                                .then(argument(DIMENSION, DimensionArgument.dimension())
                                                        .executes(ctx->updatePeace(StringArgumentType.getString(ctx,TYPE),LongArgumentType.getLong(ctx,RADIUS), Vec3Argument.getVec3(ctx,CENTER), DimensionArgument.getDimension(ctx,DIMENSION),ctx.getSource()))))))
                        .then(literal("list")
                                .executes(ctx->listPeace(null,null,ctx.getSource()))
                                .then(argument(TYPE, StringArgumentType.string())
                                        .executes(ctx->listPeace(StringArgumentType.getString(ctx,TYPE),null,ctx.getSource()))
                                        .then(argument(DIMENSION, DimensionArgument.dimension())
                                                .executes(ctx->listPeace(StringArgumentType.getString(ctx,TYPE), DimensionArgument.getDimension(ctx,DIMENSION),ctx.getSource()))))
                                .then(argument(DIMENSION, DimensionArgument.dimension())
                                        .executes(ctx->listPeace(null, DimensionArgument.getDimension(ctx,DIMENSION),ctx.getSource())))));
    }
    private static int help(CommandSourceStack source)
    {
        source.sendSuccess(()-> Component.literal("Usage: /peace <type> <radius> [<center>] [<dimension>]"),false);
        source.sendSuccess(()-> Component.literal("Disable mob spawn in a sphere."),false);
        source.sendSuccess(()-> Component.literal("Set radius to negative to remove a rule."),false);
        return Command.SINGLE_SUCCESS;
    }
    public static int updatePeace(String type, long radius, CommandSourceStack source) throws CommandSyntaxException
    {
        return updatePeace(type,radius,source.getPosition(),source.getLevel(),source);
    }
    public static int updatePeace(String type, long radius, Vec3 center, CommandSourceStack source) throws CommandSyntaxException
    {
        return updatePeace(type,radius,center,source.getLevel(),source);
    }
    public static MobCategory getMobCategory(String type)
    {
        if(type==null)return null;
        for(MobCategory i:MobCategory.values())
        {
            if(i.getName().equals(type))
            {
                return i;
            }
        }
        return null;
    }
    public static Vec3i v3dToV3i(Vec3 vec3d)
    {
        return new Vec3i((int) Math.floor(vec3d.x), (int) Math.floor(vec3d.y), (int) Math.floor(vec3d.z));
    }
    public static Vec3 v3iToV3d(Vec3i vec3i)
    {
        return new Vec3(vec3i.getX()+0.5,vec3i.getY()+0.5,vec3i.getZ()+0.5);
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
    public static int updatePeace(String type, long radius, Vec3 center, ServerLevel dimension, CommandSourceStack source) throws CommandSyntaxException
    {
        if(center==null)throw ERR_POS.create();
        if(dimension==null)throw ERR_DIMENSION.create();
        MobCategory spawnGroup=getMobCategory(type);
        if(spawnGroup==null)throw ERR_TYPE.create();
        HashMap<Identifier, HashMap<MobCategory, HashMap<Vec3i, Long>>> mp= StateSaver.getServerState(dimension.getServer()).mp;
        if(!mp.containsKey(dimension.dimension().identifier()))mp.put(dimension.dimension().identifier(),new HashMap<>());
        HashMap<MobCategory, HashMap<Vec3i, Long>> mp2=mp.get(dimension.dimension().identifier());
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
        source.sendSuccess(()-> Component.literal("Done."),false);
        return Command.SINGLE_SUCCESS;
    }
    public static int listPeace(String type, ServerLevel dimension, CommandSourceStack source)
    {
        source.sendSuccess(()-> Component.literal("Current peace ranges:"),false);
        MobCategory spawnGroup=getMobCategory(type);
        HashMap<Identifier, HashMap<MobCategory, HashMap<Vec3i, Long>>> mp= StateSaver.getServerState(source.getServer()).mp;
        for(Map.Entry<Identifier, HashMap<MobCategory, HashMap<Vec3i, Long>>> entry:mp.entrySet())
        {
            if(dimension!=null&&!entry.getKey().equals(dimension.dimension().identifier()))continue;
            for(Map.Entry<MobCategory, HashMap<Vec3i, Long>> entry1:entry.getValue().entrySet())
            {
                if(spawnGroup!=null&&!entry1.getKey().equals(spawnGroup))continue;
                for(Map.Entry<Vec3i, Long> entry2:entry1.getValue().entrySet())
                {
                    source.sendSuccess(()-> Component.literal(entry.getKey()+" "+entry1.getKey().name()+" "+entry2.getKey()+" "+entry2.getValue()),false);
                }
            }
        }
        return Command.SINGLE_SUCCESS;
    }
}
