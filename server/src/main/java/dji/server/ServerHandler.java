package dji.server;

import dji.common.objects.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ServerHandler extends SimpleChannelInboundHandler <Message> {

    public static final String FILE_NAME = "D:\\JAVA-projects\\GB_4\\Netty\\file";
    private static final int BUFFER_SIZE = 1024 * 64;
    private static final File SERVER_DIR = new File ("D:\\JAVA-projects\\GB_cloud\\server\\storage");


    public ServerHandler() {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {

//        if (msg instanceof RequestFileMessage) {
//            try (RandomAccessFile randomAccessFile = new RandomAccessFile(FILE_NAME, "r")) {
//                final long fileLength = randomAccessFile.length();
//                do {
//                    var position = randomAccessFile.getFilePointer();
//                    long availableBytes = fileLength - position;
//                    byte[] bytes;
//                    if (availableBytes >= BUFFER_SIZE) {
//                        bytes = new byte[BUFFER_SIZE];
//                    } else {
//                        bytes = new byte[(int) availableBytes];
//                    }
//                    randomAccessFile.read(bytes);
//                    FileTransferMessage message = new FileTransferMessage();
//                    message.setContent(bytes);
//                    message.setStartPosition(position);
//                    ctx.writeAndFlush(message);
//                } while (randomAccessFile.getFilePointer() < fileLength);
//                ctx.writeAndFlush(new EndFileTransferMessage());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        if (msg instanceof RequestFileList) {

            List<File> files = new ArrayList<>();
            for (File item : SERVER_DIR.listFiles()) {
                files.add(item);
            }
            FileListMessage response = new FileListMessage();
            response.setFileList(files);
            ctx.writeAndFlush(response);
        }
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("New active channel");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("client disconnect");
    }
}

