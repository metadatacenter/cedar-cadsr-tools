package org.metadatacenter.cadsr.ingestor.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A local FTP server with one account and a few folders, speaking as much of the protocol as Apache Commons Net's
 * client uses to log in, change folder, list it in Unix format, and retrieve a file over a passive connection.
 */
public final class StubFtp implements AutoCloseable {

  private record Entry(byte[] content, LocalDate modified, boolean directory) {
  }

  private static final DateTimeFormatter LISTING_DATE = DateTimeFormatter.ofPattern("MMM dd  yyyy", Locale.US);

  private final String user;
  private final String password;
  private final Map<String, Map<String, Entry>> folders = new LinkedHashMap<>();
  private final ServerSocket control;
  private final Thread acceptor;

  public StubFtp(String user, String password) throws IOException {
    this.user = user;
    this.password = password;
    control = new ServerSocket(0, 5, InetAddress.getLoopbackAddress());
    acceptor = new Thread(this::serve, "stub-ftp");
    acceptor.setDaemon(true);
    acceptor.start();
  }

  /** The host and port, as the tools' FTP host option takes them. */
  public String address() {
    return "127.0.0.1:" + control.getLocalPort();
  }

  public StubFtp file(String folder, String name, byte[] content, LocalDate modified) {
    folders.computeIfAbsent(folder, f -> new LinkedHashMap<>()).put(name, new Entry(content, modified, false));
    return this;
  }

  public StubFtp directory(String folder, String name, LocalDate modified) {
    folders.computeIfAbsent(folder, f -> new LinkedHashMap<>()).put(name, new Entry(new byte[0], modified, true));
    return this;
  }

  @Override
  public void close() throws IOException {
    control.close();
  }

  private void serve() {
    while (!control.isClosed()) {
      try (Socket socket = control.accept()) {
        session(socket);
      } catch (IOException e) {
        // The server was closed, or the client went away; the next client gets a fresh session.
      }
    }
  }

  private void session(Socket socket) throws IOException {
    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
    PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
    reply(out, "220 stub FTP");
    String givenUser = null;
    boolean loggedIn = false;
    String folder = "/";
    ServerSocket passive = null;
    String line;
    while ((line = in.readLine()) != null) {
      String command = line.split(" ", 2)[0].toUpperCase(Locale.ROOT);
      String argument = line.contains(" ") ? line.substring(line.indexOf(' ') + 1) : "";
      switch (command) {
        case "USER" -> {
          givenUser = argument;
          reply(out, "331 Password required");
        }
        case "PASS" -> {
          loggedIn = user.equals(givenUser) && password.equals(argument);
          reply(out, loggedIn ? "230 Logged in" : "530 Login incorrect");
        }
        case "SYST" -> reply(out, "215 UNIX Type: L8");
        case "TYPE" -> reply(out, "200 Type set");
        case "CWD" -> {
          if (loggedIn && folders.containsKey(argument)) {
            folder = argument;
            reply(out, "250 Directory changed");
          } else {
            reply(out, "550 No such directory");
          }
        }
        case "PASV" -> {
          passive = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
          int port = passive.getLocalPort();
          reply(out, "227 Entering Passive Mode (127,0,0,1," + (port / 256) + "," + (port % 256) + ")");
        }
        case "LIST" -> {
          reply(out, "150 Here comes the listing");
          StringBuilder listing = new StringBuilder();
          for (Map.Entry<String, Entry> entry : folders.getOrDefault(folder, Map.of()).entrySet()) {
            Entry e = entry.getValue();
            listing.append(e.directory() ? "drwxr-xr-x" : "-rw-r--r--").append("   1 ftp ftp ")
                .append(String.format("%10d ", e.content().length)).append(LISTING_DATE.format(e.modified()))
                .append(' ').append(entry.getKey()).append("\r\n");
          }
          send(passive, listing.toString().getBytes(StandardCharsets.UTF_8));
          reply(out, "226 Directory send OK");
        }
        case "RETR" -> {
          Entry entry = folders.getOrDefault(folder, Map.of()).get(argument);
          if (entry == null || entry.directory()) {
            passive.close();
            reply(out, "550 No such file");
          } else {
            reply(out, "150 Opening data connection");
            send(passive, entry.content());
            reply(out, "226 Transfer complete");
          }
        }
        case "QUIT" -> {
          reply(out, "221 Goodbye");
          return;
        }
        default -> reply(out, "502 Command not implemented");
      }
    }
  }

  private static void send(ServerSocket passive, byte[] bytes) throws IOException {
    try (ServerSocket listener = passive; Socket data = listener.accept(); OutputStream out = data.getOutputStream()) {
      out.write(bytes);
    }
  }

  private static void reply(PrintWriter out, String reply) {
    out.print(reply + "\r\n");
    out.flush();
  }
}
