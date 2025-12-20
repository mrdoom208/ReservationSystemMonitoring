package com.mycompany.reservationsystem.constraint;
import io.github.palexdev.materialfx.controls.MFXTextField;

public class PhoneNumberRestriction {

    /**
     * Apply phone number restriction to MFXTextField
     * Automatically adds +63 prefix and formats as user types
     */
    public static void applyPhoneRestriction(MFXTextField textField) {
        // Set initial text with country code
        textField.setText("+63 ");

        // Add listener to enforce rules
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Ensure it always starts with +63
            if (newValue == null || newValue.isEmpty()) {
                textField.setText("+63 ");
                textField.positionCaret(4);
                return;
            }

            if (!newValue.startsWith("+63 ")) {
                textField.setText("+63 ");
                textField.positionCaret(4);
                return;
            }

            // Get only digits after +63
            String afterPrefix = newValue.substring(4);
            String digitsOnly = afterPrefix.replaceAll("[^0-9]", "");

            // Limit to 10 digits
            if (digitsOnly.length() > 10) {
                digitsOnly = digitsOnly.substring(0, 10);
            }

            // Must start with 9
            if (digitsOnly.length() > 0 && !digitsOnly.startsWith("9")) {
                textField.setText(oldValue);
                return;
            }

            // Format with spaces
            String formatted = formatPhoneNumber(digitsOnly);

            // Only update if changed to avoid infinite loop
            if (!newValue.equals(formatted)) {
                // Store caret position
                int caretPos = textField.getCaretPosition();
                textField.setText(formatted);

                // Restore caret position (adjusted for formatting)
                int newCaretPos = Math.min(caretPos, formatted.length());
                textField.positionCaret(newCaretPos);
            }
        });

        // Prevent cursor from going before +63
        textField.caretPositionProperty().addListener((obs, oldPos, newPos) -> {
            if (newPos.intValue() < 4) {
                textField.positionCaret(4);
            }
        });

        // Position caret at end initially
        textField.positionCaret(4);
    }

    /**
     * Format phone number with spaces
     * Input: "9171234567"
     * Output: "+63 917 123 4567"
     */
    private static String formatPhoneNumber(String digits) {
        StringBuilder formatted = new StringBuilder("+63 ");

        for (int i = 0; i < digits.length(); i++) {
            if (i == 3 || i == 6) {
                formatted.append(" ");
            }
            formatted.append(digits.charAt(i));
        }

        return formatted.toString();
    }

    /**
     * Get unformatted phone number (digits only with +63)
     * Input: "+63 917 123 4567"
     * Output: "+639171234567"
     */
    public static String getUnformattedNumber(MFXTextField textField) {
        String text = textField.getText();
        return text.replaceAll("\\s", ""); // Remove spaces
    }

    /**
     * Get digits only (no country code)
     * Input: "+63 917 123 4567"
     * Output: "9171234567"
     */
    public static String getDigitsOnly(MFXTextField textField) {
        String text = textField.getText();
        return text.substring(4).replaceAll("[^0-9]", "");
    }

    /**
     * Validate if phone number is complete (10 digits)
     */
    public static boolean isValidPhoneNumber(MFXTextField textField) {
        String digitsOnly = getDigitsOnly(textField);
        return digitsOnly.length() == 10 && digitsOnly.startsWith("9");
    }

    /**
     * Alternative: Simple digit-only restriction (no formatting with spaces)
     * Format: +639XXXXXXXXX
     */
    public static void applySimplePhoneRestriction(MFXTextField textField) {
        textField.setText("+63");

        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Ensure it starts with +63
            if (newValue == null || newValue.isEmpty()) {
                textField.setText("+63");
                textField.positionCaret(3);
                return;
            }

            if (!newValue.startsWith("+63")) {
                textField.setText("+63");
                textField.positionCaret(3);
                return;
            }

            // Get digits after +63
            String digits = newValue.substring(3).replaceAll("[^0-9]", "");

            // Limit to 10 digits
            if (digits.length() > 10) {
                digits = digits.substring(0, 10);
            }

            // Must start with 9
            if (digits.length() > 0 && !digits.startsWith("9")) {
                textField.setText(oldValue);
                return;
            }

            // Update text
            String correctText = "+63" + digits;
            if (!newValue.equals(correctText)) {
                int caretPos = textField.getCaretPosition();
                textField.setText(correctText);
                textField.positionCaret(Math.min(caretPos, correctText.length()));
            }
        });

        // Prevent cursor before +63
        textField.caretPositionProperty().addListener((obs, oldPos, newPos) -> {
            if (newPos.intValue() < 3) {
                textField.positionCaret(3);
            }
        });

        textField.positionCaret(3);
    }

    /**
     * Clear the phone field (reset to +63)
     */
    public static void clearPhoneField(MFXTextField textField) {
        textField.setText("+63 ");
        textField.positionCaret(4);
    }
}

/*
 * USAGE EXAMPLE:
 *
 * @FXML
 * private MFXTextField phoneField;
 *
 * @Override
 * public void initialize(URL location, ResourceBundle resources) {
 *     // Apply formatted restriction (+63 917 123 4567)
 *     PhoneNumberRestriction.applyPhoneRestriction(phoneField);
 *
 *     // OR simple version (+639171234567)
 *     // PhoneNumberRestriction.applySimplePhoneRestriction(phoneField);
 * }
 *
 * @FXML
 * private void handleSubmit() {
 *     if (PhoneNumberRestriction.isValidPhoneNumber(phoneField)) {
 *         String phone = PhoneNumberRestriction.getUnformattedNumber(phoneField);
 *         // Save: +639171234567
 *     } else {
 *         showError("Please enter a valid 10-digit phone number");
 *     }
 * }
 */