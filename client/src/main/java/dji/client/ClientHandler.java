package dji.client;

import dji.common.objects.EndFileTransferMessage;
import dji.common.objects.FileListMessage;
import dji.common.objects.FileTransferMessage;
import dji.common.objects.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class ClientHandler extends SimpleChannelInboundHandler<Message> {

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

        if (msg instanceof FileListMessage) {
            System.out.println("Files list on server: ");
            for (File file : ((FileListMessage) msg).getFileList()) {
                System.out.println(file.getName());
            }
        }
    }
}
