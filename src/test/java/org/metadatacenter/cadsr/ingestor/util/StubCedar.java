package org.metadatacenter.cadsr.ingestor.util;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

/**
 * A local server that answers for CEDAR while it runs: the resource server under /resource, the repository server
 * under /repo and the terminology server under /terminology. It records every request, and answers each from the
 * most recently added route whose method matches and whose path starts with the route's; a request no route
 * matches gets 404.
 */
public final class StubCedar implements AutoCloseable {

  public record Request(String method, String path, String query, String authorization, String sourceHash,
                        String body) {
  }

  public record Reply(int status, String body) {
  }

  private record Route(String method, String pathPrefix, Function<Request, Reply> reply) {
  }

  private final HttpServer server;
  private final List<Request> requests = new CopyOnWriteArrayList<>();
  private final List<Route> routes = new CopyOnWriteArrayList<>();

  private StubCedar() throws IOException {
    server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
    server.createContext("/", this::handle);
    server.start();
    CedarServerUtil.overrideServerBaseUrl("http://127.0.0.1:" + server.getAddress().getPort());
  }

  public static StubCedar start() throws IOException {
    return new StubCedar();
  }

  /** The prefix of every resource-server address while the stub runs. */
  public String resourceServer() {
    return CedarServerUtil.getResourceServerUrl(Constants.CedarServer.LOCAL);
  }

  public StubCedar answer(String method, String pathPrefix, int status, String body) {
    return answer(method, pathPrefix, request -> new Reply(status, body));
  }

  public StubCedar answer(String method, String pathPrefix, Function<Request, Reply> reply) {
    routes.add(new Route(method, pathPrefix, reply));
    return this;
  }

  public List<Request> requests() {
    return List.copyOf(requests);
  }

  public List<Request> requests(String method, String pathPrefix) {
    List<Request> matching = new ArrayList<>();
    for (Request request : requests) {
      if (request.method().equals(method) && request.path().startsWith(pathPrefix)) {
        matching.add(request);
      }
    }
    return matching;
  }

  @Override
  public void close() {
    CedarServerUtil.overrideServerBaseUrl(null);
    server.stop(0);
  }

  private void handle(HttpExchange exchange) throws IOException {
    Request request = new Request(exchange.getRequestMethod(), exchange.getRequestURI().getRawPath(),
        exchange.getRequestURI().getRawQuery(), exchange.getRequestHeaders().getFirst("Authorization"),
        exchange.getRequestHeaders().getFirst("CEDAR-Source-Hash"),
        new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
    requests.add(request);
    Reply reply = new Reply(404, "{\"message\": \"no route\"}");
    for (int i = routes.size() - 1; i >= 0; i--) {
      Route route = routes.get(i);
      if (route.method().equals(request.method()) && request.path().startsWith(route.pathPrefix())) {
        reply = route.reply().apply(request);
        break;
      }
    }
    byte[] body = reply.body() == null ? new byte[0] : reply.body().getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
    exchange.sendResponseHeaders(reply.status(), reply.status() == 204 || body.length == 0 ? -1 : body.length);
    try (OutputStream out = exchange.getResponseBody()) {
      out.write(body);
    }
  }
}
