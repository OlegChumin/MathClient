package org.example.mathclient.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class OperationRequestDTO {
    private double a;
    private double b;
}