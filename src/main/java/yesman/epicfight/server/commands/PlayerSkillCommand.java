package yesman.epicfight.server.commands;

import java.util.Collection;
import java.util.Locale;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.EntitySelector;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPChangeSkill;
import yesman.epicfight.network.server.SPClearSkills;
import yesman.epicfight.network.server.SPRemoveSkill;
import yesman.epicfight.server.commands.arguments.SkillArgument;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class PlayerSkillCommand {
	private static final SimpleCommandExceptionType ERROR_ADD_FAILED = new SimpleCommandExceptionType(new TranslationTextComponent("commands.epicfight.skill.add.failed"));
	private static final SimpleCommandExceptionType ERROR_REMOVE_FAILED = new SimpleCommandExceptionType(new TranslationTextComponent("commands.epicfight.skill.remove.failed"));
	private static final SimpleCommandExceptionType ERROR_CLEAR_FAILED = new SimpleCommandExceptionType(new TranslationTextComponent("commands.epicfight.skill.clear.failed"));

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		RequiredArgumentBuilder<CommandSource, EntitySelector> addCommandBuilder = Commands.argument("targets", EntityArgument.players());
		RequiredArgumentBuilder<CommandSource, EntitySelector> removeCommandBuilder = Commands.argument("targets", EntityArgument.players());

		for (SkillSlot skillSlot : SkillSlot.ENUM_MANAGER.universalValues()) {
			if (skillSlot.category().learnable()) {
				addCommandBuilder
						.then(Commands.literal(skillSlot.toString().toLowerCase(Locale.ROOT))
								.then(Commands.argument("skill", SkillArgument.skill())
										.executes((commandContext) -> {
											return addSkill(commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), skillSlot, SkillArgument.getSkill(commandContext, "skill"));
										})));

				removeCommandBuilder
						.then(Commands.literal(skillSlot.toString().toLowerCase(Locale.ROOT))
								.executes((commandContext) -> {
									return removeSkill(commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), skillSlot, null);
								})
								.then(Commands.argument("skill", SkillArgument.skill())
										.executes((commandContext) -> {
											return removeSkill(commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), skillSlot, SkillArgument.getSkill(commandContext, "skill"));
										})));
			}
		}

		LiteralArgumentBuilder<CommandSource> builder = Commands.literal("skill").requires((commandSourceStack) -> commandSourceStack.hasPermission(2))
				.then(Commands.literal("clear").executes((commandContext) -> {
							return clearSkill(commandContext.getSource(), ImmutableList.of(commandContext.getSource().getPlayerOrException()));
						})
						.then(Commands.argument("targets", EntityArgument.players()).executes((commandContext) -> {
							return clearSkill(commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"));
						})))
				.then(Commands.literal("add")
						.then(addCommandBuilder))
				.then(Commands.literal("remove")
						.then(removeCommandBuilder));

		dispatcher.register(Commands.literal("epicfight").then(builder));
	}

	public static int clearSkill(CommandSource commandSourceStack, Collection<? extends ServerPlayerEntity> targets) throws CommandSyntaxException {
		int i = 0;

		for (ServerPlayerEntity player : targets) {
			ServerPlayerPatch playerpatch = EpicFightCapabilities.getEntityPatch(player, ServerPlayerPatch.class);
			playerpatch.getSkillCapability().clear();
			EpicFightNetworkManager.sendToPlayer(new SPClearSkills(), player);
			i++;
		}

		if (i > 0) {
			if (i == 1) {
				commandSourceStack.sendSuccess((new TranslationTextComponent("commands.epicfight.skill.clear.success.single", targets.iterator().next().getDisplayName())), true);
			} else {
				commandSourceStack.sendSuccess((new TranslationTextComponent("commands.epicfight.skill.clear.success.multiple", i)), true);
			}
		} else {
			throw ERROR_CLEAR_FAILED.create();
		}

		return i;
	}

	public static int addSkill(CommandSource commandSourceStack, Collection<? extends ServerPlayerEntity> targets, SkillSlot slot, Skill skill) throws CommandSyntaxException {
		int i = 0;

		for (ServerPlayerEntity player : targets) {
			ServerPlayerPatch playerpatch = EpicFightCapabilities.getEntityPatch(player, ServerPlayerPatch.class);

			if (playerpatch.getSkillCapability().skillContainers[slot.universalOrdinal()].setSkill(skill)) {
				if (skill.getCategory().learnable()) {
					playerpatch.getSkillCapability().addLearnedSkill(skill);
				}

				EpicFightNetworkManager.sendToPlayer(new SPChangeSkill(slot, skill.toString(), SPChangeSkill.State.ENABLE), player);
				i++;
			}
		}

		if (i > 0) {
			if (i == 1) {
				commandSourceStack.sendSuccess((new TranslationTextComponent("commands.epicfight.skill.add.success.single", skill.getDisplayName(), targets.iterator().next().getDisplayName())), true);
			} else {
				commandSourceStack.sendSuccess((new TranslationTextComponent("commands.epicfight.skill.add.success.multiple", skill.getDisplayName(), i)), true);
			}
		} else {
			throw ERROR_ADD_FAILED.create();
		}

		return i;
	}

	public static int removeSkill(CommandSource commandSourceStack, Collection<? extends ServerPlayerEntity> targets, SkillSlot slot, Skill skill) throws CommandSyntaxException {
		int i = 0;

		for (ServerPlayerEntity player : targets) {
			ServerPlayerPatch playerpatch = EpicFightCapabilities.getEntityPatch(player, ServerPlayerPatch.class);

			if (playerpatch != null) {
				if (skill == null) {
					SkillContainer skillContainer = playerpatch.getSkill(slot);
					skill = skillContainer.getSkill();

					if (skill != null) {
						skillContainer.setSkill(null);
						EpicFightNetworkManager.sendToPlayer(new SPRemoveSkill(skill.toString(), slot), player);
						i++;
					}
				} else {
					if (playerpatch.getSkillCapability().removeLearnedSkill(skill)) {
						SkillContainer skillContainer = playerpatch.getSkill(slot);

						if (skillContainer.getSkill() == skill) {
							skillContainer.setSkill(null);
							EpicFightNetworkManager.sendToPlayer(new SPRemoveSkill(skill.toString(), slot), player);
							i++;
						}
					}
				}
			}
		}

		if (i > 0) {
			if (i == 1) {
				commandSourceStack.sendSuccess((new TranslationTextComponent("commands.epicfight.skill.remove.success.single", skill.getDisplayName(), targets.iterator().next().getDisplayName())), true);
			} else {
				commandSourceStack.sendSuccess((new TranslationTextComponent("commands.epicfight.skill.remove.success.multiple", skill.getDisplayName(), i)), true);
			}
		} else {
			throw PlayerSkillCommand.ERROR_REMOVE_FAILED.create();
		}

		return i;
	}
}

