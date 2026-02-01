package org.cloudburstmc.protocol.bedrock.codec.v898.serializer;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.longs.LongObjectPair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.v594.serializer.AvailableCommandsSerializer_v594;
import org.cloudburstmc.protocol.bedrock.data.command.*;
import org.cloudburstmc.protocol.common.util.LongKeys;
import org.cloudburstmc.protocol.common.util.TypeMap;
import org.cloudburstmc.protocol.common.util.VarInts;

import java.util.*;
import java.util.function.Consumer;

import static org.cloudburstmc.protocol.common.util.Preconditions.checkArgument;

public class AvailableCommandsSerializer_v898 extends AvailableCommandsSerializer_v594 {

    private static final List<String> PERMISSION_LEVEL = Arrays.asList("any", "gamedirectors", "admin", "host", "owner", "internal");

    public AvailableCommandsSerializer_v898(TypeMap<CommandParam> paramTypeMap) {
        super(paramTypeMap);
    }

    @Override
    protected void writeCommand(ByteBuf buffer, BedrockCodecHelper helper, CommandData commandData,
                                List<CommandEnumData> enums, List<CommandEnumData> softEnums, List<String> postFixes, List<ChainedSubCommandData> subCommands) {
        helper.writeString(buffer, commandData.getName());
        helper.writeString(buffer, commandData.getDescription());
        this.writeFlags(buffer, commandData.getFlags());
        CommandPermission permission = commandData.getPermission() == null ? CommandPermission.ANY : commandData.getPermission();
        helper.writeString(buffer, PERMISSION_LEVEL.get(permission.ordinal()));

        CommandEnumData aliases = commandData.getAliases();
        buffer.writeIntLE(aliases == null ? -1 : enums.indexOf(aliases));

        helper.writeArray(buffer, commandData.getSubcommands(), (buf, subcommand) -> {
            int index = subCommands.indexOf(subcommand);
            checkArgument(index > -1, "Invalid subcommand index: " + subcommand);
            buf.writeShortLE(index);
        });

        CommandOverloadData[] overloads = commandData.getOverloads();
        VarInts.writeUnsignedInt(buffer, overloads.length);
        for (CommandOverloadData overload : overloads) {
            buffer.writeBoolean(overload.isChaining());
            VarInts.writeUnsignedInt(buffer, overload.getOverloads().length);
            for (CommandParamData param : overload.getOverloads()) {
                this.writeParameter(buffer, helper, param, enums, softEnums, postFixes);
            }
        }
    }

    @Override
    protected CommandData readCommand(ByteBuf buffer, BedrockCodecHelper helper, List<CommandEnumData> enums,
                                      List<String> postfixes, Set<Consumer<List<CommandEnumData>>> softEnumParameters, List<ChainedSubCommandData> subCommandsList) {
        String name = helper.readString(buffer);
        String description = helper.readString(buffer);
        Set<CommandData.Flag> flags = this.readFlags(buffer);
        CommandPermission permissions = PERMISSIONS[PERMISSION_LEVEL.indexOf(helper.readString(buffer))];
        int aliasIndex = buffer.readIntLE();
        CommandEnumData aliases = aliasIndex == -1 ? null : enums.get(aliasIndex);

        List<ChainedSubCommandData> subcommands = new ObjectArrayList<>();
        helper.readArray(buffer, subcommands, (buf, help) -> {
            int index = Math.toIntExact(buf.readUnsignedIntLE());
            return subCommandsList.get(index);
        });

        CommandOverloadData[] overloads = new CommandOverloadData[VarInts.readUnsignedInt(buffer)];
        for (int i = 0; i < overloads.length; i++) {
            boolean chaining = buffer.readBoolean();
            CommandParamData[] params = new CommandParamData[VarInts.readUnsignedInt(buffer)];
            overloads[i] = new CommandOverloadData(chaining, params);
            for (int i2 = 0; i2 < params.length; i2++) {
                params[i2] = readParameter(buffer, helper, enums, postfixes, softEnumParameters);
            }
        }
        return new CommandData(name, description, flags, permissions, aliases, subcommands, overloads);
    }

    @Override
    protected void writeEnums(ByteBuf buffer, BedrockCodecHelper helper, List<String> values, List<CommandEnumData> enums) {
        helper.writeArray(buffer, enums, (buf, commandEnum) -> {
            helper.writeString(buf, commandEnum.getName());

            VarInts.writeUnsignedInt(buffer, commandEnum.getValues().size());
            for (String value : commandEnum.getValues().keySet()) {
                int index = values.indexOf(value);
                checkArgument(index > -1, "Invalid enum value detected: " + value);
                buffer.writeIntLE(index);
            }
        });
    }

    @Override
    protected void readEnums(ByteBuf buffer, BedrockCodecHelper helper, List<String> values, List<CommandEnumData> enums) {
        helper.readArray(buffer, enums, buf -> {
            String name = helper.readString(buf);

            int length = VarInts.readUnsignedInt(buffer);
            LinkedHashMap<String, Set<CommandEnumConstraint>> enumValues = new LinkedHashMap<>();
            for (int i = 0; i < length; i++) {
                enumValues.put(values.get((int) buf.readUnsignedIntLE()), EnumSet.noneOf(CommandEnumConstraint.class));
            }
            return new CommandEnumData(name, enumValues, false);
        });
    }

    @Override
    protected void writeSubCommand(ByteBuf buffer, BedrockCodecHelper helper, List<String> values, ChainedSubCommandData data) {
        helper.writeString(buffer, data.getName());
        helper.writeArray(buffer, data.getValues(), (buf, val) -> {
            int first = values.indexOf(val.getFirst());
            checkArgument(first > -1, "Invalid enum value detected: " + val.getFirst());

            int second = values.indexOf(val.getSecond());
            checkArgument(second > -1, "Invalid enum value detected: " + val.getSecond());

            VarInts.writeUnsignedInt(buf, first);
            VarInts.writeUnsignedInt(buf, second);
        });
    }

    @Override
    protected ChainedSubCommandData readSubCommand(ByteBuf buffer, BedrockCodecHelper helper, List<String> values) {
        String name = helper.readString(buffer);
        ChainedSubCommandData data = new ChainedSubCommandData(name);

        helper.readArray(buffer, data.getValues(), buf -> {
            int first = VarInts.readUnsignedInt(buf);
            int second = VarInts.readUnsignedInt(buf);
            return new ChainedSubCommandData.Value(values.get(first), values.get(second));
        });
        return data;
    }

    @Override
    protected void writeEnumConstraint(ByteBuf buffer, BedrockCodecHelper helper, LongObjectPair<Set<CommandEnumConstraint>> pair) {
        buffer.writeIntLE(LongKeys.high(pair.keyLong()));
        buffer.writeIntLE(LongKeys.low(pair.keyLong()));
        helper.writeArray(buffer, pair.value(), (buf, constraint) -> buf.writeByte(constraint.ordinal()));
    }

    @Override
    protected void readConstraints(ByteBuf buffer, BedrockCodecHelper helper, List<CommandEnumData> enums,
                                   List<String> enumValues) {
        int count = VarInts.readUnsignedInt(buffer);
        for (int i = 0; i < count; i++) {
            String key = enumValues.get((int) buffer.readUnsignedIntLE());
            CommandEnumData enumData = enums.get((int) buffer.readUnsignedIntLE());
            Set<CommandEnumConstraint> constraints = enumData.getValues().get(key);
            helper.readArray(buffer, constraints, buf -> CONSTRAINTS[buf.readUnsignedByte()]);
        }
    }

    @Override
    protected CommandParamData readParameter(ByteBuf buffer, BedrockCodecHelper helper, List<CommandEnumData> enums,
                                             List<String> postfixes, Set<Consumer<List<CommandEnumData>>> softEnumParameters) {
        CommandParamData param = new CommandParamData();

        param.setName(helper.readString(buffer));

        int symbol = (int) buffer.readUnsignedIntLE(); // uint
        if ((symbol & ARG_FLAG_POSTFIX) != 0) {
            param.setPostfix(postfixes.get(symbol & ~ARG_FLAG_POSTFIX));
        } else if ((symbol & ARG_FLAG_VALID) != 0) {
            if ((symbol & ARG_FLAG_SOFT_ENUM) != 0) {
                softEnumParameters.add((softEnums) -> param.setEnumData(softEnums.get(symbol & ~(ARG_FLAG_SOFT_ENUM | ARG_FLAG_VALID))));
            } else if ((symbol & ARG_FLAG_ENUM) != 0) {
                param.setEnumData(enums.get(symbol & ~(ARG_FLAG_ENUM | ARG_FLAG_VALID)));
            } else {
                int parameterTypeId = symbol & ~ARG_FLAG_VALID;
                CommandParam type = paramTypeMap.getTypeUnsafe(parameterTypeId);
                if (type == null) {
                    throw new IllegalStateException("Invalid parameter type: " + parameterTypeId + ", Symbol: " + symbol);
                }
                param.setType(type);
            }
        } else {
            throw new IllegalStateException("No param type specified: " + param.getName());
        }

        param.setOptional(buffer.readBoolean());

        Set<CommandParamOption> options = param.getOptions();
        int optionsBits = buffer.readUnsignedByte();

        for (CommandParamOption option : OPTIONS) {
            if ((optionsBits & 1 << option.ordinal()) != 0) {
                options.add(option);
            }
        }
        return param;
    }
}
