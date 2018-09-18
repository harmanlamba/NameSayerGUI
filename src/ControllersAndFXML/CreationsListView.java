package ControllersAndFXML;

import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXListCell;

import javafx.scene.Node;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import NameSayer.backend.Recording;
import NameSayer.backend.Creation;
import NameSayer.backend.CreationStore;

public class CreationsListView extends JFXListView<Creation> {

    private ObservableList<Recording> _selectedRecordings = FXCollections.observableArrayList();
    private ObjectProperty<CreationStore> _creationStore = new SimpleObjectProperty<CreationStore>();

    public CreationsListView() {

        _creationStore.addListener(observer1 -> {
            // Dummy:
            _selectedRecordings.setAll(
                    _creationStore
                            .getValue()
                            .get("Ratnayake")
                            .getVersions()
                            .get(0),
                    _creationStore
                            .getValue()
                            .get("Xuyun")
                            .getVersions()
                            .get(0));

            getItems().setAll(_creationStore.getValue().getCreations());
            _creationStore.getValue().addListener(observer2 -> {
                // TODO: search filtered, preserve selections.
                getItems().setAll(_creationStore.getValue().getCreations());
            });
        });

        setCellFactory(listView -> new JFXListCell<Creation>() {
            @Override
            public void updateItem(Creation creation, boolean empty) {
                super.updateItem(creation, empty);
                if (!empty) {
                    assert creation != null;
                    setText("Temporary cell: " + creation.getName());
                    setGraphic(null);
                    // TODO
                }
            }
        });
    }

    public void setCreationStore(CreationStore store) {
        _creationStore.setValue(store);
    }

    public ObservableList<Recording> getSelectedRecordings() {
        return _selectedRecordings;
    }

    public String selectNext() {
        return "Selecting Next";
    }

    public String selectPrevious() {
        return "Selecting Previous";
    }

}
