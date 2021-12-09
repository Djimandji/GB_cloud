package dji.client;

import dji.common.objects.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class ClientHandler extends SimpleChannelInboundHandler<Message> {

    public static String CLIENT_DIR = "D:\\JAVA-projects\\GB_cloud\\client\\storage";

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        if (msg instanceof FileTransferMessage) {
            var message = (FileTransferMessage) msg;
            System.out.println(CLIENT_DIR + "\\" + ((FileTransferMessage) msg).getName());
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(CLIENT_DIR + "\\" + ((FileTransferMessage) msg).getName() , "rw")) {
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

        if (msg instanceof FileListMessage) {
            System.out.println("Files list on server: ");
            for (File file : ((FileListMessage) msg).getFileList()) {
                System.out.println(file.getName());
            }
        }

        if (msg instanceof CommandMessage) {
            System.out.println(((CommandMessage) msg).getCommand());
        }

        if (msg instanceof ServerResponse) {
            System.out.println(((ServerResponse) msg).getResponse());
        }
    }
}
