package dji.server;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import dji.common.JSON.JsonDecoder;
import dji.common.JSON.JsonEncoder;

import java.io.File;


public class Server {
    private final int port;
    private static final String server = System.getProperty("user.dir") + "\\storage";
    private static final File SERVER_DIR = new File(server);
    public static void main(String[] args) throws InterruptedException {
        new Server(9000).start();
    }

    public Server(int port) {
        this.port = port;
    }

    public void start() throws InterruptedException {
        if (!SERVER_DIR.exists()) {
            System.out.println("creating directory: " + SERVER_DIR.getName());
            SERVER_DIR.mkdir();
        }
        System.out.println(server);
            NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
            NioEventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                ServerBootstrap server = new ServerBootstrap();
                server
                        .group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<NioSocketChannel>() {
                            @Override
                            protected void initChannel(NioSocketChannel ch) {
                                ch.pipeline().addLast(
                                        new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 3, 0, 3),
                                        new LengthFieldPrepender(3),
                                        new JsonDecoder(),
                                        new JsonEncoder(),
                                        new ServerHandler()
                                );
                            }
                        })
                        .option(ChannelOption.SO_BACKLOG, 128)
                        .childOption(ChannelOption.SO_KEEPALIVE, true);

                ChannelFuture future = server.bind(port).sync();

                System.out.println("Server started");

                future.channel().closeFuture().sync();
            } finally {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        }
    }

