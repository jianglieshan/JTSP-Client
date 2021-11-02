package darwin.rtsp;

import java.util.function.BiConsumer;

public enum RtspDirectiveEnum {
    /**
     *
     */
    OPTIONS((response, client) -> {
        client.describe();
    }),
    DESCRIBE((response, client) -> {
        client.setup();
    }),
    SETUP((response, client) -> {
        String[] split = response.split("\r\n");
        for (String line : split) {
            if (line.contains("Session")) {
                String sessionId = line.substring(9, 18);
                client.setSessionId(sessionId);
            }
            if (line.contains("Transport")) {
                String[] transportParam = line.split(";");
                String clientPort = transportParam[5];
                String[] ports = clientPort.replace("client_port=", "").split("-");
                client.startReceiveRTPPacket(Integer.parseInt(ports[0]), Integer.parseInt(ports[1]));
            }
        }
        client.play();
    }),
    PLAY((response, client) -> {

    }),
    TEARDOWN(null),
    ;

    private final BiConsumer<String, RtspClient> action;

    RtspDirectiveEnum(BiConsumer<String, RtspClient> action) {
        this.action = action;
    }

    public void trigger(String content, RtspClient client) {
        if (this.action != null) {
            this.action.accept(content, client);
        }
    }
}
