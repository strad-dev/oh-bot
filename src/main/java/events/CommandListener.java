package events;

import main.Main;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

public class CommandListener extends ListenerAdapter {
	private static boolean isAnalysisRunning = false;

	@SuppressWarnings("DataFlowIssue")
	public void onMessageReceived(MessageReceivedEvent e) {
		if(!e.getAuthor().isBot() && e.getMessage().getContentRaw().startsWith("!")) {
			String[] message = e.getMessage().getContentRaw().toLowerCase().substring(1).split(" ");
			if(e.getChannel() instanceof ThreadChannel threadChannel && Utils.isForumChannel(threadChannel)) {
				ForumChannel parent = threadChannel.getParentChannel().asForumChannel();
				try {
					switch(message[0]) {
						case "claim" -> {
							if(Utils.isMod(e.getMember())) {
								if(threadChannel.getAppliedTags().contains(Utils.getTagByName("open", parent))) {
									// apply correct tag + rename channel to include the TA name
									String nickname = e.getMember().getNickname();
									if(nickname == null) {
										nickname = e.getMember().getUser().getEffectiveName();
									}
									Utils.editPost(threadChannel, "open", "claimed", "[TA: " + nickname + "] " + threadChannel.getName());

									// edit bot's message
									String newMessage = """
											**CLAIMED TICKET**
											<@%s> has claimed this ticket.

											TAs: To unclaim this ticket, run `!unclaim` in this channel.
											TAs and OP: To close this ticket, run `!close` in this channel.

											%s
											""".formatted(e.getAuthor().getId(), e.getAuthor().getId());
									Utils.editBotMessage(threadChannel, newMessage);

									// feedback message
									e.getMessage().reply("You have successfully claimed this ticket!").mentionRepliedUser(false).queue();
								} else {
									e.getMessage().reply("This ticket has already been claimed, or is closed.").mentionRepliedUser(false).queue();
								}
							} else {
								e.getMessage().reply(":no_entry: **403 FORBIDDEN** :no_entry:\nYou do not have permission to run this command.").mentionRepliedUser(false).queue();
							}
						}
						case "unclaim" -> {
							if(Utils.isMod(e.getMember())) {
								if(threadChannel.getAppliedTags().contains(Utils.getTagByName("claimed", parent))) {
									// check if the person who claimed the ticket is the person who is running the command
									String newMessage = """
											**OPEN TICKET**
											TAs: To claim this post, run `!claim` in this channel.
											OP: To close this post, run `!close` in this channel.
											""";
									String botMessage = Utils.getBotMessage(threadChannel, newMessage);
									if(botMessage == null) {
										e.getMessage().reply("Oops!  My original message got deleted!  This ticket is now unclaimed.").mentionRepliedUser(false).queue();
										return;
									}
									String[] botMessageSplit = botMessage.split("\n");
									if(botMessageSplit[botMessageSplit.length - 1].equals(e.getAuthor().getId()) || Utils.isAdmin(e.getMember())) {
										// apply correct tag + remove TA name
										String currentName = threadChannel.getName();
										String newName = currentName.replaceFirst("^\\[TA: [^]]+] ", "");
										Utils.editPost(threadChannel, "claimed", "open", newName);

										// edit bot's message
										Utils.editBotMessage(threadChannel, newMessage);

										// feedback message
										e.getMessage().reply("You have successfully unclaimed this ticket!").mentionRepliedUser(false).queue();
									} else {
										e.getMessage().reply("You are not the person who claimed this ticket!").mentionRepliedUser(false).queue();
									}
								} else {
									e.getMessage().reply("This ticket is currently unclaimed or closed.").mentionRepliedUser(false).queue();
								}
							} else {
								e.getMessage().reply(":no_entry: **403 FORBIDDEN** :no_entry:\nYou do not have permission to run this command.").mentionRepliedUser(false).queue();
							}
						}
						case "close" -> {
							if(Utils.isMod(e.getMember()) || Utils.isOP(threadChannel, e.getMember())) {
								if(!threadChannel.getAppliedTags().contains(Utils.getTagByName("closed", parent))) {
									// check if the person who claimed the ticket is the person who is running the command, or the original poster
									String newMessage = """
											**CLOSED TICKET**
											<@%s> has completed this ticket.

											OP: To reopen this ticket, run `!reopen` in this channel.
											""".formatted(e.getAuthor().getId());
									String botMessage = Utils.getBotMessage(threadChannel, newMessage);
									if(botMessage == null) {
										e.getMessage().reply("Oops!  My original message got deleted!  This ticket is now closed.").mentionRepliedUser(false).queue();
										return;
									}
									String[] botMessageSplit = botMessage.split("\n");
									if(botMessageSplit[botMessageSplit.length - 1].equals(e.getAuthor().getId()) || Utils.isAdmin(e.getMember()) || Utils.isOP(threadChannel, e.getMember())) {
										// apply correct tags + remove TA name
										String currentName = threadChannel.getName();
										String newName = currentName.replaceFirst("^\\[TA: [^]]+]", "");
										newName = "[CLOSED] " + newName;

										if(threadChannel.getAppliedTags().contains(Utils.getTagByName("open", parent))) {
											Utils.editPost(threadChannel, "open", "closed", newName);
										} else {
											Utils.editPost(threadChannel, "claimed", "closed", newName);
										}

										// edit bot's message
										Utils.editBotMessage(threadChannel, newMessage);

										// feedback message
										e.getMessage().reply("You have successfully closed this ticket!").mentionRepliedUser(false).queue();
									} else {
										e.getMessage().reply("You are not the person who claimed this ticket, or the original poster!").mentionRepliedUser(false).queue();
									}
								} else {
									e.getMessage().reply("This ticket is already closed!").mentionRepliedUser(false).queue();
								}
							} else {
								e.getMessage().reply(":no_entry: **403 FORBIDDEN** :no_entry:\nYou do not have permission to run this command.").mentionRepliedUser(false).queue();
							}
						}
						case "reopen" -> {
							if(Utils.isAdmin(e.getMember()) || Utils.isOP(threadChannel, e.getMember())) {
								if(threadChannel.getAppliedTags().contains(Utils.getTagByName("closed", parent))) {
									// apply correct tags + remove CLOSED name
									String currentName = threadChannel.getName();
									String newName = currentName.replaceFirst("^\\[CLOSED] ", "");
									Utils.editPost(threadChannel, "closed", "open", newName);

									// edit bot's message
									String newMessage = """
											**OPEN TICKET**
											TAs: To claim this post, run `!claim` in this channel.
											OP: To close this post, run `!close` in this channel.
											""";
									Utils.editBotMessage(threadChannel, newMessage);

									// feedback message
									e.getMessage().reply("You have successfully reopened this ticket!").mentionRepliedUser(false).queue();
								} else {
									e.getMessage().reply("This ticket is not closed!").mentionRepliedUser(false).queue();
								}
							} else {
								e.getMessage().reply("You are not the original poster and cannot reopen this ticket.").mentionRepliedUser(false).queue();
							}
						}
					}
				} catch(Exception exception) {
					e.getMessage().reply("Oh no!  Something went VERY WRONG!  Please yell at the developers (aka just Bruce Qiang).").mentionRepliedUser(false).queue();
				}
			}
			if(message[0].equals("help")) {
				String messageToSend = """
						# HOW TO USE THIS BOT
						**Office Hours Tickets**
						Head over to <#1414699886124728370> and create a post.  A TA will be with you shortly.

						**Commands**
						`!claim` - TAs only.  Claim a ticket as yours.  Only one TA may claim a ticket at a time.
						`!unclaim` - TAs only.  Unclaim a ticket if you cannot solve a problem.  Only the person who claimed the ticket may unclaim it.
						`!close` - TAs and OP only.  Close a ticket (aka mark it as solved).  Only the person who claimed the ticket or the original poster may close it.
						`!reopen` - OP only.  If you need additional help related to the topic, run this command.  Only the original poster may reopen a ticket.

						**NOTE**: All Instructors and Mods can use all commands in any ticket.
						""";
				e.getChannel().sendMessage(messageToSend).queue();
			}
			if(message[0].equals("analyze") && e.getMember().hasPermission(Permission.ADMINISTRATOR)) {
				if(!isAnalysisRunning) {
					isAnalysisRunning = true;
					e.getMessage().reply("Beginning analysis of the Office Hours Channel...").mentionRepliedUser(false).queue();
					System.out.println("Analysis started.");

					/*
					 * Things to Analyze
					 * -----------------
					 * - Total # of Tickets
					 * - Claims per TA
					 * - Total Unclaims
					 * - Unclaims per TA
					 * - Total Reopens
					 * - Tickets per Student
					 *
					 * - Average time between open and claim (OH hours)
					 * - Average time between open and claim (non-OH hours)
					 * - Average time between open and claim (aggregated)
					 * - Average time between claim and close
					 *
					 * - Tickets that were never claimed but a TA sent a message in will affect time between open and claim (first TA message is effective)
					 * - Tickets that were claimed but never closed will calculate based on the last message sent
					 */

					ForumChannel channel = e.getGuild().getForumChannelById(Main.getForumChannelID());
					List<ThreadChannel> tickets = new ArrayList<>(channel.getThreadChannels());
					List<ThreadChannel> archivedThreads = channel.retrieveArchivedPublicThreadChannels()
							.stream()
							.toList();
					tickets.addAll(archivedThreads);
					System.out.println("Analyzing a list of " + tickets.size() + " tickets.");
					Map<String, Integer> taTracker = new HashMap<>();
					Map<String, Long> taTimeTracker = new HashMap<>();
					Map<String, Integer> taUnclaimTracker = new HashMap<>();
					Map<String, Integer> studentTracker = new HashMap<>();
					int unclaims = 0;
					int reopens = 0;
					int peakTickets = 0;
					int offPeakTickets = 0;
					long timeToClaimPeak = 0;
					long timeToClaimOffPeak = 0;
					long claimToClose = 0;
					int count = 0;
					for(ThreadChannel tc : tickets) {
						studentTracker.put(tc.getOwnerId(), studentTracker.getOrDefault(tc.getOwnerId(), 0) + 1);
						OffsetDateTime created = tc.getTimeCreated();
						if(isPeakHours(created)) {
							peakTickets++;
						} else {
							offPeakTickets++;
						}
						OffsetDateTime whenClaimed = OffsetDateTime.MIN;
						List<Message> messages = tc.getIterableHistory().complete();
						Message mostRecent = messages.getFirst();
						Message firstTAMessage = null;
						messages = messages.reversed();
						System.out.println("====================================================================================================");
						System.out.println("Now analyzing #" + count + ": " + tc.getId() + " (" + tc.getName() + ") with " + messages.size() + " messages.");
						Message previousMessage = messages.getFirst();
						long timeToClaim = 0;
						long timeToClose = 0;
						String status = "open";
						for(Message m : messages) {
							if(firstTAMessage == null && Utils.isMod(e.getGuild().getMemberById(m.getAuthor().getId()))) {
								System.out.println("This is the first TA message:");
								firstTAMessage = m;
							}
							String content = m.getContentRaw();
							if(content.isEmpty() || content.startsWith("[OPEN]") || content.startsWith("[TA: ") || content.startsWith("[CLOSED]")) {
								continue;
							}
							System.out.println("Message: " + content);
							String previousContent = previousMessage.getContentRaw();

							// if the previous String was a command (e.g. !claim), this checks for the bot's success message as the current message.  if the bot message is not detected, assume the command went through
							if(previousContent.startsWith("!claim") && (!e.getAuthor().getId().equals(e.getJDA().getSelfUser().getId()) || content.contains("You have successfully claimed this ticket!"))) {
								whenClaimed = m.getTimeCreated();
								if(status.equals("open")) {
									taTracker.put(previousMessage.getAuthor().getId(), taTracker.getOrDefault(previousMessage.getAuthor().getId(), 0) + 1);
									timeToClaim = whenClaimed.toEpochSecond() - created.toEpochSecond();
									System.out.println("Claimed at " + whenClaimed + ", took " + timeToClaim + " seconds");
									if(isPeakHours(created)) {
										timeToClaimPeak += timeToClaim;
									} else {
										timeToClaimOffPeak += timeToClaim;
									}
								} else {
									System.out.println("Reopened ticket claimed at " + whenClaimed);
								}
								status = "claimed";
							} else if(previousContent.startsWith("!unclaim") && status.equals("claimed") && (!e.getAuthor().getId().equals(e.getJDA().getSelfUser().getId()) || content.contains("You have successfully unclaimed this ticket!"))) {
								taUnclaimTracker.put(previousMessage.getAuthor().getId(), taUnclaimTracker.getOrDefault(previousMessage.getAuthor().getId(), 0) + 1);
								unclaims++;
								System.out.println("Unclaimed");
								whenClaimed = OffsetDateTime.MIN;
								status = "open";
							} else if(previousContent.startsWith("!close") && (!e.getAuthor().getId().equals(e.getJDA().getSelfUser().getId()) || content.contains("You have successfully closed this ticket!"))) {
								OffsetDateTime closed = m.getTimeCreated();
								if(Utils.isMod(e.getGuild().getMemberById(previousMessage.getAuthor().getId()))) {
									if(status.equals("claimed")) {
										timeToClose = closed.toEpochSecond() - whenClaimed.toEpochSecond();
										System.out.println("Closed at " + closed + " by a TA, took " + timeToClose + " seconds");
										claimToClose += timeToClose;
										taTimeTracker.put(previousMessage.getAuthor().getId(), taTimeTracker.getOrDefault(previousMessage.getAuthor().getId(), 0L) + timeToClose);
									} else {
										System.out.println("Closed at " + closed + " by a TA");
									}
								} else {
									System.out.println("Closed at " + closed + " by a student");
								}
								whenClaimed = OffsetDateTime.MIN;
								status = "closed";
							} else if(previousContent.startsWith("!reopen") && status.equals("closed") && (!e.getAuthor().getId().equals(e.getJDA().getSelfUser().getId()) || content.contains("You have successfully reopened this ticket!"))) {
								reopens++;
								System.out.println("Reopened");
								whenClaimed = OffsetDateTime.MIN;
								status = "reopen";
							}
							previousMessage = m;
						}

						if(status.equals("open") || status.equals("reopen")) {
							if(firstTAMessage != null) {
								taTracker.put(firstTAMessage.getAuthor().getId(), taTracker.getOrDefault(firstTAMessage.getAuthor().getId(), 0) + 1);
								whenClaimed = firstTAMessage.getTimeCreated();
							} else {
								whenClaimed = mostRecent.getTimeCreated();
							}
							timeToClaim = whenClaimed.toEpochSecond() - created.toEpochSecond();
							System.out.println("Open at end detected.  Defaulting to claimed at " + whenClaimed + ", took " + timeToClaim + " seconds");
							if(isPeakHours(created)) {
								timeToClaimPeak += timeToClaim;
							} else {
								timeToClaimOffPeak += timeToClaim;
							}
							status = "claimed";
						}
						if(status.equals("claimed")) {
							OffsetDateTime closed = mostRecent.getTimeCreated();
							timeToClose = closed.toEpochSecond() - whenClaimed.toEpochSecond();
							System.out.println("Claimed at end detected.  Defaulting to closed at " + whenClaimed + ", took " + timeToClose + " seconds");
							claimToClose += timeToClose;
						}

						System.out.println("Finished analyzing " + tc.getId() + " (" + tc.getName() + ").  This ticket's stats:");
						System.out.println("Time to Claim: " + timeToClaim);
						System.out.println("Time to Close: " + timeToClose);
						count++;
					}

					System.out.println("====================================================================================================");
					System.out.println("Full analysis finished.  Stats:");
					System.out.println("Total Number of Tickets: " + tickets.size());
					System.out.println("Tickets to TAs: " + taTracker);
					System.out.println("Average time per TA: " + taTimeTracker);
					System.out.println("Total Unclaims: " + unclaims);
					System.out.println("Unclaims to TAs: " + taUnclaimTracker);
					System.out.println("Students to Tickets: " + studentTracker);
					System.out.println("Total Reopens: " + reopens);
					System.out.println("--------------------------------------------------");
					System.out.println("Average time between open and claim (peak hours): " + timeToClaimPeak / peakTickets);
					System.out.println("Average time between open and claim (off-peak hours): " + timeToClaimOffPeak / offPeakTickets);
					System.out.println("Average time between open and claim (in general): " + (timeToClaimPeak + timeToClaimOffPeak) / (peakTickets + offPeakTickets));
					System.out.println("Total time spent waiting for TAs: " + (timeToClaimPeak + timeToClaimOffPeak));
					System.out.println("--------------------------------------------------");
					System.out.println("Average time between claim and close: " + claimToClose / (peakTickets + offPeakTickets));
					System.out.println("Total time spent helping students: " + claimToClose);
					isAnalysisRunning = false;
				} else {
					e.getMessage().reply("An analysis is already running!").mentionRepliedUser(false).queue();
				}
			}
		}
	}

	private static boolean isPeakHours(OffsetDateTime time) {
		ZonedDateTime estTime = time.atZoneSameInstant(ZoneId.of("America/New_York"));
		int hour = estTime.getHour();
		switch(estTime.getDayOfWeek()) {
			case MONDAY -> {
				return hour >= 11 && hour < 21;
			}
			case TUESDAY -> {
				return hour >= 11 && hour < 21 && hour != 12;
			}
			case WEDNESDAY, SUNDAY -> {
				return hour >= 11 && hour < 22;
			}
			case THURSDAY -> {
				return hour >= 12 && hour < 22;
			}
			case FRIDAY -> {
				return hour >= 11 && hour < 18;
			}
			case SATURDAY -> {
				return false;
			}
		}
		return false;
	}
}