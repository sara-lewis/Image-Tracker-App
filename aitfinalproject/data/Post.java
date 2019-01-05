package us.ait.android.aitfinalproject.data;

import java.io.Serializable;

public class Post implements Serializable {
    private String timestamp;
    private Double lat;
    private Double lan;
    private String body;
    private String imgUrl;


    public Post(String timestamp, Double lat, Double lan, String body, String imgUrl) {
        this.timestamp = timestamp;
        this.lat = lat;
        this.lan = lan;
        this.body = body;
        this.imgUrl = imgUrl;
    }

    public Post(){};


    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLan() {
        return lan;
    }

    public void setLan(Double lan) {
        this.lan = lan;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
