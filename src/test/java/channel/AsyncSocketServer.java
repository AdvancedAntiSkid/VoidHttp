package channel;

import lombok.SneakyThrows;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Scanner;

public class AsyncSocketServer {
    public static void main(String[] args) throws Exception {
        AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open();
        server.bind(new InetSocketAddress("127.0.0.1", 1234));

        // accept the first connection
        server.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            @Override
            @SneakyThrows
            public void completed(AsynchronousSocketChannel channel, Void attachment) {
                // accept the next connection recursively, if the server is still open
                if (server.isOpen())
                    server.accept(null, this);

                System.out.println("Socket accepted: " + channel.getRemoteAddress());

                channelRead(channel);
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                System.out.println("Failed to accept connection: " + exc.getMessage());
            }
        });

        System.out.println("Server up");
        new Scanner(System.in).nextLine();
        System.out.println("Server down");
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

                channelWrite(channel);
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                System.out.println("Failed to read from channel: " + exc.getMessage());
            }
        });
    }

    private static void channelWrite(AsynchronousSocketChannel channel) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.put("Hello from server".getBytes());
        buffer.flip();

        channel.write(buffer, null, new CompletionHandler<Integer, Void>() {
            @Override
            public void completed(Integer bytesWritten, Void attachment) {
                System.out.println("Wrote " + bytesWritten + " bytes");
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                System.out.println("Failed to write to channel: " + exc.getMessage());
            }
        });
    }
}
