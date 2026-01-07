package com.mycompany.reservationsystem.dto;

public class WebUpdateDTO {

    private String code;

    private String message;

    private String phone;

    private String reference;

    private int pax;

    private String customerName;

    private String link;

    public WebUpdateDTO() {}

    public WebUpdateDTO(String code, String message,String phone, String reference, int pax ,String customerName) {
        this.code = code;
        this.message = message;
        this.phone = phone;
        this.reference = reference;
        this.pax = pax;
        this.customerName= customerName;
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

    public int getPax() {return pax;}

    public void setPax(int pax) {this.pax = pax;}

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getLink() {return link;}

    public void setLink(String link) {this.link = link;}
}
