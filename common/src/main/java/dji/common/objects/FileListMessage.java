package dji.common.objects;

import java.io.File;
import java.util.List;

public class FileListMessage extends Message {
    private List <File> fileList;

    public List<File> getFileList() {
        return fileList;
    }

    public void setFileList(List<File> fileList) {
        this.fileList = fileList;
    }
}
