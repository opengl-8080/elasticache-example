package example;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.resource.DefaultClientResources;
import io.lettuce.core.resource.DirContextDnsResolver;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        final Map<String, String> argsMap = parse(args);
        final String schema = argsMap.get("schema");
        final String host = argsMap.getOrDefault("host", "localhost");
        final int port = Integer.parseInt(argsMap.getOrDefault("port", "6379"));
        final String password = argsMap.get("password");

        final RedisURI uri;
        if (schema == null) {
            final RedisURI.Builder builder = RedisURI.builder()
                    .withHost(host)
                    .withPort(port);

            if (password != null) {
                builder.withPassword(password);
            }
            uri = builder.build();
        } else {
            String pass;
            if (password == null) {
                pass = "";
            } else {
                pass = password + "@";
            }
            uri = RedisURI.create(String.format("%s://%s%s:%d", schema, pass, host, port));
        }

        final boolean ssl = argsMap.getOrDefault("ssl", "false").equals("true");
        final boolean tls = argsMap.getOrDefault("tls", "false").equals("true");
        if (ssl) {
            uri.setSsl(true);
        }
        
        if (tls) {
            uri.setStartTls(true);
        }
        

        final DefaultClientResources clientResources = DefaultClientResources.builder()
                .dnsResolver(new DirContextDnsResolver())
                .build();

        System.out.println("uri = " + uri.toURI());
        
        final RedisClient client = RedisClient.create(clientResources, uri);
        final RedisCommands<String, String> commands = client.connect().sync();
        commands.set("message", "Hello at " + LocalDateTime.now());
        final String message = commands.get("message");
        System.out.println("message = " + message);
        client.shutdown();
    }
    
    private static Map<String, String> parse(String[] args) {
        Map<String, String> argsMap = new HashMap<>();
        for (String arg : args) {
            final String[] tokens = arg.split("=");
            argsMap.put(tokens[0], tokens[1]);
        }
        return argsMap;
    }
}
