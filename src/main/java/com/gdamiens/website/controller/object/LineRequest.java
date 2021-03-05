package com.gdamiens.website.controller.object;

public class LineRequest {

    private String lineId;

    private String code;

    private String codeStif;

    private String realm;

    private String reseau;

    public LineRequest() {
    }

    public String getLineId() {
        return lineId;
    }

    public void setLineId(String lineId) {
        this.lineId = lineId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCodeStif() {
        return codeStif;
    }

    public void setCodeStif(String codeStif) {
        this.codeStif = codeStif;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getReseau() {
        return reseau;
    }

    public void setReseau(String reseau) {
        this.reseau = reseau;
    }
}
