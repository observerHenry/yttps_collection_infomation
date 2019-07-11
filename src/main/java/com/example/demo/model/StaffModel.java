package com.example.demo.model;

/**
 * @program:yttps_collection_infomation
 * @description:用于接收前台传入的值
 * @author: Mr.Henry
 * @create:2019/6/20 14:31
 */
public class StaffModel {
    private String name;
    private String base64Face;
    private String birthday;
    private String staffId;
    private String phone;
    private String tagName;

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBase64Face() {
        return base64Face;
    }

    public void setBase64Face(String base64Face) {
        this.base64Face = base64Face;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
