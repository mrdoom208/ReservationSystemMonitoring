package com.mycompany.reservationsystem.dto;

public class WebUpdateDTO {

    private String code;

    private String message;

    private String phone;

    private String reference;

    public WebUpdateDTO() {}

    public WebUpdateDTO(String code, String message,String phone, String reference) {
        this.code = code;
        this.message = message;
        this.phone = phone;
        this.reference = reference;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPhone(){return phone;}

    public void setPhone(String phone) {this.phone = phone;}

    public String getReference() {return reference;}

    public void setReference(String reference) {this.reference = reference;}
}
