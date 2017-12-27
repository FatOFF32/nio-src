package com.itmo.nio.chat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

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

    private static class Receiver extends Worker {
        private static final int BUF_SIZE = 1024;
        private SocketChannel socketCh;
        private Selector sel;
        private ByteBuffer buf;


        @Override
        protected void loop() throws Exception {

            // ждем новых событий
            sel.select();

            // получим ключи, на которые пришли события
            Set<SelectionKey> selKey = sel.selectedKeys();

            // Используем итератор для того, чтобы во время итерации по элементам была возможность удалять элементы из итератора
            Iterator<SelectionKey> iterator = selKey.iterator();

            while (iterator.hasNext()){

                SelectionKey curKey = iterator.next();

                if (curKey.isReadable()){

                    int read = socketCh.read(buf);
                    System.out.println(new String(buf.array(), 0, read));
                }

            }

        }

        @Override
        protected void init() throws Exception {

            // создаем буфер для работы
            buf = ByteBuffer.allocate(BUF_SIZE);

            // Создаем селектор.
            sel = Selector.open();

            // открываем канал, который будет слушать сообщения, и отправлять наши сообщения
            socketCh = SocketChannel.open();

            // конектимся к серверу
            socketCh.connect(new InetSocketAddress("localhost", 12345));

            socketCh.configureBlocking(false);

            socketCh.register(sel, socketCh.validOps());


        }

        @Override
        protected void stop() throws Exception {
            socketCh.close();
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
