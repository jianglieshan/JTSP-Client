package darwin;

import darwin.rtsp.RtspClient;

/**
 * @author jiangzheng
 * @since 2021/10/30 15:09
 */
public class CmdClient {
    private RtspClient rtspClient;

    public CmdClient(String ip, int port, String address) {
        rtspClient = new RtspClient(ip, port, address);
    }

    public static void main(String[] args) {
        CmdClient cmdClient = new CmdClient("192.168.1.166", 7550, "rtsp://192.168.1.166:7550/1");
        cmdClient.start();
    }

    private void start() {
        rtspClient.start();
        rtspClient.options();
    }
}
