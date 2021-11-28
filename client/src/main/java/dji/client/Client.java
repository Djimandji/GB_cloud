package dji.client;

import dji.common.handler.JsonDecoder;
import dji.common.handler.JsonEncoder;
import dji.common.objects.EndFileTransferMessage;
import dji.common.objects.FileTransferMessage;
import dji.common.objects.Message;
import dji.common.objects.RequestFileMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Client {

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
                                    new SimpleChannelInboundHandler<Message>() {
                                        @Override
                                        protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
                                            if (msg instanceof FileTransferMessage) {
                                                var message = (FileTransferMessage) msg;
                                                try (RandomAccessFile randomAccessFile = new RandomAccessFile("Netty\\1" , "rw")) {
                                                    randomAccessFile.seek(message.getStartPosition());
                                                    randomAccessFile.write(message.getContent());
                                                } catch (FileNotFoundException e) {
                                                    e.printStackTrace();
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            if (msg instanceof EndFileTransferMessage) {
                                                System.out.println("File transfer is finished");
                                                ctx.close();
                                            }
                                        }
                                    }
                            );
                        }
                    });

            System.out.println("Client started");

            ChannelFuture channelFuture = bootstrap.connect("localhost", 9000).sync();
            channelFuture.channel().writeAndFlush(new RequestFileMessage());
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}
