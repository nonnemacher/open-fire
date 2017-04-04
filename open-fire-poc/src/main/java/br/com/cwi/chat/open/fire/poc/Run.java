package br.com.cwi.chat.open.fire.poc;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Scanner;

import javax.net.ssl.X509TrustManager;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;

/**
 * @author carloshenrique
 */
public class Run {

	public static void main(String[] args) throws NoSuchAlgorithmException, IOException, SmackException, XMPPException, InterruptedException {
		try {
			try (final Scanner scanner = new Scanner(System.in)) {

				final String user, pass;

				System.out.print("User: ");
				System.out.println((user = scanner.nextLine()));
				System.out.print("Pass: ");
				System.out.println((pass = scanner.nextLine()));

				ChatUnimed.getInstance().login(user, pass);

				final String contact;
				System.out.print("Open chat with: ");
				System.out.println((contact = scanner.nextLine()));

				final Chat chat = ChatUnimed.getInstance().getChat(contact);

				while (true) {
					final String line;
					System.out.print(user + " - ");
					System.out.println((line = scanner.nextLine()));

					if (line == "\\quit") {
						break;
					}
					ChatUnimed.getInstance().send(chat, line);
					System.out.println("");
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ChatUnimed.getInstance().disconnect();
		}
	}

	public static class ChatUnimed {

		private static final String HOST = System.getProperty("openfire.host");
		private static final Integer PORT = Integer.valueOf(System.getProperty("openfire.port"));
		private static final String DOMAIN = System.getProperty("openfire.domain");

		private static ChatUnimed chatUnimed;

		private final XMPPTCPConnection connection;
		private ChatManager chatManager;

		private ChatUnimed() throws IOException, NoSuchAlgorithmException {
			final XMPPTCPConnectionConfiguration configuration = XMPPTCPConnectionConfiguration.builder()
					.setXmppDomain(DOMAIN)
					.setHost(HOST)
					.setPort(PORT)
					.build();
			this.connection = new XMPPTCPConnection(configuration);
		}

		public static ChatUnimed getInstance() throws IOException, NoSuchAlgorithmException {
			if (chatUnimed == null) {
				chatUnimed = new ChatUnimed();
			}
			return chatUnimed;
		}

		public XMPPTCPConnection getConnection() throws SmackException, IOException, XMPPException, InterruptedException {
			if (!connection.isConnected()) {
				connection.connect();
			}
			return connection;
		}

		public void login(final String user, final String password) throws SmackException, IOException, XMPPException, InterruptedException {
			this.getConnection().login(user, password);
		}

		public void disconnect() throws SmackException, IOException, XMPPException, InterruptedException {
			this.getConnection().disconnect();
		}

		public Chat getChat(String user) throws SmackException, IOException, XMPPException, InterruptedException, NoSuchAlgorithmException {
			final EntityBareJid entityBareFrom = JidCreate.entityBareFrom(String.format("%s@chat.unimedvtrp.com.br", user));
			final Roster roster = Roster.getInstanceFor(ChatUnimed.getInstance().getConnection());
			if (!roster.contains(entityBareFrom)) {
				roster.createEntry(entityBareFrom, user, null);
			}
			return this.getChatManager().chatWith(entityBareFrom);
		}

		public void send(final Chat chat, final String message) throws SmackException, IOException, XMPPException, InterruptedException {
			chat.send(message);
		}

		public ChatManager getChatManager() throws SmackException, IOException, XMPPException, InterruptedException {
			if (chatManager == null) {
				this.chatManager = ChatManager.getInstanceFor(this.getConnection());
				chatManager.addIncomingListener((from, message, chat) -> {
					System.out.println("New message from " + from + ": " + message.getBody());
				});
			}
			return chatManager;
		}

	}

	public class TrustAllX509TrustManager implements X509TrustManager {

		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[0];
		}

		public void checkClientTrusted(java.security.cert.X509Certificate[] certs,
				String authType) {
		}

		public void checkServerTrusted(java.security.cert.X509Certificate[] certs,
				String authType) {
		}

	}

}
