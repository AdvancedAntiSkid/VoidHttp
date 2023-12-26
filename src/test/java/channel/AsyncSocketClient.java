package channel;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Scanner;

public class AsyncSocketClient {
    public static void main(String[] args) throws Exception {
        AsynchronousSocketChannel client = AsynchronousSocketChannel.open();

        client.connect(new InetSocketAddress("127.0.0.1", 1234), null, new CompletionHandler<Void, Void>() {
            @Override
            public void completed(Void result, Void attachment) {
                channelWrite(client);
                channelRead(client);
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                System.out.println("Failed to connect to server: " + exc.getMessage());
            }
        });

        System.out.println("Client up");
        new Scanner(System.in).nextLine();
        System.out.println("Client down");
    }

    private static void channelWrite(AsynchronousSocketChannel channel) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.put("Hello World".getBytes());
        buffer.flip();

        channel.write(buffer, null, new CompletionHandler<Integer, Void>() {
            @Override
            public void completed(Integer bytesWritten, Void attachment) {
                if (bytesWritten == -1) {
                    System.out.println("Connection closed");
                    return;
                }

                System.out.println("Sent: " + bytesWritten + " bytes");
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                System.out.println("Failed to write to channel: " + exc.getMessage());
            }
        });
    }

    private static void channelRead(AsynchronousSocketChannel channel) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        channel.read(buffer, null, new CompletionHandler<Integer, Void>() {
            @Override
            public void completed(Integer bytesRead, Void attachment) {
                if (bytesRead == -1) {
                    System.out.println("Connection closed");
                    return;
                }

                buffer.flip();
                byte[] bytes = new byte[bytesRead];
                buffer.get(bytes);
                System.out.println("Received: " + new String(bytes));
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                System.out.println("Failed to read from channel: " + exc.getMessage());
            }
        });
    }
}
