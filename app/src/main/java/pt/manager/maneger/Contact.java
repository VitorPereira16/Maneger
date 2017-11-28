package pt.manager.maneger;

/**
 * Created by Vitor on 27/11/2017.
 */

public class Contact {
    String id, type, type_name, contact_name, contact_number;

    public Contact(String id, String type, String type_name, String contact_name, String contact_number) {
        this.id = id;
        this.type = type;
        this.type_name = type_name;
        this.contact_name = contact_name;
        this.contact_number = contact_number;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getType_name() {
        return type_name;
    }

    public String getContact_name() {
        return contact_name;
    }

    public String getContact_number() {
        return contact_number;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setType_name(String type_name) {
        this.type_name = type_name;
    }

    public void setContact_name(String contact_name) {
        this.contact_name = contact_name;
    }

    public void setContact_number(String contact_number) {
        this.contact_number = contact_number;
    }
}
