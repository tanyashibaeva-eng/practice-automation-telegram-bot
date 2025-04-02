package ru.itmo.domain.dto;

import lombok.Data;
import ru.itmo.domain.type.StudentStatus;

@Data
public class Student {
    private long chatId;
    private long eduStreamId;
    private int isu;
    private String st_group;
    private String fullname;
    private StudentStatus status;
    private String comments;
    private int company_inn;
    private String company_name;
    private String company_lead_fullname;
    private String company_lead_phone;
    private String company_lead_email;
    private String company_lead_job_title;
}
