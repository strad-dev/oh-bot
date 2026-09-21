package events;

import main.Main;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class TicketCreate extends ListenerAdapter {
	public void onMessageReceived(MessageReceivedEvent e) {
		if(e.getChannel() instanceof ThreadChannel thread && Utils.isForumChannel(thread) && e.getMessageId().equals(thread.getId())) {
			if(Utils.isForumChannel(thread)) {
				Utils.editPost(thread, "", "open", "");
				String message = """
				**OPEN TICKET**
				TAs: To claim this post, run `!claim` in this channel.
				OP: To close this post, run `!close` in this channel.
				""";
				Message sentMessage = thread.sendMessage(message).complete();
				sentMessage.pin().queue();
			}
		}
	}
}
