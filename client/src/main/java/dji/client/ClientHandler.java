package dji.client;

import dji.common.objects.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class ClientHandler extends SimpleChannelInboundHandler<Message> {

    private static final String client = System.getProperty("user.dir") + "\\client" + "\\storage";
    private static final File CLIENT_DIR = new File(client);


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
            System.out.println("File list on server: ");
            for (File file : ((FileListMessage) msg).getFileList()) {
                System.out.println(file.getName());
            }
            System.out.println("File list on client: ");
            for (File file : CLIENT_DIR.listFiles()) {
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
