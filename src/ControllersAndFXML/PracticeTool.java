package ControllersAndFXML;

import java.io.IOException;


public class PracticeTool {
    private Controller _controller;

    public PracticeTool(Controller controller) {
        _controller = controller;
    }

    public void recordButtonAction() throws IOException {
        _controller.recordButtonAction();
    }


}
