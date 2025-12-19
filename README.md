# Setup
## Discord Developer Mode
Developer Mode is a feature on Discord that exposes the internal IDs of every entity (e.g. roles, users, channels).  It is required to explicitly tell the Discord API which entity is being referenced.

To activate it, go to User Settings --> Advanced --> Developer Mode.  Alternatively, you can search "Developer Mode" in the User Settings menu.

Once it is activated, right click on any message.  There should be a new option at the bottom - "Copy Message ID".  This will now be the case for all entities, including users and roles.

A mistake (that I have done many times) is not right-clicking the correct entity.  Sometimes you want to copy the ID of a User, but get the Message ID instead.  The "Copy XXX ID" will update based on which entity you are attempting to copy, so this can be used to ensure accuracy.

## Setting Up Your Bot
1. Head over to https://discord.com/developers/applications
2. On this page, click "New Application" in the top right.
3. Give it a name and (optional) add it to a team.  You can also keep it under your personal account as a default.
4. On the left sidebar, choose "Bot" (fourth option).  Here is where you will configure the bot account in your server.
5. Change the username of your bot and upload a profile picture.
6. Reset the token of the bot.  **THIS WILL GIVE FULL ACCESS TO YOUR BOT, AND SHOULD BE TREATED LIKE ANY OTHER SECRET!!!**  Save it to a notepad file for now.
7. Turn off the Public Bot setting.  Then, under Privileged Gateway Intents, turn on all three toggles.
8. Go to the OAuth2 tab (third option).  Here is where you will generate an invite link for the bot.
9. Select the `bot` scope.  Then, under permissions, give it `Administrator` OR the following:
   1. `Manage Channels`
   2. `View Channels`
   3. `Send Messages`
   4. `Create Public Threads`
   5. `Create Private Threads`
   6. `Send Messages in Threads`
   7. `Pin Messages`
   8. `Manage Threads`
   9. `Embed Links`
   10. `Attach Files`
   11. `Read Message History`
   12. `Add Reactions`
10. Make sure Integration Type is set to Guild.  Then, copy the generated URL and add the bot to your server.

## .env File & General Config
Copy the .env.example file and rename it to .env.

Three bits of information are required for the bot to work.
1. Bot Token (see Step 4-6 in Setting Up Your Bot if you don't have it)
2. The ID of the Forum Channel.  Right click your Forum Channel and copy its ID.
3. The ID of the moderator role.  Give yourself the role, right click it when viewing your profile, and copy its ID.

In the forum channel the bot is being used in, make sure the following tags are present: `open` `claimed` `closed`

You can verify that your config is correct using `!validate`

## Building and Hosting
### Command Line
Contributions welcome!

### IntelliJ
1. Clone the repository into a directory.
2. Make sure you have downloaded all the dependencies in `pom.xml`
3. Go to File --> Project Structure (ctrl + alt + shift + S)
4. Open the Artifacts tab (fifth from the top).  Add a new Artifact.
5. Choose JAR --> From modules with dependencies...
6. The main class is in `main.Main`.  Leave the rest of the options as default.
7. Save and close.  Then, go to Build --> Build Artifacts...
8. Build the bot.  The output should be in out/artifacts/(artifactName).  Copy the JAR file onto your server.
9. You can host this bot on pretty much any server with Java 21.  256MB of RAM is more than enough, 128MB might even be enough.  Make sure you don't host the bot on a window that closes on logout, use GNU Screen or a similar app.

# Basic Guide
## Tickets
This bot utilizes the Discord Forum channel.  Each thread is a separate Ticket, and moderators can claim and close tickets like in a normal ticketing system.

## Commands

`!help` - Show this help message

`!validate` - Ensures that your config is correct.

`!claim` - TAs only.  Claim a ticket as yours.  Only one TA may claim a ticket at a time.

`!unclaim` - TAs only.  Unclaim a ticket if you cannot solve a problem.  Only the person who claimed the ticket may unclaim it.

`!close` - TAs and OP only.  Close a ticket (aka mark it as solved).  Only the person who claimed the ticket or the original poster may close it.

`!reopen` - OP only.  If you need additional help related to the topic, run this command.  Only the original poster may reopen a ticket.

`!analyze` - Run an analysis of the channel.  **WARNING: EXPERIMENTAL.  RESULTS MAY BE VERY INACCURATE.**
						
**NOTE**: All Instructors and Mods can use all commands in any ticket.

# Contributing
This was made for Prof. Daniel Patterson, et al. at Northeastern University for the Fall 2025 semester of CS 2000.  The current implementation is VERY basic, if you would like to improve it (e.g. private threads, proper logging), feel free to make an Issue or PR.