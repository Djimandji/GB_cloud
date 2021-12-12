package dji.server;

import dji.common.objects.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class ServerHandler extends SimpleChannelInboundHandler <Message> {

    private static final String server = System.getProperty("user.dir") + "\\storage";
    private static final File SERVER_DIR = new File(server);
    private static final int BUFFER_SIZE = 1024 * 64;


    public ServerHandler() {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        //todo command handler
        if (msg instanceof CommandMessage) {
            String command = ((CommandMessage) msg).getCommand();
            String[] subCommand;
            ServerResponse response = new ServerResponse();
            response.setResponse("Unknown command");
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
                        System.out.println(new File(SERVER_DIR + "\\" + subCommand[1]).isFile());
                        if (new File(SERVER_DIR + "\\" + subCommand[1]).isFile()) {
                            requestFileMessage(ctx, subCommand[1]);
                        } else {
                            response.setResponse("File not found");
                        }
                    }

                case "sendFile":
                    System.out.println("New send file message");


                case "fileList":
                    channelRead0(ctx, new RequestFileList());
                    response.setResponse("command done");
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
        }

        if (msg instanceof FileTransferMessage) {
            var message = (FileTransferMessage) msg;
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(SERVER_DIR + "\\" + ((FileTransferMessage) msg).getName(), "rw")) {
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

