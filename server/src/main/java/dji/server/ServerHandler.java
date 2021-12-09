package dji.server;

import dji.common.objects.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class ServerHandler extends SimpleChannelInboundHandler <Message> {

    public static String CLIENT_DIR = "D:\\JAVA-projects\\GB_4\\GB_cloud\\client\\storage";
    private static final int BUFFER_SIZE = 1024 * 64;
    private static final File SERVER_DIR = new File ("D:\\JAVA-projects\\GB_cloud\\server\\storage");
    private File file;


    public ServerHandler() {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        //todo command handler
        if (msg instanceof CommandMessage) {
            String command = ((CommandMessage) msg).getCommand();
            String[] subCommand;
            ServerResponse response = new ServerResponse();
            response.setResponse("Server response");
            subCommand = command.split(":");
            switch (subCommand[0]) {

                case "?":
                    System.out.println("New command");
                    response.setResponse("Server response");
                    break;

                case "getFile":
                    System.out.println("New command");
                    if ((subCommand.length > 1)) {
                        System.out.println(SERVER_DIR + "\\" + subCommand[1]);
                        System.out.println(new File(SERVER_DIR + "\\" +  subCommand[1]).isFile());
                        if (new File(SERVER_DIR + "\\" + subCommand[1]).isFile()) {
                            requestFileMessage(ctx, subCommand[1]);
                        } else {
                            response.setResponse("File not found");
                        }
                    }
            }
            ctx.writeAndFlush(response);
        }

        if (msg instanceof RequestFileList) {

            List<File> files = new ArrayList<>();
            for (File item : SERVER_DIR.listFiles()) {
                files.add(item);
            }
            FileListMessage fileList = new FileListMessage();
            fileList.setFileList(files);
            ctx.writeAndFlush(fileList);
            ServerResponse response = new ServerResponse();
            response.setResponse("type the command or '?' to see command list");
            ctx.writeAndFlush(response);
        }
    }


        public void requestFileMessage (ChannelHandlerContext ctx, String file) {
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(SERVER_DIR.getAbsolutePath() + "\\" +  file, "r")) {
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
                    message.setName(file);
                    message.setContent(bytes);
                    message.setStartPosition(position);
                    ctx.writeAndFlush(message);
                } while (randomAccessFile.getFilePointer() < fileLength);
                ctx.writeAndFlush(new EndFileTransferMessage());
            } catch (IOException e) {
                e.printStackTrace();
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

