import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Scanner;

public class Client {
    private SocketChannel ssc;
    private static final int BUFFER_SIZE = 16384;
    private static Charset charset = Charset.defaultCharset();
    private ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
    private StringBuffer response = new StringBuffer();

    public Client(String host, int port) throws Exception {
        ssc = SocketChannel.open(new InetSocketAddress(host, port));
        ssc.configureBlocking(false);

    }

    public static void main(String[] args) throws Exception {
        InetAddress host = InetAddress.getLocalHost();
        int port = 12345;
        Client client = new Client(host.getHostName(), port);
        while(true) {
            String message = client.getMessage();
            if("finish".equalsIgnoreCase(message)) {
                client.ssc.close();
                return;
            }
            client.sendMessage(message);
            client.receiveMessage();
            client.displayMessage();
        }


    }

    private String getMessage() {
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }

    private void sendMessage(String message) throws Exception{
        message += '\n';
        ByteBuffer buffer = charset.encode(CharBuffer.wrap(message));
        this.ssc.write(buffer);
    }

    private void receiveMessage() throws Exception{
        if (!ssc.isOpen())
            return;
        response = receiveMessage(buffer,ssc,charset);
    }

    public static StringBuffer receiveMessage(ByteBuffer buffer, SocketChannel ssc, Charset charset) throws IOException {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.setLength(0);
        buffer.clear();
        MAIN_LOOP:
        for (; ; ) {
            int n = ssc.read(buffer);
            if (n > 0) {
                buffer.flip();
                CharBuffer charBuffer = charset.decode(buffer);
                while (charBuffer.hasRemaining()) {
                    char c = charBuffer.get();
                    if (c == '\n')
                        break MAIN_LOOP;
                    stringBuffer.append(c);
                }

            }
        }
        return stringBuffer;
    }

    private void displayMessage(){
        char [] chars  = new char[response.length()];
        response.getChars(0,response.length(),chars,0);
        String message = new String(chars);
        System.out.println(message);

    }


}
