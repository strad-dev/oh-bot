package events;

import main.Main;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class TicketCreate extends ListenerAdapter {
	public void onChannelCreate(ChannelCreateEvent e) {
		if(e.getChannel() instanceof ThreadChannel channel) {
			if(Utils.isForumChannel(channel)) {
				//System.out.println("Creating ticket in thread: " + channel.getName());
				Utils.editPost(channel, "", "open", "");
				String message = """
				**OPEN TICKET**
				TAs: To claim this post, run `!claim` in this channel.
				OP: To close this post, run `!close` in this channel.
				""";
				Message sentMessage = channel.sendMessage(message).complete();
				sentMessage.pin().queue();
				//System.out.println("Ticket created successfully!");
			}
		} else {
			//System.out.println("NOT a ThreadChannel, it's: " + e.getChannel().getClass().getSimpleName());
		}
	}
}
