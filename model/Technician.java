package model;

public class Technician {

    private int technicianId;
    private String name;
    private String contactNo;
    private String email;
    private String department;
    private String status;


    public Technician() {

        status="Available";

    }


    public Technician(int technicianId,
                      String name,
                      String contactNo,
                      String email,
                      String department,
                      String status) {

        this.technicianId=technicianId;
        this.name=name;
        this.contactNo=contactNo;
        this.email=email;
        this.department=department;
        this.status=status;

    }



    public int getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(int technicianId) {
        this.technicianId = technicianId;
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }



    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }



    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }



    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }



    @Override
    public String toString() {

        return name+" ("+status+")";

    }

}