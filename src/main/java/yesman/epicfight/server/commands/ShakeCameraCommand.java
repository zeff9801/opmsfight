package yesman.epicfight.server.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.CameraShake;

public class ShakeCameraCommand implements Command<CommandSource> {
    private static final ShakeCameraCommand COMMAND = new ShakeCameraCommand();

    public ShakeCameraCommand() {
    }

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal("shake")
            .then(
                Commands.argument("time", IntegerArgumentType.integer(0, 400))
                    .then(
                        Commands.argument("amplitude", FloatArgumentType.floatArg(0.0F, 10.0F))
                            .then(Commands.argument("frequency", FloatArgumentType.floatArg(0.0F, 10.0F)).executes(COMMAND))
                    )
            );
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = EntityArgument.getPlayer(context, "player");
        int time = IntegerArgumentType.getInteger(context, "time");
        float amplitude = FloatArgumentType.getFloat(context, "amplitude");
        float frequency = FloatArgumentType.getFloat(context, "frequency");
        EpicFightNetworkManager.sendToPlayer(new CameraShake(time, amplitude, frequency), player);
        return 1;
    }
}
