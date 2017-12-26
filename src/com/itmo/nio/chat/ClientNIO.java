package com.itmo.nio.chat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

/**
 * Created by xmitya on 10.01.17.
 */
public class ClientNIO {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket();

        socket.connect(new InetSocketAddress("localhost", 12345));

//        new Thread(new Receiver(socket)).start();
        new Thread(new Sender(socket)).start();
    }

    private static class NioClient extends Worker {
        private static final int BUF_SIZE = 1024;
        private SocketChannel socketCh;
        private Selector sel;
        private ByteBuffer buf;


        @Override
        protected void loop() throws Exception {
            int read = in.read(buf);

            System.out.println(new String(buf, 0, read));
        }

        @Override
        protected void init() throws Exception {

            // создаем буфер для работы
            buf = ByteBuffer.allocate(BUF_SIZE);

            // Создаем селектор.
            sel = Selector.open();

            // открываем канал, который будет слушать сообщения, и отправлять наши сообщения
            socketCh = SocketChannel.open(new InetSocketAddress("localhost", 12345));

            serverCh.accept();


        }

        @Override
        protected void stop() throws Exception {
            socket.close();
        }
    }

    private static class Sender extends Worker {
        private OutputStream out;
        private final Socket socket;
        private Scanner scanner;

        public Sender(Socket socket) {
            this.socket = socket;
        }

        @Override
        protected void loop() throws Exception {
            String msg = scanner.nextLine();

            out.write(msg.getBytes("utf-8"));
        }

        @Override
        protected void init() throws Exception {
            out = socket.getOutputStream();
            scanner = new Scanner(System.in);
        }

        @Override
        protected void stop() throws Exception {
            socket.close();
        }
    }
}
