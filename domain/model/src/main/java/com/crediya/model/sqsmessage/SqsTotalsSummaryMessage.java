package com.crediya.model.sqsmessage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class SqsTotalsSummaryMessage {
    List<Total> totalList;
    List<String> recipients;
}
