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
        final String scheme = argsMap.getOrDefault("scheme", "redis");
        final String host = argsMap.getOrDefault("host", "localhost");
        final String port = argsMap.getOrDefault("port", "6379");
        final String password = argsMap.containsKey("password") ? argsMap.get("password") + "@" : "";


        final DefaultClientResources clientResources = DefaultClientResources.builder()
                .dnsResolver(new DirContextDnsResolver())
                .build();

        final RedisURI uri = RedisURI.create(String.format("%s://%s%s:%s", scheme, password, host, port));
        uri.setSsl(true);
        uri.setStartTls(true);
        
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
