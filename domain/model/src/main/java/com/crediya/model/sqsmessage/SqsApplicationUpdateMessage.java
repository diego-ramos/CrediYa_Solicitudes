package com.crediya.model.sqsmessage;

import com.crediya.model.application.FirstInstallment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class SqsApplicationUpdateMessage {
    String email;
    Long applicationId;
    String newApplicationStatus;
    List<FirstInstallment> installments;
}
