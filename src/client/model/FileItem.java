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

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (getClass() != obj.getClass()) return false;
        final FileItem other = (FileItem) obj;
        return (this.isDirectory == other.isDirectory) && this.name.equals(other.getName());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        result += prime * result + (isDirectory ? 1 : 0);
        return result;
    }
}
