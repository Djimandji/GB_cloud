package dji.client;

import dji.common.JSON.JsonDecoder;
import dji.common.JSON.JsonEncoder;
import dji.common.objects.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;


import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Client {

    private static final String client = System.getProperty("user.dir") + "\\client" + "\\storage";
    private static final File CLIENT_DIR = new File(client);
    private static final int BUFFER_SIZE = 1024 * 64;

    public static void main(String[] args) throws InterruptedException {
        new Client().start();
    }

    public void start() {
        final NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(
                                    new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 3, 0, 3),
                                    new LengthFieldPrepender(3),
                                    new JsonDecoder(),
                                    new JsonEncoder(),
                                    new ClientHandler()
                            );
                        }
                    });

            System.out.println("Client started");
            System.out.println(client);
            if (!CLIENT_DIR.exists()) {
                System.out.println("creating directory: " + CLIENT_DIR.getName());
                CLIENT_DIR.mkdir();
            }
            ChannelFuture channelFuture = bootstrap.connect("localhost", 9000).sync();
            channelFuture.channel().writeAndFlush(new RequestFileList());
            Scanner sc = new Scanner(System.in);
            String command;
            String[] subCommand;
            CommandMessage commandMsg = new CommandMessage();
            System.out.println("type the command or ? to see command list");

            while (channelFuture.channel().isActive()) {
                command = sc.next();
                commandMsg.setCommand(command);
                if (command.equals("disconnect")) {
                    channelFuture.channel().close();
                    channelFuture.channel().closeFuture().sync();
                    System.out.println("Client disconnect");
                }
                if (command.startsWith("sendFile:")) {
                    subCommand = command.split(":");
                    if (subCommand.length > 1) {
                        if (new File(CLIENT_DIR + "\\" + subCommand[1]).isFile()) {
                            try (RandomAccessFile randomAccessFile = new RandomAccessFile(CLIENT_DIR.getAbsolutePath() + "\\" +  subCommand[1], "r")) {
                                final long fileLength = randomAccessFile.length();
                                do {
                                    var position = randomAccessFile.getFilePointer();
                                    long availableBytes = fileLength - position;
                                    byte[] bytes;
                                    if (availableBytes >= BUFFER_SIZE) {
                                        bytes = new byte[BUFFER_SIZE];
                                    } else {
                                        bytes = new byte[(int) availableBytes];
                                    }
                                    randomAccessFile.read(bytes);
                                    FileTransferMessage message = new FileTransferMessage();
                                    message.setName(subCommand[1]);
                                    message.setContent(bytes);
                                    message.setStartPosition(position);
                                    channelFuture.channel().writeAndFlush(message);
                                } while (randomAccessFile.getFilePointer() < fileLength);
                                channelFuture.channel().writeAndFlush(new EndFileTransferMessage());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            System.out.println("file not found");
                        }
                    }
                }
                channelFuture.channel().writeAndFlush(commandMsg);
            }
            channelFuture.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}
