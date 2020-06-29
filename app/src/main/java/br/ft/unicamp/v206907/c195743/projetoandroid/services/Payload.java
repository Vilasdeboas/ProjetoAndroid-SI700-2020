package br.ft.unicamp.v206907.c195743.projetoandroid.services;

import com.google.firebase.database.Exclude;

public class Payload {

    private String name;
    private String description;
    private String tag;
    private String uri;
    private String key;
    private String extension;

    public Payload() {
    }

    public Payload(String name, String description, String tag, String uri, String extension) {
        this.name = name;
        this.description = description;
        this.tag = tag;
        this.uri = uri;
        this.extension = extension;
    }

    //Name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    //Description
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    //Tag
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    //Uri
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    //Key
    @Exclude
    public String getKey() {
        return key;
    }

    @Exclude
    public void setKey(String key) {
        this.key = key;
    }

    //Extension
    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }
}
