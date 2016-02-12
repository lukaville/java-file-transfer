package client.model;

import java.io.File;

/**
 * Created by nickolay on 12.02.16.
 */
public class FileItem {
    private String name;
    private boolean isDirectory;

    public FileItem(String name, boolean isDirectory) {
        this.name = name;
        this.isDirectory = isDirectory;
    }

    public FileItem(File file) {
        this(file.getName(), file.isDirectory());
    }

    public String getName() {
        return name;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    @Override
    public String toString() {
        return name;
    }
}
