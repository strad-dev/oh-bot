package events;

import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.List;

public class TicketCreate extends ListenerAdapter {
	public void onChannelCreate(ChannelCreateEvent e) {
		if(e.getChannel() instanceof ThreadChannel channel && channel.getParentChannel().getId().equals("1414699886124728370")) {
			ForumChannel parent = channel.getParentChannel().asForumChannel();
			List<ForumTag> appliedTags = channel.getAppliedTags();
			ArrayList<ForumTag> newAppliedTags = new ArrayList<>(appliedTags);
			newAppliedTags.add(parent.getAvailableTagsByName("open", true).getFirst());
			channel.getManager().setAppliedTags(newAppliedTags).queue();

			String message = "**OPEN TICKET**\nTAs: To claim this post, run `!claim` in this channel.";
			channel.sendMessage(message).queue();
		}
	}
}