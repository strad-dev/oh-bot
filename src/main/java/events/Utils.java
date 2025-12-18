package events;

import main.Main;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class Utils {
	/**
	 * Retrieves a ForumTag by its name from the given ForumChannel.
	 *
	 * @param name    the name of the tag to retrieve
	 * @param channel the ForumChannel from which to retrieve the tag
	 * @return the matching ForumTag, or null if no matching tag is found
	 */
	public static ForumTag getTagByName(String name, ForumChannel channel) {
		return channel.getAvailableTagsByName(name, true).getFirst();
	}

	/**
	 * Updates the tags associated with a given thread channel by removing a specified tag
	 * and adding a new one. This method modifies the thread's applied tags list and ensures the changes
	 * are reflected in the channel by applying these updates through its manager.
	 *
	 * @param channel the thread channel whose tags are being modified
	 * @param remove  the name of the tag to be removed from the thread channel
	 * @param add     the name of the tag to be added to the thread channel
	 * @param newName the new name for the thread channel
	 */
	public static void editPost(ThreadChannel channel, String remove, String add, String newName) {
		ForumChannel parent = channel.getParentChannel().asForumChannel();
		List<ForumTag> appliedTags = channel.getAppliedTags();
		ArrayList<ForumTag> newAppliedTags = new ArrayList<>(appliedTags);
		if(!remove.isEmpty()) {
			newAppliedTags.remove(Utils.getTagByName(remove, parent));
		}

		if(!add.isEmpty()) {
			newAppliedTags.add(Utils.getTagByName(add, parent));
		}

		String name = channel.getName();
		if(!newName.isEmpty()) {
			name = newName;
		}
		if(name.length() > 100) {
			name = name.substring(0, 97) + "...";
		}
		channel.getManager().setAppliedTags(newAppliedTags).setName(name).queue();
	}

	/**
	 * Checks if a given member has ADMINISTRATOR permissions.
	 * <br>
	 * @param member The Member in question
	 * @return Whether the Member has Admin perms
	 */
	public static boolean isAdmin(Member member) {
		return member.getPermissions().contains(Permission.ADMINISTRATOR);
	}

	/**
	 * Determines if a given member is the owner (OP) of a specified thread channel.
	 * <br>
	 * This method checks if the ID of the owner of the thread channel matches the ID of the provided member.
	 *
	 * @param channel the ThreadChannel to check ownership against
	 * @param member  the Member whose ownership is being verified
	 * @return true if the member is the owner of the thread channel, false otherwise
	 */
	public static boolean isOP(ThreadChannel channel, Member member) {
		return channel.getOwnerId().equals(member.getId());
	}

	/**
	 * Gets the pinned bot message of this channel.
	 *
	 * @param channel  The channel to get the bot message from
	 * @param fallback The String that will be pinned if the bot message is not found
	 * @return The bot message, or null if it is not found
	 */
	public static String getBotMessage(ThreadChannel channel, String fallback) {
		try {
			return channel.retrievePinnedMessages().complete().getFirst().getContentRaw();
		} catch(NoSuchElementException exception) {
			Message sentMessage = channel.sendMessage(fallback).complete();
			sentMessage.pin().queue();
			return null;
		}
	}

	/**
	 * Edits the bot's pinned message
	 *
	 * @param channel Channel to edit the bot message from
	 * @param newMessage The new message content
	 */
	public static void editBotMessage(ThreadChannel channel, String newMessage) {
		Message botMessage;
		try {
			botMessage = channel.retrievePinnedMessages().complete().getFirst();
			botMessage.editMessage(newMessage).queue();
		} catch(NoSuchElementException exception) {
			botMessage = channel.sendMessage(newMessage).complete();
			botMessage.pin().queue();
		}
	}

	/**
	 * Checks if a given Object is equal to the forum channel
	 *
	 * @param object The Object in question
	 * @return If the Object is the forum channel
	 */
	public static boolean isForumChannel(Object object) {
		if(!(object instanceof ThreadChannel)) {
			return false;
		}
		return Main.getForumChannelID().equals(((ThreadChannel) object).getId());
	}

	/**
	 * Checks if a given Object is equal to the Mod role
	 *
	 * @param object The Object in question
	 * @return If the Object is the Mod role
	 */
	public static boolean isModRole(Object object) {
		if(!(object instanceof Role)) {
			return false;
		}

		return Main.getModRoleID().equals(((Role) object).getId());
	}

	/**
	 * Checks if a Member is a moderator for the forum
	 *
	 * @param member The Member in question
	 * @return If the Member is a moderator
	 */
	public static boolean isMod(Member member) {
		return member.getRoles().stream().anyMatch(role -> role.getId().equals(Main.getModRoleID()));
	}
}