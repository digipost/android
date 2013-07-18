package no.digipost.android.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonFilter;

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFilter("toJSON")
public class Settings {
    @JsonProperty
    private ArrayList<SenderBlockingStatus> senderBlockingStatus;

    @JsonProperty
    private String acceptsInformation;

    @JsonProperty
    private String visibleInSearch;

    @JsonProperty
    private String notificationEmail;

    @JsonProperty
    private String reminderEmail;

    @JsonProperty
    private String notificationSmsPaidBySender;

    @JsonProperty
    private ArrayList<String> email;

    @JsonProperty
    private ArrayList<ExtendedEmail> extendedEmail;

    @JsonProperty
    private ExtendedPhone extendedPhone;

    @JsonProperty
    private String phonenumber;

    @JsonProperty
    private ArrayList<Link> link;

    public ArrayList<SenderBlockingStatus> getSenderBlockingStatus() {
        return senderBlockingStatus;
    }

    public void setSenderBlockingStatus(ArrayList<SenderBlockingStatus> senderBlockingStatus) {
        this.senderBlockingStatus = senderBlockingStatus;
    }

    public String getAcceptsInformation() {
        return acceptsInformation;
    }

    public void setAcceptsInformation(String acceptsInformation) {
        this.acceptsInformation = acceptsInformation;
    }

    public String getVisibleInSearch() {
        return visibleInSearch;
    }

    public void setVisibleInSearch(String visibleInSearch) {
        this.visibleInSearch = visibleInSearch;
    }

    public String getNotificationEmail() {
        return notificationEmail;
    }

    public void setNotificationEmail(String notificationEmail) {
        this.notificationEmail = notificationEmail;
    }

    public String getReminderEmail() {
        return reminderEmail;
    }

    public void setReminderEmail(String reminderEmail) {
        this.reminderEmail = reminderEmail;
    }

    public String getNotificationSmsPaidBySender() {
        return notificationSmsPaidBySender;
    }

    public void setNotificationSmsPaidBySender(String notificationSmsPaidBySender) {
        this.notificationSmsPaidBySender = notificationSmsPaidBySender;
    }

    public ArrayList<String> getEmail() {
        return email;
    }

    public void setEmail(ArrayList<String> email) {
        this.email = email;
    }

    public ArrayList<ExtendedEmail> getExtendedEmail() {
        return extendedEmail;
    }

    public void setExtendedEmail(ArrayList<ExtendedEmail> extendedEmail) {
        this.extendedEmail = extendedEmail;
    }

    public ExtendedPhone getExtendedPhone() {
        return extendedPhone;
    }

    public void setExtendedPhone(ExtendedPhone extendedPhone) {
        this.extendedPhone = extendedPhone;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public ArrayList<Link> getLink() {
        return link;
    }

    public void setLink(ArrayList<Link> link) {
        this.link = link;
    }
}
