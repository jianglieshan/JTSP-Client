package darwin;

import darwin.rtsp.RtspClient;

/**
 * @author jiangzheng
 * @since 2021/10/30 15:09
 */
public class CmdClient {
    private final RtspClient rtspClient;

    public CmdClient(String ip, int port, String address, String account, String password) {
        rtspClient = new RtspClient(ip, port, address, account, password);
    }

    public static void main(String[] args) {

//        CmdClient cmdClient = new CmdClient("192.168.1.59", 7554, "rtsp://192.168.1.166:7550/1",null,null);
//        CmdClient cmdClient = new CmdClient("192.168.1.166", 7550, "rtsp://192.168.1.166:7550/1",null,null);
        CmdClient cmdClient = new CmdClient("192.168.1.243", 554, "rtsp://192.168.1.243:554/1/medium", "admin", "admin");
        cmdClient.start();
    }

    private void start() {
        rtspClient.start();
        rtspClient.options();
    }
}
