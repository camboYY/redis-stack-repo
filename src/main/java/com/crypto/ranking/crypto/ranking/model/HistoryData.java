package com.crypto.ranking.crypto.ranking.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistoryData {
    private String timestamp;
    private double value;
}
