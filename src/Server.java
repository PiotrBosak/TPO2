import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

public class Server {
    private ServerSocketChannel ssc;
    private Selector selector;
    private static final int BUFFER_SIZE = 16384;
    private static Charset charset = Charset.defaultCharset();
    private ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
    private StringBuffer response = new StringBuffer();
    private static final String DELIMITER = " ";



    public Server(String host, int port) throws Exception {
        ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.socket().bind(new InetSocketAddress(host, port));
        selector = Selector.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT);
    }

    public static void main(String[] args) {
        try {
            InetAddress host = InetAddress.getLocalHost();
            int port = 12345;
            Server server = new Server(host.getHostName(),port);
            server.handleConnections();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void handleConnections() throws Exception {
        for (; ; ) {
            selector.select();

            Set keys = selector.selectedKeys();

            Iterator iterator = keys.iterator();

            while (iterator.hasNext()) {
                SelectionKey key = (SelectionKey) iterator.next();
                iterator.remove();

                if (key.isAcceptable()) {
                    SocketChannel sc = ssc.accept();
                    sc.configureBlocking(false);
                    sc.register(selector, SelectionKey.OP_READ);
                    continue;
                }
                if (key.isReadable()) {
                    SocketChannel sc = (SocketChannel) key.channel();
                    handleRequest(sc); }
            }
        }
    }


    private void handleRequest(SocketChannel sc) throws Exception {
        StringBuffer stringBuffer = getMessage(sc);
        if(stringBuffer == null){
            System.out.println("no message");
            return;
        }
        processMessage(stringBuffer,sc);


    }

    private StringBuffer getMessage(SocketChannel sc) throws Exception{
        if (!sc.isOpen())
            return null;
        return Client.receiveMessage(buffer, sc, charset);

    }

    private void processMessage(StringBuffer sb, SocketChannel sc) throws Exception{
        char [] chars = new char [sb.length()];
        sb.getChars(0,sb.length(),chars,0);
        String message = new String(chars);
        String [] words = message.split(DELIMITER);
        String operation = words[0];
        if("echo".equalsIgnoreCase(operation)) {
            processEcho(sc, words);
            System.out.println("echo command");
        }
        else if("add".equalsIgnoreCase(operation)){
            processAddition(sc,words);
            System.out.println("add command");
        }
        else throw new RuntimeException("Not a valid operation");
    }

    private void processEcho(SocketChannel sc, String [] words) throws Exception{
        response.setLength(0);
        for(int i = 1; i<words.length; ++i) {
            response.append(words[i]);
            response.append(' ');
        }
        response.append('\n');
        ByteBuffer buffer = charset.encode(CharBuffer.wrap(response));
        sc.write(buffer);

    }

    private void processAddition(SocketChannel sc,String [] words) throws Exception{
        response.setLength(0);
        int [] numbers = new int[words.length-1];
        for(int i = 1; i<words.length; ++i)
            numbers[i-1] = Integer.parseInt(words[i]);
        int result = 0;
        for(int n : numbers)
            result += n;
        response.append(result);
        response.append('\n');
        ByteBuffer buffer = charset.encode(CharBuffer.wrap(response));
        sc.write(buffer);

    }





}
