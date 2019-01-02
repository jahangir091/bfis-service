package eu.flora.faobis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
public class FAOBISResponse {

    @XmlElement
    private Status status;

    @XmlElement
    public String message;

    @XmlElement
    private List<String> results = new ArrayList<String>();

    @XmlTransient
    public List<String> getResults() {
	return results;
    }

    @XmlTransient
    public Status getStatus() {
	return status;
    }

    public void setStatus(Status status) {
	this.status = status;
    }

    public enum Status {
	OK, INVALID_ARGUMENT, ERROR
    }

    public FAOBISResponse() {

    }

    public FAOBISResponse(Status status, String message) {
	this.message = message;
	this.status = status;
    }

    @XmlTransient
    public String getMessage() {
	return message;
    }

    public void setMessage(String message) {
	this.message = message;
    }

}
