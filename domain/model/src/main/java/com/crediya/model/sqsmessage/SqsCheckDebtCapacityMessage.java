package com.crediya.model.sqsmessage;

import com.crediya.model.application.Application;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class SqsCheckDebtCapacityMessage {
    Application application;
}
