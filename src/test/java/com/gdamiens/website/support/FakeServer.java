package com.gdamiens.website.support;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * Local HTTP server standing for the external APIs (PRIM, Navitia, Opendatasoft, Vélib, Google's keys): each test
 * says what a path answers, and reads the requests it got. Unknown paths answer 404.
 */
public final class FakeServer implements AutoCloseable {

    public record Request(String method, String path, Map<String, List<String>> query, Map<String, List<String>> headers) {

        public String param(String name) {
            List<String> values = query.get(name);
            return values == null || values.isEmpty() ? null : values.getFirst();
        }

        public List<String> params(String name) {
            return query.getOrDefault(name, List.of());
        }

        public String header(String name) {
            return headers.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(name))
                .map(entry -> entry.getValue().getFirst())
                .findFirst().orElse(null);
        }
    }

    public record Response(int status, String contentType, byte[] body) {

        public static Response json(String json) {
            return json(200, json);
        }

        public static Response json(int status, String json) {
            return new Response(status, "application/json", json.getBytes(StandardCharsets.UTF_8));
        }

        public static Response bytes(byte[] body) {
            return new Response(200, "application/octet-stream", body);
        }

        public static Response status(int status) {
            return new Response(status, "text/plain", new byte[0]);
        }
    }

    private final HttpServer server;

    private final Map<String, Function<Request, Response>> routes = new ConcurrentHashMap<>();

    private final List<Request> requests = new CopyOnWriteArrayList<>();

    private FakeServer(HttpServer server) {
        this.server = server;
    }

    public static FakeServer start() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
            FakeServer fake = new FakeServer(server);
            server.createContext("/", fake::handle);
            server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
            server.start();
            return fake;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String url() {
        return "http://" + server.getAddress().getHostString() + ":" + server.getAddress().getPort();
    }

    /** What [path] answers from now on (exact path, or the longest prefix ending with {@code /**}) */
    public void on(String path, Function<Request, Response> handler) {
        routes.put(path, handler);
    }

    public void on(String path, Response response) {
        on(path, request -> response);
    }

    /** Requests received on [path] */
    public List<Request> requests(String path) {
        return requests.stream().filter(request -> request.path().equals(path)).toList();
    }

    /** Forgets the routes and the requests */
    public void reset() {
        routes.clear();
        requests.clear();
    }

    @Override
    public void close() {
        server.stop(0);
    }

    private void handle(HttpExchange exchange) throws IOException {
        try (exchange; InputStream body = exchange.getRequestBody()) {
            body.readAllBytes();
            URI uri = exchange.getRequestURI();
            Request request = new Request(exchange.getRequestMethod(), uri.getPath(), Query.parse(uri.getRawQuery()),
                Map.copyOf(exchange.getRequestHeaders()));
            requests.add(request);

            Function<Request, Response> handler = route(uri.getPath());
            Response response = handler == null ? Response.status(404) : handler.apply(request);
            exchange.getResponseHeaders().set("Content-Type", response.contentType());
            exchange.sendResponseHeaders(response.status(), response.body().length == 0 ? -1 : response.body().length);
            if (response.body().length > 0) {
                try (OutputStream out = exchange.getResponseBody()) {
                    out.write(response.body());
                }
            }
        }
    }

    private Function<Request, Response> route(String path) {
        Function<Request, Response> exact = routes.get(path);
        if (exact != null) {
            return exact;
        }
        return routes.entrySet().stream()
            .filter(entry -> entry.getKey().endsWith("/**") && path.startsWith(entry.getKey().substring(0, entry.getKey().length() - 2)))
            .max(Comparator.comparingInt(entry -> entry.getKey().length()))
            .map(Map.Entry::getValue)
            .orElse(null);
    }
}
