package com.instana.depends;

public abstract class Dependency {
    private final String name;
    private byte[] content;
    private byte[] digest;

    public Dependency(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public byte[] getDigest() {
        return digest;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public void setDigest(byte[] digest) {
        this.digest = digest;
    }
}
