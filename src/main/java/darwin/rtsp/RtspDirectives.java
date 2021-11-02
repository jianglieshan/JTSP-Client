package darwin.rtsp;

/**
 * 参考 https://en.wikipedia.org/wiki/Real_Time_Streaming_Protocol 实现
 *
 * @author jiangzheng
 * @since 2021/10/31 21:44
 */
public interface RtspDirectives {


    void options();

    void describe();

    void setup();

    void play();

    void pause();

    void record();

    void announce();

    void teardown();

    void getParameter();

    void setParameter();

    void redirect();

}
