package com.mycompany.reservationsystem.util;

import com.mycompany.reservationsystem.model.Message;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import javafx.util.StringConverter;

public class ComboBoxUtil {
    public void ComboBoxUtil(){

    }
    public static void MFXComboboxMessageFormat(MFXComboBox<Message> messages){
        messages.setConverter(new StringConverter<>() {
            @Override
            public String toString(Message message) {
                return message == null ? "" : message.getMessageLabel();
            }

            @Override
            public Message fromString(String text) {
                Message selected = messages.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    selected.setMessageLabel(text);
                    return selected;
                }
                return new Message(text, "");
            }
        });
    }
    public static <T> T selectTopItem(MFXComboBox<T> comboBox) {
        if (comboBox.getItems().isEmpty()) return null;

        T topItem = comboBox.getItems().get(0);
        comboBox.getSelectionModel().selectIndex(0);
        return topItem;
    }
}
