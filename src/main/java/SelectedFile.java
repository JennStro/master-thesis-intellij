public class SelectedFile {

    private String name;
    private String content;

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public SelectedFile withName(String name) {
        this.name = name;
        return this;
    }

    public SelectedFile withContent(String content) {
        this.content = content;
        return this;
    }
}
