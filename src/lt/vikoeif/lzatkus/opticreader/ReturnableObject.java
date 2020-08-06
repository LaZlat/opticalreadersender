package lt.vikoeif.lzatkus.opticreader;

public class ReturnableObject {
    String fileName;
    String text;

    public ReturnableObject() {
    }

    public ReturnableObject(String fileName, String text) {
        this.fileName = fileName;
        this.text = text;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
